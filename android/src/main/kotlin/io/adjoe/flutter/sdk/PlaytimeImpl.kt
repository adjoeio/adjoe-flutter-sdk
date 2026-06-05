package io.adjoe.flutter.sdk

import android.os.Build
import android.app.Activity
import io.adjoe.sdk.PlaytimeInitialisationListener
import io.adjoe.sdk.PlaytimeOptionsListener
import android.text.TextUtils
import java.text.DateFormat
import java.text.ParseException
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.text.SimpleDateFormat

internal object OptionsUtil {
    fun mapOptions(options: PlaytimeOptions): io.adjoe.sdk.PlaytimeOptions {
        val playtimeOptions = io.adjoe.sdk.PlaytimeOptions()
        playtimeOptions.setUserId(options.userId)

        options.sdkHash?.let {
            playtimeOptions.setSDKHash(it)
        }

        playtimeOptions.setParams(mapParams(options.params))
        val extensions = mapExtensions(options.extensions)

        extensions?.let {
            playtimeOptions.setExtensions(it)
        }

        playtimeOptions.setUserProfile(mapUserProfile(options.userProfile))
        playtimeOptions.w("flutter")
        return playtimeOptions
    }

    fun mapUserProfile(userProfile: PlaytimeUserProfile?): io.adjoe.sdk.PlaytimeUserProfile? {
        userProfile?.let {
            val genderFlutter = userProfile.gender
            val gender = if (genderFlutter == "male")
                io.adjoe.sdk.PlaytimeGender.MALE
            else if (genderFlutter == "female")
                io.adjoe.sdk.PlaytimeGender.FEMALE
            else
                io.adjoe.sdk.PlaytimeGender.UNKNOWN

            var birthday: Date? = null

            if (!TextUtils.isEmpty(userProfile.birthday)) {
                try {
                    birthday = DateFormat.getDateInstance().parse(userProfile.birthday)
                } catch (ignore: ParseException) { }

                if (birthday == null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            birthday = Date.from(OffsetDateTime.parse(userProfile.birthday).toInstant())
                        } catch (e: DateTimeParseException) { }
                    } else {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
                        sdf.timeZone = TimeZone.getTimeZone("UTC")

                        try {
                            birthday = sdf.parse(userProfile.birthday)
                        } catch (e: Exception) { }
                    }
                }
            }

            return io.adjoe.sdk.PlaytimeUserProfile(gender, birthday)
        }

        return null
    }

    fun mapExtensions(extensions: PlaytimeExtensions?): io.adjoe.sdk.PlaytimeExtensions? {
        extensions?.let {
            return io.adjoe.sdk.PlaytimeExtensions.Builder()
                .setSubId1(extensions.subId1)
                .setSubId2(extensions.subId2)
                .setSubId3(extensions.subId3)
                .setSubId4(extensions.subId4)
                .setSubId5(extensions.subId5)
                .build()
        }

        return null
    }

    fun mapParams(params: PlaytimeParams?): io.adjoe.sdk.PlaytimeParams? {
        params?.let {
            return io.adjoe.sdk.PlaytimeParams.Builder()
                .setPlacement(params.placement)
                .setUaChannel(params.uaChannel)
                .setUaNetwork(params.uaNetwork)
                .setUaSubPublisherCleartext(params.uaSubPublisherCleartext)
                .setUaSubPublisherEncrypted(params.uaSubPublisherEncrypted)
                .setPromotionTag(params.promotionTag)
                .build()
        }

        return null
    }
}

internal object PermissionsUtil {
    fun mapPlaytimePermissionsResponse(
        playtimePermissionsResponse: io.adjoe.sdk.studio.PlaytimePermissionsResponse
    ): PlaytimePermissionsResponse {
        return PlaytimePermissionsResponse(
            permissions = mapPlaytimePermissions(playtimePermissionsResponse.permissions)
        )
    }

    private fun mapPlaytimePermissions(
        playtimePermissions: io.adjoe.sdk.studio.PlaytimePermissions
    ): PlaytimePermissions {
        return PlaytimePermissions(
            isTOSAccepted = playtimePermissions.isTOSAccepted,
            isUsagePermissionAccepted = playtimePermissions.isUsagePermissionAccepted
        )
    }
}

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
        val currentActivity = activity ?: run {
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
            io.adjoe.sdk.Playtime.init(currentActivity, apiKey, listener)
        } else {
            io.adjoe.sdk.Playtime.init(currentActivity, apiKey, OptionsUtil.mapOptions(options), listener)
        }
    }

    override fun showCatalog(
        params: PlaytimeParams?,
        callback: (Result<Unit>) -> Unit
    ) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        if (params == null) {
            io.adjoe.sdk.Playtime.showCatalog(currentActivity, io.adjoe.sdk.PlaytimeOptions())
        } else {
            io.adjoe.sdk.Playtime.showCatalog(
                currentActivity,
                io.adjoe.sdk.PlaytimeOptions().setParams(OptionsUtil.mapParams(params))
            )
        }

        callback(Result.success(Unit))
    }

    override fun showCatalogWithOptions(
        options: PlaytimeOptions,
        callback: (Result<Unit>) -> Unit
    ) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.Playtime.showCatalog(currentActivity, OptionsUtil.mapOptions(options))
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
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        val playtimeParams = OptionsUtil.mapParams(params)

        playtimeParams?.let {
            io.adjoe.sdk.Playtime.setUAParams(currentActivity, playtimeParams)
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
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        callback(Result.success(io.adjoe.sdk.Playtime.hasAcceptedTOS(currentActivity)))
    }

    override fun hasAcceptedUsagePermission(callback: (Result<Boolean>) -> Unit) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        callback(
            Result.success(
                io.adjoe.sdk.Playtime.hasAcceptedUsagePermission(currentActivity)
            )
        )
    }

    override fun getUserId(callback: (Result<String?>) -> Unit) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        callback(
            Result.success(
                io.adjoe.sdk.Playtime.getUserId(currentActivity)
            )
        )
    }

    override fun sendEvent(
        event: Long,
        extra: String,
        params: PlaytimeParams?,
        callback: (Result<Unit>) -> Unit
    ) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        val playtimeParams = OptionsUtil.mapParams(params)

        try {
            io.adjoe.sdk.Playtime.sendUserEvent(currentActivity, event.toInt(), extra, playtimeParams)
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

    override fun teardown(callback: (Result<Unit>) -> Unit) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.Playtime.teardown(currentActivity)
        callback(Result.success(Unit))
    }

    private fun mapStatus(playtimeStatus: io.adjoe.sdk.PlaytimeStatus): PlaytimeStatus {
        val details = PlaytimeStatusDetails(
            playtimeStatus.details.isFraud,
            playtimeStatus.details.campaignsAvailable,
            mapCampaignsState(playtimeStatus.details.campaignsState),
            playtimeStatus.details.testGroup?.toLong()
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
