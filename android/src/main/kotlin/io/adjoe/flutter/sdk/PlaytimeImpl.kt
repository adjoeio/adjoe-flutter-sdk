package io.adjoe.flutter.sdk

import android.app.Activity
import io.adjoe.sdk.PlaytimeInitialisationListener
import io.adjoe.sdk.PlaytimeOptionsListener

class PlaytimeImpl() : Playtime {
    private var activity: Activity? = null

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    override fun init(
        apiKey: String,
        options: PlaytimeOptions?,
        callback: (Result<Unit>) -> Unit
    ) {
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        val listener = object : PlaytimeInitialisationListener {
            override fun onInitialisationError(exception: Exception?) {
                exception?.let {
                    callback(Result.failure(Exception(exception)))
                    return
                }

                callback(Result.failure(ErrorsUtil.getFunctionError("init")))
            }

            override fun onInitialisationFinished() {
                callback(Result.success(Unit))
            }
        }

        if (options == null) {
            io.adjoe.sdk.Playtime.init(activity!!, apiKey, listener)
        } else {
            io.adjoe.sdk.Playtime.init(activity!!, apiKey, OptionsUtil.mapOptions(options), listener)
        }
    }

    override fun showCatalog(
        params: PlaytimeParams?,
        callback: (Result<Unit>) -> Unit
    ) {
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        if (params == null) {
            io.adjoe.sdk.Playtime.showCatalog(activity!!, io.adjoe.sdk.PlaytimeOptions())
        } else {
            io.adjoe.sdk.Playtime.showCatalog(
                activity!!,
                io.adjoe.sdk.PlaytimeOptions().setParams(OptionsUtil.mapParams(params))
            )
        }

        callback(Result.success(Unit))
    }

    override fun showCatalogWithOptions(
        options: PlaytimeOptions,
        callback: (Result<Unit>) -> Unit
    ) {
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.Playtime.showCatalog(activity!!, OptionsUtil.mapOptions(options))
        callback(Result.success(Unit))
    }

    override fun setPlaytimeOptions(
        options: PlaytimeOptions,
        callback: (Result<Unit>) -> Unit
    ) {
        val listener = object : PlaytimeOptionsListener {
            override fun onSuccess() {
                callback(Result.success(Unit))
            }

            override fun onError(error: String) {
                callback(Result.failure(Exception(error)))
            }
        }

        io.adjoe.sdk.Playtime.setPlaytimeOptions(OptionsUtil.mapOptions(options), listener)
    }

    override fun setUAParams(
        params: PlaytimeParams,
        callback: (Result<Unit>) -> Unit
    ) {
        val playtimeParams = OptionsUtil.mapParams(params)

        playtimeParams?.let {
            io.adjoe.sdk.Playtime.setUAParams(activity!!, playtimeParams)
            callback(Result.success(Unit))
            return
        }

        callback(Result.failure(ErrorsUtil.getFunctionError("setUAParams")))
    }

    override fun getVersion(callback: (Result<String>) -> Unit) {
        callback(Result.success(io.adjoe.sdk.Playtime.version.toString()))
    }

    override fun getVersionName(callback: (Result<String>) -> Unit) {
        callback(Result.success(io.adjoe.sdk.Playtime.versionName))
    }

    override fun isInitialized(callback: (Result<Boolean>) -> Unit) {
        callback(Result.success(io.adjoe.sdk.Playtime.isInitialized()))
    }

    override fun hasAcceptedTOS(callback: (Result<Boolean>) -> Unit) {
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        callback(Result.success(io.adjoe.sdk.Playtime.hasAcceptedTOS(activity!!)))
    }

    override fun hasAcceptedUsagePermission(callback: (Result<Boolean>) -> Unit) {
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        callback(
            Result.success(
                io.adjoe.sdk.Playtime.hasAcceptedUsagePermission(activity!!)
            )
        )
    }

    override fun getUserId(callback: (Result<String?>) -> Unit) {
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        callback(
            Result.success(
                io.adjoe.sdk.Playtime.getUserId(activity!!)
            )
        )
    }

    override fun sendEvent(
        event: Long,
        extra: String,
        params: PlaytimeParams?,
        callback: (Result<Unit>) -> Unit
    ) {
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        val playtimeParams = OptionsUtil.mapParams(params)

        try {
            io.adjoe.sdk.Playtime.sendUserEvent(activity!!, event.toInt(), extra, playtimeParams)
        } catch (exception: Exception) {
            callback(Result.failure(exception))
        }

        callback(Result.success(Unit))
    }

    override fun getStatus(callback: (Result<PlaytimeStatus>) -> Unit) {
        callback(Result.success(mapStatus(
            io.adjoe.sdk.Playtime.getStatus()
        )))
    }

    private fun mapStatus(playtimeStatus: io.adjoe.sdk.PlaytimeStatus): PlaytimeStatus {
        val details = PlaytimeStatusDetails(
            playtimeStatus.details.isFraud,
            playtimeStatus.details.campaignsAvailable,
            mapCampaignsState(playtimeStatus.details.campaignsState)
        )
        return PlaytimeStatus(playtimeStatus.isInitialized, details)
    }

    private fun mapCampaignsState(playtimeCampaignsState: List<io.adjoe.sdk.PlaytimeCampaignsState>): List<String> {
        val states: MutableList<String> = mutableListOf()

        playtimeCampaignsState.forEach {
            val state = when(it) {
                io.adjoe.sdk.PlaytimeCampaignsState.BLOCKED -> "BLOCKED"
                io.adjoe.sdk.PlaytimeCampaignsState.VPN_DETECTED -> "VPN_DETECTED"
                io.adjoe.sdk.PlaytimeCampaignsState.GEO_MISMATCH -> "GEO_MISMATCH"
                else -> "READY"
            }

            states.add(state)
        }

        return states
    }
}
