package io.adjoe.flutter.sdk

object CampaignsUtil {
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
                totalCoinsCollected = playtimeEventConfig.totalCoinsCollected?.toLong(),
                totalCoinsPossible = playtimeEventConfig.totalCoinsPossible?.toLong(),
                cashbackReward = mapPlaytimeCashbackConfig(playtimeEventConfig.cashbackReward),
                multipliersActions = mapPlaytimeMultipliersActions(playtimeEventConfig.multipliersActions),
            )
       }

        return null
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
               events = mapPlaytimeCashbackRewardEvents(it.events)
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
