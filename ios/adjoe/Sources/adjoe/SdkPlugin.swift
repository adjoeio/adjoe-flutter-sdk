import Flutter
import UIKit

public class SdkPlugin: NSObject, FlutterPlugin {
    private var shelfBridge: ShelfBridge?
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "sdk", binaryMessenger: registrar.messenger())
        let instance = SdkPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
        MainActor.assumeIsolated {
            let bridge = ShelfBridge(messenger: registrar.messenger())
            instance.shelfBridge = bridge
            ShelfHostApiSetup.setUp(binaryMessenger: registrar.messenger(), api: bridge)
            registrar.register(ShelfPlatformViewFactory(bridge: bridge), withId: "io.adjoe.playtime/shelf")
            let playtime = PlaytimeImpl(onTeardown: { [weak bridge] in bridge?.reset() })
            PlaytimeSetup.setUp(binaryMessenger: registrar.messenger(), api: playtime)
        }
        PlaytimeStudioSetup.setUp(binaryMessenger: registrar.messenger(), api: PlaytimeStudioImpl())
        StreamEventsStreamHandler.register(
            with: registrar.messenger(),
            streamHandler: PlaytimeEventForwarder.shared
        )
    }

    public func detachFromEngine(for registrar: FlutterPluginRegistrar) {
        MainActor.assumeIsolated {
            shelfBridge?.reset()
            ShelfHostApiSetup.setUp(binaryMessenger: registrar.messenger(), api: nil)
            shelfBridge = nil
        }
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        default:
            result(FlutterMethodNotImplemented)
        }
    }
}
