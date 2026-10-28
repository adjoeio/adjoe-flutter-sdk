import Flutter
import UIKit

public class SdkPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "sdk", binaryMessenger: registrar.messenger())
    let instance = SdkPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
    PlaytimeSetup.setUp(binaryMessenger: registrar.messenger(), api: PlaytimeImpl())
    PlaytimeStudioSetup.setUp(binaryMessenger: registrar.messenger(), api: PlaytimeStudioImpl())
    StreamEventsStreamHandler.register(
      with: registrar.messenger(),
      streamHandler: PlaytimeEventForwarder.shared
    )
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    default:
      result(FlutterMethodNotImplemented)
    }
  }
}
