package io.adjoe.flutter.sdk

import android.app.Activity
import io.adjoe.sdk.PlaytimeException
import io.adjoe.sdk.connect.RewardsConnectRegistrationListener
import io.adjoe.sdk.connect.RewardsConnectResetListener
import io.adjoe.sdk.studio.PlaytimeCampaignsListener
import io.adjoe.sdk.studio.PlaytimeDeeplinkListener
import io.adjoe.sdk.studio.PlaytimeExecuteImpressionListener
import io.adjoe.sdk.studio.PlaytimeOpenInstalledCampaignListener
import io.adjoe.sdk.studio.PlaytimeOpenStoreListener
import io.adjoe.sdk.studio.PlaytimePermissionsListener
import io.adjoe.sdk.studio.PlaytimeTosListener

internal object CampaignsUtil {
    fun mapPlaytimeCampaignsResponse(
        playtimeCampaignsResponse: io.adjoe.sdk.studio.PlaytimeCampaignsResponse
    ): PlaytimeCampaignsResponse {
        return PlaytimeCampaignsResponse(
            campaigns = mapPlaytimeCampaigns(playtimeCampaignsResponse.campaigns)
        )
    }

    private fun mapPlaytimeCampaigns(
        playtimeCampaigns: List<io.adjoe.sdk.studio.PlaytimeCampaign>
    ): List<PlaytimeCampaign> {
        val campaigns: MutableList<PlaytimeCampaign> = mutableListOf()

        playtimeCampaigns.forEach {
            campaigns.add(mapPlaytimeCampaign(it))
        }

        return campaigns.toList()
    }

    private fun mapPlaytimeCampaign(
        playtimeCampaign: io.adjoe.sdk.studio.PlaytimeCampaign
    ): PlaytimeCampaign {
        return PlaytimeCampaign(
            campaignUUID = playtimeCampaign.campaignUUID,
            appName = playtimeCampaign.appName,
            appDescription = playtimeCampaign.appDescription,
            appID = playtimeCampaign.appID,
            appBundleID = null,
            token = playtimeCampaign.token,
            installedAt = playtimeCampaign.installedAt,
            uninstalledAt = playtimeCampaign.uninstalledAt,
            rewardingExpiresAfter = playtimeCampaign.rewardingExpiresAfter?.toLong(),
            rewardingExpiresAt = playtimeCampaign.rewardingExpiresAt,
            campaignExpiresAt = playtimeCampaign.campaignExpiresAt,
            appCategory = playtimeCampaign.appCategory,
            campaignType = playtimeCampaign.campaignType,
            featuredPosition = playtimeCampaign.featuredPosition?.toLong(),
            score = playtimeCampaign.score?.toDouble(),
            iconImage = playtimeCampaign.iconImage,
            image = mapPlaytimeMedia(playtimeCampaign.image),
            video = mapPlaytimeMedia(playtimeCampaign.video),
            promotion = mapPlaytimePromotion(playtimeCampaign.promotion),
            eventConfig = mapPlaytimeEventConfig(playtimeCampaign.eventConfig),
            isCompleted = playtimeCampaign.isCompleted,
            status = mapPlaytimeCampaignStatus(playtimeCampaign.status),
            isCpa = playtimeCampaign.isCpa,
            cpa = playtimeCampaign.cpa.toDouble()
        )
    }

    private fun mapPlaytimeEventConfig(
        playtimeEventConfig: io.adjoe.sdk.studio.PlaytimeEventConfig?
    ): PlaytimeEventConfig? {
        playtimeEventConfig?.let {
            return PlaytimeEventConfig(
                sequentialActions = mapPlaytimeRewardActions(playtimeEventConfig.sequentialActions),
                bonusActions = mapPlaytimeRewardActions(playtimeEventConfig.bonusActions),
                timeBasedActions = mapPlaytimeRewardActions(playtimeEventConfig.timeBasedActions),
                cpaActions = mapPlaytimeRewardActions(playtimeEventConfig.cpaActions),
                totalCoinsCollected = playtimeEventConfig.totalCoinsCollected?.toLong(),
                totalCoinsPossible = playtimeEventConfig.totalCoinsPossible?.toLong(),
                totalOriginalCoinsPossible = playtimeEventConfig.totalOriginalCoinsPossible?.toLong(),
                totalSequentialCoins = playtimeEventConfig.totalSequentialCoins?.toLong(),
                totalOriginalSequentialCoins = playtimeEventConfig.totalOriginalSequentialCoins?.toLong(),
                totalBonusCoins = playtimeEventConfig.totalBonusCoins?.toLong(),
                totalOriginalBonusCoins = playtimeEventConfig.totalOriginalBonusCoins?.toLong(),
                secondsToNextLevel = playtimeEventConfig.secondsToNextLevel?.toLong(),
                cashbackReward = mapPlaytimeCashbackConfig(playtimeEventConfig.cashbackReward),
                multipliersActions = mapPlaytimeMultipliersActions(playtimeEventConfig.multipliersActions),
            )
        }

        return null
    }

    private fun mapPlaytimeCampaignStatus(
        status: io.adjoe.sdk.studio.PlaytimeCampaignStatus
    ): PlaytimeCampaignStatus {
        return when (status) {
            io.adjoe.sdk.studio.PlaytimeCampaignStatus.PENDING -> PlaytimeCampaignStatus.PENDING
            io.adjoe.sdk.studio.PlaytimeCampaignStatus.INSTALLED -> PlaytimeCampaignStatus.INSTALLED
            io.adjoe.sdk.studio.PlaytimeCampaignStatus.FAILED -> PlaytimeCampaignStatus.FAILED
            else -> PlaytimeCampaignStatus.AVAILABLE
        }
    }

    private fun mapPlaytimeMultipliersActions(
        playtimeMultipliersActions: List<io.adjoe.sdk.studio.PlaytimeRewardActionMultiplier>
    ): List<PlaytimeRewardActionMultiplier> {
        val multipliersActions: MutableList<PlaytimeRewardActionMultiplier> = mutableListOf()

        playtimeMultipliersActions.forEach {
            multipliersActions.add(PlaytimeRewardActionMultiplier(
                eventName = it.eventName,
                eventDescription = it.eventDescription,
                multiplierFactorPercentage = it.multiplierFactorPercentage?.toLong(),
                multiplierLevels = it.multiplierLevels?.toLong(),
                status = it.status,
                usedLevels = it.usedLevels?.toLong()
            ))
        }

        return multipliersActions.toList()
    }

    private fun mapPlaytimeCashbackConfig(
        playtimeCashbackConfig: io.adjoe.sdk.studio.PlaytimeCashbackConfig?
    ): PlaytimeCashbackConfig? {
        playtimeCashbackConfig?.let {
            return PlaytimeCashbackConfig(
                exchangeRate = it.exchangeRate?.toDouble(),
                cashbackDescription = it.cashbackDescription,
                maxLimitPerCampaignUSD = it.maxLimitPerCampaignUSD?.toDouble(),
                maxLimitPerCampaignCoins = it.maxLimitPerCampaignCoins?.toDouble(),
                completedRewards = mapPlaytimeCashbackRewards(it.completedRewards),
                pendingRewards =  mapPlaytimeCashbackRewards(it.pendingRewards),
            )
        }

        return null
    }

    private fun mapPlaytimeCashbackRewards(
        cashbackRewards: io.adjoe.sdk.studio.PlaytimeCashbackReward?
    ): PlaytimeCashbackReward? {
        cashbackRewards?.let {
            return PlaytimeCashbackReward(
                totalCoins = it.totalCoins?.toLong(),
                events = mapPlaytimeCashbackRewardEvents(it.events) ?: listOf()
            )
        }

        return null
    }

    private fun mapPlaytimeCashbackRewardEvents(
        playtimeEvents: List<io.adjoe.sdk.studio.PlaytimeCashbackRewardEvent>
    ): List<PlaytimeCashbackRewardEvent?>? {
        val events: MutableList<PlaytimeCashbackRewardEvent> = mutableListOf()

        playtimeEvents.forEach {
            events.add(PlaytimeCashbackRewardEvent(
                coins = it.coins?.toLong(),
                processAt = it.processAt,
                receivedAt = it.receivedAt
            ))
        }

        return events.toList()
    }

    private fun mapPlaytimeRewardActions(
        playtimeRewardActions: List<io.adjoe.sdk.studio.PlaytimeRewardAction>
    ): List<PlaytimeRewardAction?> {
        val rewardActions: MutableList<PlaytimeRewardAction> = mutableListOf()
        playtimeRewardActions.forEach {
            rewardActions.add(PlaytimeRewardAction(
                name = it.name,
                taskDescription = it.taskDescription,
                taskType = it.taskType,
                playDuration = it.playDuration?.toLong(),
                level = it.level?.toLong(),
                amount = it.amount.toLong(),
                rewardedAt = it.rewardedAt,
                rewardsCount = it.rewardsCount?.toLong(),
                completedRewards = it.completedRewards?.toLong(),
                timedCoinsDuration = it.timedCoinsDuration?.toLong(),
                timedCoins = it.timedCoins?.toLong(),
                originalCoins = it.originalCoins?.toLong(),
                isTimed = it.isTimed,
                isRewardedForPromotion = it.isRewardedForPromotion,
                boosterExpiresAt = it.boosterExpiresAt
            ))
        }
        return rewardActions.toList()
    }

    private fun mapPlaytimePromotion(
        playtimePromotion: io.adjoe.sdk.studio.PlaytimePromotion?
    ): PlaytimePromotion? {
        playtimePromotion?.let {
            return PlaytimePromotion(
                name = it.name,
                promotionDescription = it.promotionDescription,
                boostFactor = it.boostFactor?.toDouble(),
                startTime = it.startTime,
                endTime = it.endTime,
                targetingType = it.targetingType,
            )
        }

        return null
    }

    private fun mapPlaytimeMedia(
        playtimeMedia: io.adjoe.sdk.studio.PlaytimeMedia?
    ): PlaytimeMedia? {
        playtimeMedia?.let {
            return PlaytimeMedia(
                portrait = it.portrait,
                landscape = it.landscape
            )
        }

        return null
    }
}

class PlaytimeStudioImpl() : PlaytimeStudio {
    private var activity: Activity? = null
    private val playtimeCampaignsCache = HashMap<String, io.adjoe.sdk.studio.PlaytimeCampaign>()

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    override fun updateTosConsent(
        action: TosConsentAction,
        callback: (Result<Unit>) -> Unit
    ) {
        val sdkAction = when (action) {
            TosConsentAction.ACCEPT -> io.adjoe.sdk.studio.TosConsentAction.ACCEPT
            TosConsentAction.WITHDRAW -> io.adjoe.sdk.studio.TosConsentAction.WITHDRAW
        }

        io.adjoe.sdk.studio.PlaytimeStudio.updateTosConsent(
            sdkAction,
            object : PlaytimeTosListener {
                override fun onUpdated() {
                    callback(Result.success(Unit))
                }

                override fun onError(error: PlaytimeException) {
                    callback(Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))))
                }
            }
        )
    }

    override fun getCampaigns(
        campaignOptions: CampaignOptions?,
        callback: (Result<PlaytimeCampaignsResponse>) -> Unit
    ) {
        io.adjoe.sdk.studio.PlaytimeStudio.getCampaigns(
            OptionsUtil.mapCampaignOptions(campaignOptions),
            createCampaignsListener(callback)
        )
    }

    override fun getOfferDetails(
        tokens: List<String?>,
        campaignOptions: CampaignOptions?,
        callback: (Result<PlaytimeCampaignsResponse>) -> Unit
    ) {
        io.adjoe.sdk.studio.PlaytimeStudio.getOfferDetails(
            tokens.filterNotNull(),
            OptionsUtil.mapCampaignOptions(campaignOptions),
            createCampaignsListener(callback)
        )
    }

    override fun getInstalledCampaigns(
        options: InstalledCampaignOptions?,
        callback: (Result<PlaytimeCampaignsResponse>) -> Unit
    ) {
        io.adjoe.sdk.studio.PlaytimeStudio.getInstalledCampaigns(
            OptionsUtil.mapInstalledCampaignOptions(options),
            createCampaignsListener(callback)
        )
    }

    override fun openInStore(
        campaign: PlaytimeCampaign,
        useInlineInstall: Boolean,
        callback: (Result<Unit>) -> Unit
    ) {
        val playtimeCampaign = cachedCampaign(campaign, "openInStore", callback) ?: return

        io.adjoe.sdk.studio.PlaytimeStudio.openInStore(
            playtimeCampaign,
            useInlineInstall,
            object : PlaytimeOpenStoreListener {
                override fun onAlreadyClicking() {
                    callback(
                        Result.failure(
                            ErrorsUtil.toFlutterError(
                                ErrorsUtil.getFunctionError(
                                    "openInStore",
                                    "Request is already in progress"
                                )
                            )
                        )
                    )
                }

                override fun onError(error: PlaytimeException) {
                    callback(Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))))
                }

                override fun onFinished() {
                    callback(Result.success(Unit))
                }
            }
        )
    }

    override fun openInstalledCampaign(
        campaign: PlaytimeCampaign,
        callback: (Result<Unit>) -> Unit
    ) {
        val playtimeCampaign =
            cachedCampaign(campaign, "openInstalledCampaign", callback) ?: return

        io.adjoe.sdk.studio.PlaytimeStudio.openInstalledCampaign(
            playtimeCampaign,
            object : PlaytimeOpenInstalledCampaignListener {
                override fun onOpened() {
                    callback(Result.success(Unit))
                }

                override fun onError(error: PlaytimeException) {
                    callback(Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))))
                }
            }
        )
    }

    override fun openChatbot(
        campaign: PlaytimeCampaign?,
        callback: (Result<Unit>) -> Unit
    ) {
        val currentActivity = activity ?: run {
            return callback(
                Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.getActivityIsNullError()))
            )
        }

        val playtimeCampaign = getCampaignFromCache(campaign)

        io.adjoe.sdk.studio.PlaytimeStudio.openChatbot(
            currentActivity,
            playtimeCampaign,
            createDeeplinkListener(callback)
        )
    }

    override fun resetRewardsConnect(callback: (Result<Unit>) -> Unit) {
        io.adjoe.sdk.studio.PlaytimeStudio.resetRewardsConnect(
            object : RewardsConnectResetListener {
                override fun onSuccess() {
                    callback(Result.success(Unit))
                }

                override fun onFailure(error: PlaytimeException) {
                    callback(Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))))
                }
            }
        )
    }

    override fun registerRewardsConnect(callback: (Result<Unit>) -> Unit) {
        val currentActivity = activity ?: run {
            return callback(
                Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.getActivityIsNullError()))
            )
        }

        io.adjoe.sdk.studio.PlaytimeStudio.registerRewardsConnect(
            currentActivity,
            object : RewardsConnectRegistrationListener {
                override fun onSuccess() {
                    callback(Result.success(Unit))
                }

                override fun onFailure(error: PlaytimeException) {
                    callback(Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))))
                }
            }
        )
    }

    override fun executeCampaignImpression(
        campaign: PlaytimeCampaign,
        impressionType: PlaytimeImpressionType?,
        callback: (Result<Unit>) -> Unit
    ) {
        val playtimeCampaign =
            cachedCampaign(campaign, "executeCampaignImpression", callback) ?: return

        val sdkImpressionType = when (impressionType) {
            PlaytimeImpressionType.ENGAGED -> io.adjoe.sdk.studio.PlaytimeImpressionType.ENGAGED
            else -> io.adjoe.sdk.studio.PlaytimeImpressionType.DEFAULT
        }

        io.adjoe.sdk.studio.PlaytimeStudio.executeCampaignImpression(
            playtimeCampaign,
            sdkImpressionType,
            createImpressionListener("executeCampaignImpression", callback)
        )
    }

    override fun executeOfferImpression(
        token: String,
        appID: String,
        callback: (Result<Unit>) -> Unit
    ) {
        io.adjoe.sdk.studio.PlaytimeStudio.executeOfferImpression(
            token,
            appID,
            createImpressionListener("executeOfferImpression", callback)
        )
    }

    override fun showCampaignDetails(
        campaign: PlaytimeCampaign,
        callback: (Result<Unit>) -> Unit
    ) {
        val currentActivity = activity ?: run {
            return callback(
                Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.getActivityIsNullError()))
            )
        }

        val playtimeCampaign =
            cachedCampaign(campaign, "showCampaignDetails", callback) ?: return

        io.adjoe.sdk.studio.PlaytimeStudio.showCampaignDetails(
            currentActivity,
            playtimeCampaign,
            createDeeplinkListener(callback)
        )
    }

    override fun showOfferDetails(
        token: String,
        appID: String,
        callback: (Result<Unit>) -> Unit
    ) {
        val currentActivity = activity ?: run {
            return callback(
                Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.getActivityIsNullError()))
            )
        }

        io.adjoe.sdk.studio.PlaytimeStudio.showOfferDetails(
            currentActivity,
            token,
            appID,
            createDeeplinkListener(callback)
        )
    }

    override fun showInstalledCampaigns(callback: (Result<Unit>) -> Unit) {
        val currentActivity = activity ?: run {
            return callback(
                Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.getActivityIsNullError()))
            )
        }

        io.adjoe.sdk.studio.PlaytimeStudio.showInstalledCampaigns(
            currentActivity,
            createDeeplinkListener(callback)
        )
    }

    override fun showPermissionsPrompt(callback: (Result<Unit>) -> Unit) {
        val currentActivity = activity ?: run {
            return callback(
                Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.getActivityIsNullError()))
            )
        }

        io.adjoe.sdk.studio.PlaytimeStudio.showPermissionsPrompt(
            currentActivity,
            object : PlaytimePermissionsListener {
                override fun onReceived() {
                    callback(Result.success(Unit))
                }

                override fun onError(error: PlaytimeException) {
                    callback(Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))))
                }
            }
        )
    }

    private fun cachedCampaign(
        campaign: PlaytimeCampaign,
        functionName: String,
        callback: (Result<Nothing>) -> Unit
    ): io.adjoe.sdk.studio.PlaytimeCampaign? {
        val playtimeCampaign = getCampaignFromCache(campaign)

        if (playtimeCampaign == null) {
            callback(
                Result.failure(
                    ErrorsUtil.toFlutterError(
                        ErrorsUtil.getFunctionError(functionName, "campaign is null")
                    )
                )
            )
            return null
        }

        return playtimeCampaign
    }

    private fun createCampaignsListener(
        callback: (Result<PlaytimeCampaignsResponse>) -> Unit
    ): PlaytimeCampaignsListener {
        return object : PlaytimeCampaignsListener {
            override fun onReceived(response: io.adjoe.sdk.studio.PlaytimeCampaignsResponse) {
                // Cache the native campaigns so methods taking a campaign can resolve the
                // Pigeon model back to the SDK object.
                response.campaigns.forEach { campaign ->
                    if (campaign?.token?.isNullOrEmpty() == true) {
                        campaign.campaignUUID?.let { playtimeCampaignsCache[it] = campaign }
                    } else {
                        campaign.token?.let { playtimeCampaignsCache[it] = campaign }
                    }
                }

                callback(
                    Result.success(CampaignsUtil.mapPlaytimeCampaignsResponse(response))
                )
            }

            override fun onError(error: PlaytimeException) {
                callback(Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))))
            }
        }
    }

    private fun createDeeplinkListener(
        callback: (Result<Unit>) -> Unit
    ): PlaytimeDeeplinkListener {
        return object : PlaytimeDeeplinkListener {
            override fun onOpened() {
                callback(Result.success(Unit))
            }

            override fun onError(error: PlaytimeException) {
                callback(Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))))
            }
        }
    }

    private fun createImpressionListener(
        functionName: String,
        callback: (Result<Unit>) -> Unit
    ): PlaytimeExecuteImpressionListener {
        return object : PlaytimeExecuteImpressionListener {
            override fun onFinished() {
                callback(Result.success(Unit))
            }

            override fun onAlreadyEngaging() {
                callback(
                    Result.failure(
                        ErrorsUtil.toFlutterError(
                            ErrorsUtil.getFunctionError(
                                functionName,
                                "Request is already in progress"
                            )
                        )
                    )
                )
            }

            override fun onError(error: PlaytimeException) {
                callback(Result.failure(ErrorsUtil.toFlutterError(ErrorsUtil.toPlaytimeException(error))))
            }
        }
    }

    private fun getCampaignFromCache(campaign: PlaytimeCampaign?): io.adjoe.sdk.studio.PlaytimeCampaign? {
        if (campaign?.token?.isNullOrEmpty() == true) {
            return campaign?.campaignUUID?.let { playtimeCampaignsCache[it] }
        }

        return campaign?.token?.let { playtimeCampaignsCache[it] }
    }
}
