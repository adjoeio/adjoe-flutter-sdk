package io.adjoe.flutter.sdk

import android.app.Activity
import io.adjoe.sdk.connect.RewardsConnectRegistrationFailureException
import io.adjoe.sdk.connect.RewardsConnectRegistrationListener
import io.adjoe.sdk.connect.RewardsConnectResetFailureException
import io.adjoe.sdk.connect.RewardsConnectResetListener
import io.adjoe.sdk.studio.PlaytimeCampaignsListener
import io.adjoe.sdk.studio.PlaytimeOpenInstalledCampaignListener
import io.adjoe.sdk.studio.PlaytimeOpenStoreListener
import io.adjoe.sdk.studio.PlaytimePermissionsListener
import io.adjoe.sdk.studio.PlaytimeResponseError
import io.adjoe.sdk.studio.PlaytimeEngagementType
import io.adjoe.sdk.studio.PlaytimeExecuteEngagementListener

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
            appBundleID = null, // null because is used only in iOS
            appStoreID = null, // null because is used only in iOS
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
            status = mapPlaytimeStatus(playtimeCampaign.status),
            isCpa = playtimeCampaign.isCpa,
            cpa = playtimeCampaign.cpa.toDouble()
        )
    }

    private fun mapPlaytimeEventConfig(
        playtimeEventConfig: io.adjoe.sdk.studio.PlaytimeCampaign.EventConfig?
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

     private fun mapPlaytimeStatus(
       status: io.adjoe.sdk.studio.PlaytimeCampaign.PlaytimeCampaignStatus
    ): String {
        return when (status) {
            io.adjoe.sdk.studio.PlaytimeCampaign.PlaytimeCampaignStatus.PENDING -> "PENDING"
            io.adjoe.sdk.studio.PlaytimeCampaign.PlaytimeCampaignStatus.INSTALLED -> "INSTALLED"
            io.adjoe.sdk.studio.PlaytimeCampaign.PlaytimeCampaignStatus.FAILED -> "FAILED"
            else -> "AVAILABLE"
        }
    }

    private fun mapPlaytimeMultipliersActions(
        playtimeMultipliersActions: List<io.adjoe.sdk.studio.PlaytimeCampaign.PlaytimeRewardActionMultiplier>
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
        playtimeCashbackConfig: io.adjoe.sdk.studio.PlaytimeCampaign.PlaytimeCashbackConfig?
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
        cashbackRewards: io.adjoe.sdk.studio.PlaytimeCampaign.PlaytimeCashbackReward?
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
        playtimeEvents: List<io.adjoe.sdk.studio.PlaytimeCampaign.PlaytimeCashbackRewardEvent>
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
        playtimeRewardActions: List<io.adjoe.sdk.studio.PlaytimeCampaign.PlaytimeRewardAction>
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
        playtimePromotion: io.adjoe.sdk.studio.PlaytimeCampaign.PlaytimePromotion?
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
        playtimeMedia: io.adjoe.sdk.studio.PlaytimeCampaign.PlaytimeMedia?
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

    override fun getCampaigns(
        options: PlaytimeOptions,
        callback: (Result<PlaytimeCampaignsResponse>) -> Unit
    ) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

       if (options.tokens != null && options.tokens.isNotEmpty()) {
           val tokens: MutableList<String> = mutableListOf()

           options.tokens.forEach { token ->
               if (token != null) {
                   tokens.add(token)
               }
           }

           io.adjoe.sdk.studio.PlaytimeStudio.getCampaigns(
               currentActivity,
               tokens,
               OptionsUtil.mapOptions(options),
               createCampaignsListener(callback)
           )
       } else {
           io.adjoe.sdk.studio.PlaytimeStudio.getCampaigns(
               currentActivity,
               OptionsUtil.mapOptions(options),
               createCampaignsListener(callback)
           )
       }
    }

    override fun getInstalledCampaigns(
        options: PlaytimeOptions,
        callback: (Result<PlaytimeCampaignsResponse>) -> Unit
    ) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.getInstalledCampaigns(
            currentActivity,
            OptionsUtil.mapOptions(options),
            createCampaignsListener(callback)
        )
    }

    override fun openInStore(
        campaign: PlaytimeCampaign,
        callback: (Result<Unit>) -> Unit
    ) {
        val playtimeCampaign = playtimeCampaignsCache[campaign.campaignUUID]

        if (playtimeCampaign == null) {
            return callback(Result.failure(
                ErrorsUtil.getFunctionError("openInStore", "campaign is null")
            ))
        }

        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.openInStore(
            currentActivity,
            playtimeCampaign,
            object : PlaytimeOpenStoreListener {
                override fun onAlreadyClicking() {
                    callback(Result.failure(
                        ErrorsUtil.getFunctionError("openInStore", "Request is already in progress")
                    ))
                }

                override fun onError(error: PlaytimeResponseError?) {
                    callback(Result.failure(
                        ErrorsUtil.getFunctionError("openInStore", error?.error?.message)
                    ))
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
        val playtimeCampaign = playtimeCampaignsCache[campaign.campaignUUID]

        if (playtimeCampaign == null) {
            return callback(Result.failure(
                ErrorsUtil.getFunctionError("openInStore", "campaign is null")
            ))
        }

        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.openInstalledCampaign(
            currentActivity,
            playtimeCampaign,
            object : PlaytimeOpenInstalledCampaignListener {
                override fun onError(error: PlaytimeResponseError?) {
                    callback(Result.failure(
                        ErrorsUtil.getFunctionError("openInstalledCampaign", error?.error?.message)
                    ))
                }

                override fun onOpened() {
                    return callback(Result.success(Unit))
                }
            }
        )
    }

    override fun getPermissions(callback: (Result<PlaytimePermissionsResponse>) -> Unit) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.getPermissions(
            currentActivity,
            createPermissionsListener("getPermissions", callback)
        )
    }

    override fun showPermissionsPrompt(callback: (Result<PlaytimePermissionsResponse>) -> Unit) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.showPermissionsPrompt(
            currentActivity,
            createPermissionsListener("showPermissionsPrompt", callback)
        )
    }

    override fun registerRewardsConnect(callback: (Result<Unit>) -> Unit) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.registerRewardsConnect(
            currentActivity,
            object : RewardsConnectRegistrationListener {
                override fun onFailure(error: RewardsConnectRegistrationFailureException) {
                    callback(
                        Result.failure(
                            ErrorsUtil.getFunctionError("registerRewardsConnect", error.message)
                        )
                    )
                }

                override fun onSuccess() {
                    callback(Result.success(Unit))
                }
            }
        )
    }

    override fun resetRewardsConnect(callback: (Result<Unit>) -> Unit) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.resetRewardsConnect(
            currentActivity,
            object : RewardsConnectResetListener {
                override fun onFailure(error: RewardsConnectResetFailureException) {
                    callback(
                        Result.failure(
                            ErrorsUtil.getFunctionError("registerRewardsConnect", error.message)
                        )
                    )
                }

                override fun onSuccess() {
                    callback(Result.success(Unit))
                }
            }
        )
    }

    override fun showInstalledApps(callback: (Result<Unit>) -> Unit) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.showInstalledApps(
            currentActivity,
            object : io.adjoe.sdk.studio.PlaytimeDeeplinkListener {
                override fun onError(error: PlaytimeResponseError?) {
                    callback(Result.failure(
                        ErrorsUtil.getFunctionError("showInstalledApps", error?.error?.message)
                    ))
                }

                override fun onOpened() {
                    return callback(Result.success(Unit))
                }
            }
        )
    }

    override fun showAppDetails(campaign: PlaytimeCampaign, callback: (Result<Unit>) -> Unit) {
        val playtimeCampaign = playtimeCampaignsCache[campaign.campaignUUID]

        if (playtimeCampaign == null) {
            return callback(Result.failure(
                ErrorsUtil.getFunctionError("openInStore", "campaign is null")
            ))
        }

        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.showAppDetails(
            currentActivity,
            playtimeCampaign,
            object : io.adjoe.sdk.studio.PlaytimeDeeplinkListener {
                override fun onError(error: PlaytimeResponseError?) {
                    callback(Result.failure(
                        ErrorsUtil.getFunctionError("showAppDetails", error?.error?.message)
                    ))
                }

                override fun onOpened() {
                    return callback(Result.success(Unit))
                }
            }
        )
    }

    override fun showAppDetailsWithToken(token: String, campaignAppId: String, callback: (Result<Unit>) -> Unit) {
        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.showAppDetails(
            currentActivity,
            token,
            campaignAppId,
            object : io.adjoe.sdk.studio.PlaytimeDeeplinkListener {
                override fun onError(error: PlaytimeResponseError?) {
                    callback(Result.failure(
                        ErrorsUtil.getFunctionError("showInstalledAppshowAppDetailsWithToken", error?.error?.message)
                    ))
                }

                override fun onOpened() {
                    return callback(Result.success(Unit))
                }
            }
        )
    }

    override fun openChatbot(campaign: PlaytimeCampaign?, callback: (Result<Unit>) -> Unit) {
        val playtimeCampaign = playtimeCampaignsCache[campaign?.campaignUUID]

        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.openChatbot(
            currentActivity,
            playtimeCampaign,
            object : io.adjoe.sdk.studio.PlaytimeDeeplinkListener {
                override fun onError(error: PlaytimeResponseError?) {
                    callback(Result.failure(
                        ErrorsUtil.getFunctionError("openChatbot", error?.error?.message)
                    ))
                }

                override fun onOpened() {
                    return callback(Result.success(Unit))
                }
            }
        )
    }

    override fun executeEngagement(campaign: PlaytimeCampaign, engagementType: String, callback: (Result<Unit>) -> Unit) {
        val playtimeCampaign = playtimeCampaignsCache[campaign.campaignUUID]

        val currentActivity = activity ?: run {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        if (playtimeCampaign == null) {
            return callback(Result.failure(
                ErrorsUtil.getFunctionError("executeEngagement", "campaign is null")
            ))
        }

        var playtimeEngagmentType: PlaytimeEngagementType = PlaytimeEngagementType.DEFAULT

        if (engagementType.equals("engaged")) {
            playtimeEngagmentType = io.adjoe.sdk.studio.PlaytimeEngagementType.ENGAGED
        }

        io.adjoe.sdk.studio.PlaytimeStudio.executeEngagement(
            currentActivity,
            playtimeCampaign,
            playtimeEngagmentType,
            object : PlaytimeExecuteEngagementListener {
                public override fun onFinished() {
                    callback(Result.success(Unit))
                }

                public override fun onError(error: PlaytimeResponseError?) {
                    if (error == null) {
                        callback(Result.failure(
                            ErrorsUtil.getFunctionError("executeEngagement", "Unknown error occurred")
                        ))
                        return
                    }

                    callback(Result.failure(
                        ErrorsUtil.getFunctionError("executeEngagement", error?.error?.message)
                    ))
                }

                public override fun onAlreadyEngaging() {
                    callback(Result.failure(
                        ErrorsUtil.getFunctionError("executeEngagement", "Request is already in progress")
                    ))
                }
            }
        )
    }

    private fun createPermissionsListener(
        functionName: String,
        callback: (Result<PlaytimePermissionsResponse>) -> Unit
    ): PlaytimePermissionsListener {
        return object : PlaytimePermissionsListener {
            override fun onError(error: PlaytimeResponseError) {
                callback(
                    Result.failure(
                        ErrorsUtil.getFunctionError(functionName, error.error.message)
                    )
                )
            }

            override fun onReceived(response: io.adjoe.sdk.studio.PlaytimePermissionsResponse) {
                callback(
                    Result.success(
                        PermissionsUtil.mapPlaytimePermissionsResponse(response)
                    )
                )
            }
        }
    }

    private fun createCampaignsListener(
        callback: (Result<PlaytimeCampaignsResponse>) -> Unit
    ): PlaytimeCampaignsListener {
        return object : PlaytimeCampaignsListener {
            override fun onError(error: PlaytimeResponseError) {
                callback(Result.failure(error.error))
            }

            override fun onReceived(response: io.adjoe.sdk.studio.PlaytimeCampaignsResponse) {
                val campaigns = CampaignsUtil.mapPlaytimeCampaignsResponse(response)
                savePlaytimeCampaignsInCache(response)
                callback(Result.success(campaigns))
            }
        }
    }

    private fun savePlaytimeCampaignsInCache(response: io.adjoe.sdk.studio.PlaytimeCampaignsResponse) {
        response.campaigns.forEach { campaign ->
            campaign.campaignUUID?.let { campaignUUID ->
                playtimeCampaignsCache.put(campaignUUID, campaign)
            }
        }
    }
}
