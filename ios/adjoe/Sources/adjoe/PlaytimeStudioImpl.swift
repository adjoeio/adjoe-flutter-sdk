import Foundation
import PlaytimeSDK

class PlaytimeStudioImpl: PlaytimeStudio {
    private func nativeCampaign(
        _ campaign: PlaytimeCampaign
    ) throws -> PlaytimeSDK.PlaytimeCampaign {
        let jsonCampaign = try JSONUtil.convertCodableToJson(campaign)
        return try PlaytimeSDK.PlaytimeCampaign(JSONObject: jsonCampaign as Any)
    }

    private func pigeonResponse(
        _ response: PlaytimeSDK.PlaytimeCampaignsResponse
    ) throws -> PlaytimeCampaignsResponse {
        let data = try JSONSerialization.data(withJSONObject: response.toJSONObject())
        return try JSONUtil.decoder.decode(PlaytimeCampaignsResponse.self, from: data)
    }

    func updateTosConsent(
        action: TosConsentAction,
        completion: @escaping (Result<Void, Error>) -> Void
    ) {
        Task {
            do {
                let nativeAction: PlaytimeSDK.TosConsentAction =
                    (action == .wITHDRAW) ? .withdraw : .accept
                try await PlaytimeSDK.PlaytimeStudio.updateTosConsent(action: nativeAction)
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func getCampaigns(
        campaignOptions: CampaignOptions?,
        completion: @escaping (Result<PlaytimeCampaignsResponse, Error>) -> Void
    ) {
        Task {
            do {
                guard let campaignOptions else {
                    let response = try await PlaytimeSDK.PlaytimeStudio.getCampaigns()
                    completion(.success(try pigeonResponse(response)))
                    return
                }

                let jsonOptions = try JSONUtil.convertCodableToJson(campaignOptions)
                let decoded = try PlaytimeSDK.CampaignOptions(JSONObject: jsonOptions as Any)
                let response = try await PlaytimeSDK.PlaytimeStudio.getCampaigns(
                    campaignOptions: decoded
                )
                completion(.success(try pigeonResponse(response)))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func getOfferDetails(
        tokens: [String?],
        campaignOptions: CampaignOptions?,
        completion: @escaping (Result<PlaytimeCampaignsResponse, Error>) -> Void
    ) {
        Task {
            do {
                guard let campaignOptions else {
                    let response = try await PlaytimeSDK.PlaytimeStudio.getOfferDetails(
                        tokens: tokens.compactMap { $0 }
                    )
                    completion(.success(try pigeonResponse(response)))
                    return
                }

                let jsonOptions = try JSONUtil.convertCodableToJson(campaignOptions)
                let decoded = try PlaytimeSDK.CampaignOptions(JSONObject: jsonOptions as Any)
                let response = try await PlaytimeSDK.PlaytimeStudio.getOfferDetails(
                    tokens: tokens.compactMap { $0 },
                    campaignOptions: decoded
                )
                completion(.success(try pigeonResponse(response)))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func getInstalledCampaigns(
        options: InstalledCampaignOptions?,
        completion: @escaping (Result<PlaytimeCampaignsResponse, Error>) -> Void
    ) {
        Task {
            do {
                guard let options else {
                    let response = try await PlaytimeSDK.PlaytimeStudio.getInstalledCampaigns()
                    completion(.success(try pigeonResponse(response)))
                    return
                }

                let jsonOptions = try JSONUtil.convertCodableToJson(options)
                let playtimeSDKInstalledCampaignOptions = try PlaytimeSDK.InstalledCampaignOptions(JSONObject: jsonOptions as Any)
                let response = try await PlaytimeSDK.PlaytimeStudio.getInstalledCampaigns(
                    options: playtimeSDKInstalledCampaignOptions
                )
                completion(.success(try pigeonResponse(response)))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func openInStore(
        campaign: PlaytimeCampaign,
        useInlineInstall: Bool,
        completion: @escaping (Result<Void, Error>) -> Void
    ) {
        Task {
            do {
                try await PlaytimeSDK.PlaytimeStudio.openInStore(
                    campaign: try nativeCampaign(campaign),
                    useInlineInstall: useInlineInstall
                )
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func openInstalledCampaign(
        campaign: PlaytimeCampaign,
        completion: @escaping (Result<Void, Error>) -> Void
    ) {
        Task {
            do {
                try await PlaytimeSDK.PlaytimeStudio.openInstalledCampaign(
                    campaign: try nativeCampaign(campaign)
                )
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func openChatbot(
        campaign: PlaytimeCampaign?,
        completion: @escaping (Result<Void, Error>) -> Void
    ) {
        Task {
            do {
                let nativeValue = try campaign.map { try nativeCampaign($0) }
                try await PlaytimeSDK.PlaytimeStudio.openChatbot(campaign: nativeValue)
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func resetRewardsConnect(completion: @escaping (Result<Void, Error>) -> Void) {
        Task {
            do {
                try await PlaytimeSDK.PlaytimeStudio.resetRewardsConnect()
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func registerRewardsConnect(completion: @escaping (Result<Void, Error>) -> Void) {
        Task {
            do {
                try await PlaytimeSDK.PlaytimeStudio.registerRewardsConnect()
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func executeCampaignImpression(
        campaign: PlaytimeCampaign,
        impressionType: PlaytimeImpressionType?,
        completion: @escaping (Result<Void, Error>) -> Void
    ) {
        Task {
            do {
                let nativeType: PlaytimeSDK.PlaytimeImpressionType =
                    (impressionType == .eNGAGED) ? .engaged : .default
                try await PlaytimeSDK.PlaytimeStudio.executeCampaignImpression(
                    campaign: try nativeCampaign(campaign),
                    impressionType: nativeType
                )
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func executeOfferImpression(
        token: String,
        appID: String,
        completion: @escaping (Result<Void, Error>) -> Void
    ) {
        Task {
            do {
                try await PlaytimeSDK.PlaytimeStudio.executeOfferImpression(
                    token: token,
                    appID: appID
                )
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func showCampaignDetails(
        campaign: PlaytimeCampaign,
        completion: @escaping (Result<Void, Error>) -> Void
    ) {
        Task {
            do {
                try await PlaytimeSDK.PlaytimeStudio.showCampaignDetails(
                    campaign: try nativeCampaign(campaign)
                )
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func showOfferDetails(
        token: String,
        appID: String,
        completion: @escaping (Result<Void, Error>) -> Void
    ) {
        Task {
            do {
                try await PlaytimeSDK.PlaytimeStudio.showOfferDetails(
                    token: token,
                    appID: appID
                )
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func showInstalledCampaigns(completion: @escaping (Result<Void, Error>) -> Void) {
        Task {
            do {
                try await PlaytimeSDK.PlaytimeStudio.showInstalledCampaigns()
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func showPermissionsPrompt(completion: @escaping (Result<Void, Error>) -> Void) {
        completion(
            .failure(
                ErrorsUtil.toPigeonError(
                    ErrorsUtil.notImplementedError("Function is not implemented")
                )
            )
        )
    }
}
