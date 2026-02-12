package io.adjoe.flutter.sdk

import android.app.Activity
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** SdkPlugin */
class SdkPlugin :
    FlutterPlugin,
    MethodCallHandler,
    ActivityAware{
    // The MethodChannel that will the communication between Flutter and native Android
    //
    // This local reference serves to register the plugin with the Flutter Engine and unregister it
    // when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var playtimeImpl: PlaytimeImpl
    private lateinit var playtimeStudioImpl: PlaytimeStudioImpl

    public override fun onReattachedToActivityForConfigChanges(@NonNull binding: ActivityPluginBinding) {
        playtimeImpl.setActivity(binding.getActivity())
        playtimeStudioImpl.setActivity(binding.getActivity())
    }

    public override fun onAttachedToActivity(@NonNull binding: ActivityPluginBinding) {
        playtimeImpl.setActivity(binding.getActivity())
        playtimeStudioImpl.setActivity(binding.getActivity())
    }

    public override fun onDetachedFromActivityForConfigChanges() {
        playtimeImpl.setActivity(null)
        playtimeStudioImpl.setActivity(null)
    }

    public override fun onDetachedFromActivity() {
        playtimeImpl.setActivity(null)
        playtimeStudioImpl.setActivity(null)
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "adjoe")
        channel.setMethodCallHandler(this)
        playtimeImpl = PlaytimeImpl()
        playtimeStudioImpl = PlaytimeStudioImpl()
        Playtime.setUp(flutterPluginBinding.binaryMessenger, playtimeImpl)
        PlaytimeStudio.setUp(flutterPluginBinding.binaryMessenger, playtimeStudioImpl)
    }

    override fun onMethodCall(
        call: MethodCall,
        result: Result
    ) {
        result.notImplemented()
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        Playtime.setUp(binding.binaryMessenger, null)
        PlaytimeStudio.setUp(binding.binaryMessenger, null)
    }
}
