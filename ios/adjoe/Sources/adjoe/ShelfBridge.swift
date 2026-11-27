import Flutter
import Foundation
@_spi(Experimental) import PlaytimeSDK
import UIKit

/// Engine-owned sessions. Pigeon and platform-view callbacks use Flutter's main thread.
@MainActor
final class ShelfBridge: NSObject, ShelfHostApi {
    private let callbacks: ShelfFlutterApi
    private var sessions: [Int64: ShelfSessionEntry] = [:]
    private var creations: [Int64: Task<Void, Never>] = [:]
    private var views: [Int64: WeakShelfPlatformView] = [:]

    init(messenger: FlutterBinaryMessenger) {
        callbacks = ShelfFlutterApi(binaryMessenger: messenger)
    }

    deinit {
        for task in creations.values {
            task.cancel()
        }
    }

    nonisolated func create(
        controllerId: Int64,
        options: ShelfOptionsMessage,
        completion: @escaping (Result<Void, Error>) -> Void
    ) {
        MainActor.assumeIsolated {
            guard sessions[controllerId] == nil, creations[controllerId] == nil else {
                completion(.failure(error("duplicate-controller", "Shelf controller already exists.")))
                return
            }
            guard !options.placement.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
                completion(.failure(error("invalid-placement", "Shelf placement must not be blank.")))
                return
            }
            let category: PlaytimeSDK.AppCategory
            switch options.category {
            case "gaming": category = .gaming
            case "non-gaming": category = .nonGaming
            case "both": category = .both
            default:
                completion(.failure(error("invalid-category", "Unknown Shelf category.")))
                return
            }
            creations[controllerId] = Task { @MainActor [weak self] in
                do {
                    let nativeOptions = PlaytimeShelfOptions(
                        placement: PlaytimeShelfPlacement(rawValue: options.placement),
                        requestedVariant: options.requestedVariant,
                        category: category,
                        actionObserver: { [weak self] context in
                            self?.sendAction(context, controllerId: controllerId)
                        },
                        onStatusChanged: { [weak self] status in
                            self?.sendStatus(status, controllerId: controllerId)
                        },
                        onSizeChanged: { [weak self] size in
                            self?.reportSize(controllerId: controllerId, height: size.height)
                        }
                    )
                    let controller = try await PlaytimeSDK.PlaytimeStudio.makeShelfController(options: nativeOptions)
                    try Task.checkCancellation()
                    guard let self else { throw CancellationError() }
                    sessions[controllerId] = ShelfSessionEntry(controller: controller)
                    creations[controllerId] = nil
                    completion(.success(()))
                } catch {
                    self?.creations[controllerId] = nil
                    completion(.failure(ErrorsUtil.toPigeonError(error)))
                }
            }
        }
    }

    nonisolated func prefetch(controllerId: Int64) throws {
        try MainActor.assumeIsolated { try session(controllerId).controller.prefetch() }
    }

    nonisolated func refresh(controllerId: Int64) throws {
        try MainActor.assumeIsolated { try session(controllerId).controller.refresh() }
    }

    nonisolated func attach(controllerId: Int64, viewId: Int64, attachmentId: Int64) throws {
        try MainActor.assumeIsolated {
            let entry = try session(controllerId)
            guard entry.attachmentId == nil else {
                throw error("already-attached", "Only one view may mount a Shelf controller.")
            }
            guard let platformView = views[viewId]?.value,
                  platformView.controllerId == controllerId
            else {
                throw error("missing-view", "Shelf platform view is no longer available.")
            }
            entry.attachmentId = attachmentId
            entry.platformView = platformView
            // Install routing before makeShelfView(), which synchronously replays status.
            platformView.container.install(entry.controller.makeShelfView())
            reportSize(controllerId: controllerId)
        }
    }

    nonisolated func measure(controllerId: Int64, attachmentId: Int64, width: Double) throws {
        try MainActor.assumeIsolated {
            let entry = try session(controllerId)
            guard entry.attachmentId == attachmentId else { return }
            guard width.isFinite, width > 0 else {
                throw error("invalid-width", "Shelf width must be finite and positive.")
            }
            entry.platformView?.container.measurementWidth = CGFloat(width)
            entry.platformView?.container.setNeedsLayout()
            reportSize(controllerId: controllerId)
        }
    }

    nonisolated func detach(controllerId: Int64, attachmentId: Int64) throws {
        MainActor.assumeIsolated {
            guard let entry = sessions[controllerId], entry.attachmentId == attachmentId else { return }
            entry.platformView?.container.removeShelf()
            entry.platformView = nil
            entry.attachmentId = nil
            entry.lastHeight = nil
        }
    }

    nonisolated func dispose(controllerId: Int64) throws {
        MainActor.assumeIsolated { release(controllerId) }
    }

    func reset() {
        let ids = Set(sessions.keys).union(creations.keys)
        for id in ids {
            release(id)
            callbacks.disposed(controllerId: id, completion: { _ in })
        }
    }

    func register(_ view: ShelfPlatformView, id: Int64) {
        views = views.filter { $0.value.value != nil }
        views[id] = WeakShelfPlatformView(value: view)
    }

    func reportSize(controllerId: Int64, height reportedHeight: CGFloat? = nil) {
        guard let entry = sessions[controllerId], let attachment = entry.attachmentId,
              let container = entry.platformView?.container,
              let height = reportedHeight ?? container.measuredHeight(), height.isFinite, height >= 0,
              entry.lastHeight != height else { return }
        entry.lastHeight = height
        callbacks.sizeChanged(
            controllerId: controllerId, attachmentId: attachment, height: Double(height),
            completion: { _ in }
        )
    }

    private func release(_ id: Int64) {
        creations.removeValue(forKey: id)?.cancel()
        let entry = sessions.removeValue(forKey: id)
        entry?.platformView?.container.removeShelf()
    }

    private func session(_ id: Int64) throws -> ShelfSessionEntry {
        guard let entry = sessions[id] else {
            throw error("disposed-controller", "Shelf controller is missing or disposed.")
        }
        return entry
    }

    private func error(_ code: String, _ message: String) -> PigeonError {
        PigeonError(code: code, message: message, details: nil)
    }

    private func sendStatus(_ status: PlaytimeShelfStatus, controllerId: Int64) {
        guard sessions[controllerId] != nil else { return }
        let message: ShelfStatusMessage
        switch status {
        case .idle: message = ShelfStatusMessage(kind: .idle)
        case .loading: message = ShelfStatusMessage(kind: .loading)
        case let .ready(available, installed):
            message = ShelfStatusMessage(
                kind: .ready, availableCampaigns: Int64(available), installedCampaigns: Int64(installed)
            )
        case let .empty(reason):
            message = ShelfStatusMessage(kind: .empty, reason: emptyReason(reason))
        case let .unavailable(reason, retryable, description):
            message = ShelfStatusMessage(
                kind: .unavailable, reason: unavailableReason(reason), retryable: retryable, message: description
            )
        @unknown default:
            message = ShelfStatusMessage(kind: .unavailable, reason: "unknown", retryable: false)
        }
        callbacks.statusChanged(controllerId: controllerId, status: message, completion: { _ in })
    }

    private func sendAction(_ context: PlaytimeShelfActionContext, controllerId: Int64) {
        guard sessions[controllerId] != nil else { return }
        callbacks.actionPerformed(
            controllerId: controllerId, campaignId: context.campaignId,
            actionName: context.actionName, completion: { _ in }
        )
    }

    private func emptyReason(_ reason: PlaytimeShelfEmptyReason) -> String {
        switch reason {
        case .noCampaigns: "noCampaigns"
        case .allCampaignsSkipped: "allCampaignsSkipped"
        case .unknown: "unknown"
        @unknown default: "unknown"
        }
    }

    private func unavailableReason(_ reason: PlaytimeShelfUnavailableReason) -> String {
        switch reason {
        case .notConfigured: "notConfigured"
        case .unsupportedSdkVersion: "unsupportedSdkVersion"
        case .network: "network"
        case .invalidConfiguration: "invalidConfiguration"
        case .sdkNotInitialized: "sdkNotInitialized"
        case .tosNotAccepted: "tosNotAccepted"
        case .unknown: "unknown"
        @unknown default: "unknown"
        }
    }
}

@MainActor
private final class ShelfSessionEntry {
    let controller: PlaytimeShelfController
    weak var platformView: ShelfPlatformView?
    var attachmentId: Int64?
    var lastHeight: CGFloat?

    init(controller: PlaytimeShelfController) {
        self.controller = controller
    }
}

private struct WeakShelfPlatformView {
    weak var value: ShelfPlatformView?
}
