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
        if (activity == null) {
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
               activity!!,
               tokens,
               OptionsUtil.mapOptions(options),
               createCampaignsListener(callback)
           )
       } else {
           io.adjoe.sdk.studio.PlaytimeStudio.getCampaigns(
               activity!!,
               OptionsUtil.mapOptions(options),
               createCampaignsListener(callback)
           )
       }
    }

    override fun getInstalledCampaigns(
        options: PlaytimeOptions,
        callback: (Result<PlaytimeCampaignsResponse>) -> Unit
    ) {
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.getInstalledCampaigns(
            activity!!,
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

        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.openInStore(
            activity!!,
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

        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.openInstalledCampaign(
            activity!!,
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
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.getPermissions(
            activity!!,
            createPermissionsListener("getPermissions", callback)
        )
    }

    override fun showPermissionsPrompt(callback: (Result<PlaytimePermissionsResponse>) -> Unit) {
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.showPermissionsPrompt(
            activity!!,
            createPermissionsListener("showPermissionsPrompt", callback)
        )
    }

    override fun registerRewardsConnect(callback: (Result<Unit>) -> Unit) {
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.registerRewardsConnect(
            activity!!,
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
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.resetRewardsConnect(
            activity!!,
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
        // TODO: Uncomment this when this function is in native library
        callback(Result.success(Unit))
        /*
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.showInstalledApps(
            activity!!,
            object : PlaytimeShowDetailsListener {
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
        */
    }

    override fun showAppDetails(campaign: PlaytimeCampaign, callback: (Result<Unit>) -> Unit) {
        // TODO: Uncomment this when this function is in native library
        callback(Result.success(Unit))
        /*
        val playtimeCampaign = playtimeCampaignsCache[campaign.campaignUUID]
        
        if (playtimeCampaign == null) {
            return callback(Result.failure(
                ErrorsUtil.getFunctionError("openInStore", "campaign is null")
            ))
        }

        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.showAppDetails(
            activity!!,
            playtimeCampaign,
            object : PlaytimeShowDetailsListener {
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
        */
    }

    override fun showAppDetailsWithToken(token: String, campaignAppId: String, callback: (Result<Unit>) -> Unit) {
        // TODO: Uncomment this when this function is in native library
        callback(Result.success(Unit))
        /*
        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.showAppDetails(
            activity!!,
            token,
            campaignAppId,
            object : PlaytimeShowDetailsListener {
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
        */
    }

    override fun openChatbot(campaign: PlaytimeCampaign?, callback: (Result<Unit>) -> Unit) {
        // TODO: Uncomment this when this function is in native library
        callback(Result.success(Unit))
        /*
        val playtimeCampaign = playtimeCampaignsCache[campaign.campaignUUID]

        if (activity == null) {
            return callback(Result.failure(ErrorsUtil.getActivityIsNullError()))
        }

        io.adjoe.sdk.studio.PlaytimeStudio.openChatbot(
            activity!!,
            playtimeCampaign,
            object : PlaytimeOpenChatbotListener {
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
        */
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
