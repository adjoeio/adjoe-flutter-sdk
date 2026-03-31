import PlaytimeMonetize

class PlaytimeStudioImpl: PlaytimeStudio {
    func showInstalledApps(completion: @escaping (Result<Void, any Error>) -> Void) {
        Task {
            do {
                try await PlaytimeMonetize.PlaytimeStudio.showInstalledApps()
                completion(.success(()))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    func showAppDetails(campaign: PlaytimeCampaign, completion: @escaping (Result<Void, any Error>) -> Void) {
        Task {
            do {
                let jsonCampaign = try JSONUtil.convertCodableToJson(campaign)
                 try await PlaytimeMonetize.PlaytimeStudio.showAppDetails(
                     campaign: PlaytimeMonetize.PlaytimeCampaign(JSONObject: jsonCampaign)
                 )
                completion(.success(()))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    func showAppDetailsWithToken(token: String, campaignAppId: String, completion: @escaping (Result<Void, any Error>) -> Void) {
        Task {
            do {
                 try await PlaytimeMonetize.PlaytimeStudio.showAppDetails(
                    campaignAppId: campaignAppId,
                    token: token
                 )
                completion(.success(()))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    func openChatbot(campaign: PlaytimeCampaign?, completion: @escaping (Result<Void, any Error>) -> Void) {
        Task {
            do {
                if let campaign = campaign {
                    let jsonCampaign = try JSONUtil.convertCodableToJson(campaign)
                     try await PlaytimeMonetize.PlaytimeStudio.openChatbot(
                        campaign: PlaytimeMonetize.PlaytimeCampaign(JSONObject: jsonCampaign)
                        )
                } else {
                    try await PlaytimeMonetize.PlaytimeStudio.openChatbot(
                        campaign: nil,
                    )
                }
                completion(.success(()))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    func getCampaigns(
        options: PlaytimeOptions,
        completion: @escaping (Result<PlaytimeCampaignsResponse, any Error>) -> Void
    ) {
        Task {
            do {
                let jsonOptions = try JSONUtil.convertPlaytimeOptionsCodableToJson(options)
                var campaignsResponse: PlaytimeMonetize.PlaytimeCampaignsResponse
                
                if let tokens = options.tokens {
                    campaignsResponse = try await PlaytimeMonetize.PlaytimeStudio.getCampaigns(
                        tokens: filterTokens(tokens),
                        options: PlaytimeMonetize.PlaytimeOptions(JSONObject: jsonOptions)
                    )
                } else {
                    campaignsResponse = try await PlaytimeMonetize.PlaytimeStudio.getCampaigns(
                        options: PlaytimeMonetize.PlaytimeOptions(JSONObject: jsonOptions)
                    )
                }
                
                let campaignsResponseData = try JSONSerialization.data(
                    withJSONObject: campaignsResponse.toJSONObject()
                )
                
                completion(.success(try JSONUtil.decoder.decode(
                    PlaytimeCampaignsResponse.self, from: campaignsResponseData
                )))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    func getInstalledCampaigns(
        options: PlaytimeOptions,
        completion: @escaping (Result<PlaytimeCampaignsResponse, any Error>) -> Void
    ) {
        Task {
            do {
                let jsonOptions = try JSONUtil.convertPlaytimeOptionsCodableToJson(options)
                let campaignsResponse = try await PlaytimeMonetize.PlaytimeStudio.getInstalledCampaigns(
                    options: PlaytimeMonetize.PlaytimeOptions(JSONObject: jsonOptions)
                )
                let campaignsResponseData = try JSONSerialization.data(
                    withJSONObject: campaignsResponse.toJSONObject()
                )
                
                completion(.success(try JSONUtil.decoder.decode(
                    PlaytimeCampaignsResponse.self, from: campaignsResponseData
                )))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    func openInStore(
        campaign: PlaytimeCampaign,
        completion: @escaping (Result<Void, any Error>) -> Void
    ) {
        Task {
            do {
                let jsonCampaign = try JSONUtil.convertCodableToJson(campaign)
                try await PlaytimeMonetize.PlaytimeStudio.openInStore(
                    campaign: PlaytimeMonetize.PlaytimeCampaign(JSONObject: jsonCampaign)
                )
                completion(.success(()))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    func openInstalledCampaign(
        campaign: PlaytimeCampaign,
        completion: @escaping (Result<Void, any Error>) -> Void
    ) {
        Task {
            do {
                let jsonCampaign = try JSONUtil.convertCodableToJson(campaign)
                try await PlaytimeMonetize.PlaytimeStudio.openInstalledCampaign(
                    campaign: PlaytimeMonetize.PlaytimeCampaign(JSONObject: jsonCampaign)
                )
                completion(.success(()))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    func getPermissions(
        completion: @escaping (Result<PlaytimePermissionsResponse, any Error>) -> Void
    ) {
        Task {
            do {
                let permissionsResponse = try await PlaytimeMonetize.PlaytimeStudio.getPermissions()
                let permissionsResponseData = try JSONSerialization.data(
                    withJSONObject: permissionsResponse.toJSONObject()
                )
                
                completion(.success(try JSONUtil.decoder.decode(
                    PlaytimePermissionsResponse.self, from: permissionsResponseData
                )))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    func showPermissionsPrompt(
        completion: @escaping (Result<PlaytimePermissionsResponse, any Error>) -> Void
    ) {
        Task {
            do {
                let permissionsResponse = try await PlaytimeMonetize.PlaytimeStudio.showPermissionsPrompt()
                let permissionsResponseData = try JSONSerialization.data(
                    withJSONObject: permissionsResponse.toJSONObject()
                )
                
                completion(.success(try JSONUtil.decoder.decode(
                    PlaytimePermissionsResponse.self, from: permissionsResponseData
                )))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    func registerRewardsConnect(
        completion: @escaping (Result<Void, any Error>) -> Void
    ) {
        Task {
            do {
                try await PlaytimeMonetize.PlaytimeStudio.registerRewardsConnect()
                completion(.success(()))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    func resetRewardsConnect(
        completion: @escaping (Result<Void, any Error>) -> Void
    ) {
        Task {
            do {
                try await PlaytimeMonetize.PlaytimeStudio.resetRewardsConnect()
                completion(.success(()))
            } catch {
                completion(.failure(error))
            }
        }
    }

    func executeEngagement(
        campaign: PlaytimeCampaign,
        engagementType: String,
        completion: @escaping (Result<Void, Error>) -> Void
    ) {
        Task {
            do {
                let jsonCampaign = try JSONUtil.convertCodableToJson(campaign)
                var playtimeEngagementType: PlaytimeMonetize.PlaytimeEngagementType = .default
                
                if engagementType == "engaged" {
                    playtimeEngagementType = .engaged
                }

                 try await PlaytimeMonetize.PlaytimeStudio.executeEngagement(
                    for: PlaytimeMonetize.PlaytimeCampaign(JSONObject: jsonCampaign),
                    engagementType: playtimeEngagementType
                 )

                completion(.success(()))
            } catch {
                completion(.failure(error))
            }
        }
    }

    private func filterTokens(_ tokens: [String?]) -> [String] {
        tokens.filter { $0 != nil }.compactMap { $0 }
    }
}
