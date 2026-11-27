import Flutter
@_spi(Experimental) import PlaytimeSDK
import UIKit

final class ShelfPlatformViewFactory: NSObject, FlutterPlatformViewFactory {
    private let bridge: ShelfBridge

    init(bridge: ShelfBridge) {
        self.bridge = bridge
    }

    func createArgsCodec() -> FlutterMessageCodec & NSObjectProtocol {
        FlutterStandardMessageCodec.sharedInstance()
    }

    func create(withFrame frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?) -> FlutterPlatformView {
        MainActor.assumeIsolated {
            let controllerId = (args as? NSNumber)?.int64Value ?? -1
            let view = ShelfPlatformView(frame: frame, controllerId: controllerId)
            bridge.register(view, id: viewId)
            return view
        }
    }
}

final class ShelfPlatformView: NSObject, FlutterPlatformView {
    let controllerId: Int64
    let container: ShelfHostView

    @MainActor
    init(frame: CGRect, controllerId: Int64) {
        self.controllerId = controllerId
        container = ShelfHostView(frame: frame)
        super.init()
    }

    func view() -> UIView {
        container
    }
}

/// Explicit width measurement breaks the zero-height platform-view bootstrap cycle.
@MainActor
final class ShelfHostView: UIView {
    var measurementWidth: CGFloat = 0
    private var shelf: UIView?
    private var measuring = false

    func install(_ view: UIView) {
        removeShelf()
        shelf = view
        addSubview(view)
        setNeedsLayout()
    }

    func removeShelf() {
        shelf?.removeFromSuperview()
        shelf = nil
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        let frame = CGRect(origin: .zero, size: CGSize(
            width: measurementWidth > 0 ? measurementWidth : bounds.width,
            height: bounds.height
        ))
        if shelf?.frame != frame {
            shelf?.frame = frame
        }
    }

    func measuredHeight() -> CGFloat? {
        guard let shelf, !measuring else { return nil }
        let width = measurementWidth > 0 ? measurementWidth : bounds.width
        guard width > 0 else { return nil }
        measuring = true
        defer { measuring = false }
        if shelf.bounds.width != width {
            shelf.frame.size.width = width
            shelf.setNeedsLayout()
        }
        shelf.layoutIfNeeded()
        // The native renderer caches intrinsic height after its width-driven layout.
        // Fitting measurement creates throwaway card views for the entire corpus.
        return shelf.intrinsicContentSize.height
    }
}
