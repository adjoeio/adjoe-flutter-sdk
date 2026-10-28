package io.adjoe.flutter.sdk

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import io.adjoe.sdk.PlaytimeCatalogLaunchListener
import io.adjoe.sdk.PlaytimeException
import io.adjoe.sdk.PlaytimeImpressionEventListener
import io.adjoe.sdk.PlaytimeInitListener
import io.adjoe.sdk.PlaytimeTeardownListener
import io.adjoe.sdk.AppCategory as SdkAppCategory
import io.adjoe.sdk.CampaignOptions as SdkCampaignOptions
import io.adjoe.sdk.CatalogPlacement as SdkCatalogPlacement
import io.adjoe.sdk.DisplayFormat as SdkDisplayFormat
import io.adjoe.sdk.StudioPlacement as SdkStudioPlacement
import io.adjoe.sdk.CatalogOptions as SdkCatalogOptions
import io.adjoe.sdk.InitOptions as SdkInitOptions
import io.adjoe.sdk.InstalledCampaignOptions as SdkInstalledCampaignOptions
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Bridges the native SDK's [io.adjoe.sdk.PlaytimeEventListener] stream onto the Pigeon
 * `PlaytimeEvents` event channel.
 *
 * The native SDK delivers events through a single registered listener, while Dart consumes
 * them through an event-channel [PigeonEventSink] that only exists once Dart has subscribed.
 * This object owns both ends so the two lifecycles (host `addEventListener` vs. channel
 * `onListen`) can be wired together regardless of their order.
 */
internal object PlaytimeEventForwarder : StreamEventsStreamHandler() {
    @Volatile
    private var sink: PigeonEventSink<PlaytimeEvent>? = null

    // The native SDK invokes didReceive on a background thread, but a Flutter
    // EventChannel.EventSink must only be touched on the main thread, so every delivery is
    // posted to the main looper.
    private val mainHandler = Handler(Looper.getMainLooper())

    private val nativeListener = object : io.adjoe.sdk.PlaytimeEventListener {
        override fun didReceive(event: io.adjoe.sdk.PlaytimeEvent) {
            val mapped = PlaytimeEvent(
                name = event.name,
                occurredAt = OptionsUtil.formatIso8601(event.occurredAt),
            )
            mainHandler.post { sink?.success(mapped) }
        }
    }

    override fun onListen(p0: Any?, sink: PigeonEventSink<PlaytimeEvent>) {
        this.sink = sink
    }

    override fun onCancel(p0: Any?) {
        this.sink = null
    }

    fun startListening() {
        io.adjoe.sdk.Playtime.addEventListener(nativeListener)
    }

    fun stopListening() {
        io.adjoe.sdk.Playtime.removeEventListener()
    }
}

internal object OptionsUtil {

    fun studioPlacementFrom(sealed: StudioPlacement?): SdkStudioPlacement? {
        val value = sealed?.value ?: return null
        if (sealed.isCustom == true) return SdkStudioPlacement.Custom(value)
        return when (value) {
            "navbar" -> SdkStudioPlacement.Navbar
            "icon" -> SdkStudioPlacement.Icon
            "earn_tab" -> SdkStudioPlacement.EarnTab
            "game_tab" -> SdkStudioPlacement.GameTab
            "home_page" -> SdkStudioPlacement.HomePage
            "notification_inbox" -> SdkStudioPlacement.NotificationInbox
            "notification" -> SdkStudioPlacement.Notification
            "mini_game" -> SdkStudioPlacement.MiniGame
            "order_page" -> SdkStudioPlacement.OrderPage
            "redeem_section" -> SdkStudioPlacement.RedeemSection
            "scan_receipt" -> SdkStudioPlacement.ScanReceipt
            "step_converter" -> SdkStudioPlacement.StepConverter
            else -> SdkStudioPlacement.Custom(value)
        }
    }

    fun catalogPlacementFrom(sealed: CatalogPlacement?): SdkCatalogPlacement? {
        val value = sealed?.value ?: return null
        if (sealed.isCustom == true) return SdkCatalogPlacement.Custom(value)
        return when (value) {
            "navbar" -> SdkCatalogPlacement.Navbar
            "icon" -> SdkCatalogPlacement.Icon
            "earn_tab" -> SdkCatalogPlacement.EarnTab
            "game_tab" -> SdkCatalogPlacement.GameTab
            "home_page" -> SdkCatalogPlacement.HomePage
            "notification_inbox" -> SdkCatalogPlacement.NotificationInbox
            "notification" -> SdkCatalogPlacement.Notification
            "mini_game" -> SdkCatalogPlacement.MiniGame
            "order_page" -> SdkCatalogPlacement.OrderPage
            "redeem_section" -> SdkCatalogPlacement.RedeemSection
            "scan_receipt" -> SdkCatalogPlacement.ScanReceipt
            "step_converter" -> SdkCatalogPlacement.StepConverter
            else -> SdkCatalogPlacement.Custom(value)
        }
    }

    fun displayFormatFrom(sealed: DisplayFormat?): SdkDisplayFormat? {
        val value = sealed?.value ?: return null
        if (sealed.isCustom == true) return SdkDisplayFormat.Custom(value)
        return when (value) {
            "icon" -> SdkDisplayFormat.Icon
            "notification" -> SdkDisplayFormat.Notification
            "notification_inbox" -> SdkDisplayFormat.NotificationInbox
            "carousel" -> SdkDisplayFormat.Carousel
            "custom_catalog" -> SdkDisplayFormat.CustomCatalog
            "banner" -> SdkDisplayFormat.Banner
            "popup" -> SdkDisplayFormat.Popup
            "interstitial" -> SdkDisplayFormat.Interstitial
            else -> SdkDisplayFormat.Custom(value)
        }
    }

    fun appCategoryFrom(sealed: AppCategory?): SdkAppCategory {
        return when (sealed?.value) {
            "gaming" -> SdkAppCategory.GAMING
            "non-gaming" -> SdkAppCategory.NON_GAMING
            "both" -> SdkAppCategory.BOTH
            else -> SdkAppCategory.GAMING
        }
    }

    fun formatIso8601(date: Date): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return date.toInstant().toString()
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    fun parseBirthday(value: String?): Date? {
        if (value == null || TextUtils.isEmpty(value)) return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                return Date.from(Instant.parse(value))
            } catch (e: Exception) {
                // Not an instant; try the remaining representations below.
            }
            try {
                return Date.from(OffsetDateTime.parse(value).toInstant())
            } catch (e: Exception) {
                // Not an offset date time either.
            }
            try {
                return Date.from(LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant())
            } catch (e: Exception) {
                return null
            }
        }

        for (pattern in arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd",
        )) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                sdf.isLenient = false
                return sdf.parse(value)
            } catch (e: Exception) {
                // Try the next pattern.
            }
        }
        return null
    }

    fun mapInitOptions(options: InitOptions): SdkInitOptions {
        return SdkInitOptions(
            sdkHash = options.sdkHash,
            userId = options.userId,
            userProfile = mapUserProfile(options.userProfile),
            extensions = mapExtensions(options.extensions),
            language = options.language,
            uaNetwork = options.uaNetwork,
            uaChannel = options.uaChannel,
            uaCampaignName = options.uaCampaignName,
            country = options.country,
        )
    }

    fun mapUserProfile(userProfile: PlaytimeUserProfile?): io.adjoe.sdk.PlaytimeUserProfile? {
        if (userProfile == null) return null

        val gender = when (userProfile.gender) {
            PlaytimeGender.MALE -> io.adjoe.sdk.PlaytimeGender.MALE
            PlaytimeGender.FEMALE -> io.adjoe.sdk.PlaytimeGender.FEMALE
            else -> io.adjoe.sdk.PlaytimeGender.UNKNOWN
        }

        return io.adjoe.sdk.PlaytimeUserProfile(gender, parseBirthday(userProfile.birthday))
    }

    fun mapExtensions(extensions: PlaytimeExtensions?): io.adjoe.sdk.PlaytimeExtensions? {
        if (extensions == null) return null

        return io.adjoe.sdk.PlaytimeExtensions.Builder()
            .setSubId1(extensions.subId1)
            .setSubId2(extensions.subId2)
            .setSubId3(extensions.subId3)
            .setSubId4(extensions.subId4)
            .setSubId5(extensions.subId5)
            .build()
    }

    fun mapMetadata(metadata: PlaytimeMetadata?): io.adjoe.sdk.PlaytimeMetadata? {
        if (metadata == null) return null
        return io.adjoe.sdk.PlaytimeMetadata(
            metadata1 = metadata.metadata1,
            metadata2 = metadata.metadata2,
        )
    }

    fun mapCampaignOptions(options: CampaignOptions?): SdkCampaignOptions {
        return SdkCampaignOptions(
            placement = studioPlacementFrom(options?.placement),
            category = appCategoryFrom(options?.category),
            displayFormat = displayFormatFrom(options?.displayFormat),
            promotionTag = options?.promotionTag,
            metadata = mapMetadata(options?.metadata),
        )
    }

    fun mapCatalogOptions(options: CatalogOptions?): SdkCatalogOptions {
        return SdkCatalogOptions(
            placement = catalogPlacementFrom(options?.placement),
            category = appCategoryFrom(options?.category),
            promotionTag = options?.promotionTag,
            metadata = mapMetadata(options?.metadata),
        )
    }

    fun mapInstalledCampaignOptions(
        options: InstalledCampaignOptions?
    ): SdkInstalledCampaignOptions {
        return SdkInstalledCampaignOptions(category = appCategoryFrom(options?.category))
    }
}

class PlaytimeImpl() : Playtime {
    private var activity: Activity? = null

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    override fun initPlaytime(
        options: InitOptions,
        callback: (Result<Unit>) -> Unit
    ) {
        val listener = object : PlaytimeInitListener {
            override fun onSuccess() {
                callback(Result.success(Unit))
            }

            override fun onError(error: PlaytimeException) {
                callback(
                    Result.failure(
                        ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))
                    )
                )
            }
        }

        io.adjoe.sdk.Playtime.initPlaytime(OptionsUtil.mapInitOptions(options), listener)
    }

    override fun showCatalog(
        catalogOptions: CatalogOptions?,
        callback: (Result<Unit>) -> Unit
    ) {
        val currentActivity = activity ?: run {
            return callback(
                Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.getActivityIsNullError()))
            )
        }

        val listener = object : PlaytimeCatalogLaunchListener {
            override fun onCatalogLaunched() {
                callback(Result.success(Unit))
            }

            override fun onError(error: PlaytimeException) {
                callback(Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))))
            }
        }

        io.adjoe.sdk.Playtime.showCatalog(
            currentActivity,
            OptionsUtil.mapCatalogOptions(catalogOptions),
            listener
        )
    }

    override fun getStatus(callback: (Result<PlaytimeStatus>) -> Unit) {
        callback(Result.success(mapStatus(io.adjoe.sdk.Playtime.getStatus())))
    }

    override fun teardown(callback: (Result<Unit>) -> Unit) {
        val listener = object : PlaytimeTeardownListener {
            override fun onSuccess() {
                callback(Result.success(Unit))
            }

            override fun onError(error: PlaytimeException) {
                callback(Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))))
            }
        }

        io.adjoe.sdk.Playtime.teardown(listener)
    }

    override fun addEventListener(
        callback: (Result<Unit>) -> Unit
    ) {
        try {
            PlaytimeEventForwarder.startListening()
            callback(Result.success(Unit))
        } catch (e: Exception) {
            callback(Result.failure(ErrorsUtil.toFlutterError(e)))
        }
    }

    override fun removeEventListener(callback: (Result<Unit>) -> Unit) {
        try {
            PlaytimeEventForwarder.stopListening()
            callback(Result.success(Unit))
        } catch (e: Exception) {
            callback(Result.failure(ErrorsUtil.toFlutterError(e)))
        }
    }

    override fun executeImpressionEvent(
        catalogPlacement: CatalogPlacement,
        callback: (Result<Unit>) -> Unit
    ) {
        val sdkPlacement = OptionsUtil.catalogPlacementFrom(catalogPlacement)
        if (sdkPlacement == null) {
            return callback(
                Result.failure(
                    ErrorsUtil.toFlutterError(
                        ErrorsUtil.getFunctionError("executeImpressionEvent", "catalogPlacement is null")
                    )
                )
            )
        }

        val listener = object : PlaytimeImpressionEventListener {
            override fun onSuccess() {
                callback(Result.success(Unit))
            }

            override fun onError(error: PlaytimeException) {
                callback(Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))))
            }
        }

        io.adjoe.sdk.Playtime.executeImpressionEvent(sdkPlacement, listener)
    }

    private fun mapStatus(playtimeStatus: io.adjoe.sdk.PlaytimeStatus): PlaytimeStatus {
        val details = PlaytimeStatusDetails(
            playtimeStatus.details.isFraud,
            playtimeStatus.details.campaignsAvailable,
            mapCampaignsState(playtimeStatus.details.campaignsState),
            playtimeStatus.details.testGroup?.toLong()
        )

        return PlaytimeStatus(
            version = playtimeStatus.version,
            isInitialized = playtimeStatus.isInitialized,
            userId = playtimeStatus.userId,
            isTosAccepted = playtimeStatus.isTosAccepted,
            details = details,
            isUsagePermissionAccepted = playtimeStatus.isUsagePermissionAccepted
        )
    }

    private fun mapCampaignsState(
        playtimeCampaignsState: List<io.adjoe.sdk.PlaytimeCampaignsState>
    ): List<PlaytimeCampaignsState> {
        return playtimeCampaignsState.map {
            when (it) {
                io.adjoe.sdk.PlaytimeCampaignsState.BLOCKED -> PlaytimeCampaignsState.BLOCKED
                io.adjoe.sdk.PlaytimeCampaignsState.VPN_DETECTED -> PlaytimeCampaignsState.VPN_DETECTED
                io.adjoe.sdk.PlaytimeCampaignsState.GEO_MISMATCH -> PlaytimeCampaignsState.GEO_MISMATCH
                io.adjoe.sdk.PlaytimeCampaignsState.NOT_INITIALIZED -> PlaytimeCampaignsState.NOT_INITIALIZED
                else -> PlaytimeCampaignsState.READY
            }
        }
    }
}
