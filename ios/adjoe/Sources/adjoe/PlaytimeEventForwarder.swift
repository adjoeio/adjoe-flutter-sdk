import Foundation
import PlaytimeSDK

/// Bridges the native SDK's `PlaytimeEventListener` stream onto the Pigeon `PlaytimeEvents`
/// event channel.
///
/// The native SDK delivers events through a single registered listener, while Dart consumes
/// them through an event-channel `PigeonEventSink` that only exists once Dart has subscribed.
/// This object owns both ends so the two lifecycles (host `addEventListener` vs. channel
/// `onListen`) can be wired together regardless of their order.
final class PlaytimeEventForwarder: StreamEventsStreamHandler, @unchecked Sendable {
    static let shared = PlaytimeEventForwarder()

    private var sink: PigeonEventSink<PlaytimeEvent>?

    override func onListen(withArguments arguments: Any?, sink: PigeonEventSink<PlaytimeEvent>) {
        self.sink = sink
    }

    override func onCancel(withArguments arguments: Any?) {
        self.sink = nil
    }
}

extension PlaytimeEventForwarder: PlaytimeSDK.PlaytimeEventListener {
    func didReceive(_ event: PlaytimeSDK.PlaytimeEvent) {
        let occurredAt = ISO8601DateFormatter.playtimeEventFormatter.string(from: event.occurredAt)
        sink?.success(PlaytimeEvent(name: event.name, occurredAt: occurredAt))
    }
}

private extension ISO8601DateFormatter {
    /// Formats `PlaytimeEvent.occurredAt` as the ISO 8601 string carried across the bridge,
    /// matching what the Dart side (`DateTime.parse`) and the Android wrapper expect.
    static let playtimeEventFormatter: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return formatter
    }()
}
