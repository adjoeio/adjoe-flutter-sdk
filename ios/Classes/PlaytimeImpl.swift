import PlaytimeMonetize

class PlaytimeImpl: Playtime {
    func showCatalog(
        params: PlaytimeParams?,
        completion: @escaping (Result<Void, any Error>) -> Void
    ) {
        completion(.failure(PlaytimeImplError.notImplemented("Function is not implemented")))
    }
    
    func showCatalogWithOptions(
        options: PlaytimeOptions,
        completion: @escaping (Result<Void, any Error>) -> Void
    ) {
        Task {
            do {
                let jsonOptions = try JSONUtil.convertPlaytimeOptionsCodableToJson(options)
                try await PlaytimeMonetize.Playtime.showCatalog(
                    options: PlaytimeMonetize.PlaytimeOptions(JSONObject: jsonOptions)
                )
                completion(.success(()))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    func setUAParams(
        params: PlaytimeParams,
        completion: @escaping (Result<Void, any Error>) -> Void
    ) {
        completion(.failure(PlaytimeImplError.notImplemented("Function is not implemented")))
    }
    
    func getVersion(
        completion: @escaping (Result<String, any Error>) -> Void
    ) {
        completion(.failure(PlaytimeImplError.notImplemented("Function is not implemented")))
    }
    
    func getVersionName(
        completion: @escaping (Result<String, any Error>) -> Void
    ) {
        completion(.failure(PlaytimeImplError.notImplemented("Function is not implemented")))
    }
    
    func isInitialized(
        completion: @escaping (Result<Bool, any Error>) -> Void
    ) {
        completion(.failure(PlaytimeImplError.notImplemented("Function is not implemented")))
    }
    
    func hasAcceptedTOS(
        completion: @escaping (Result<Bool, any Error>) -> Void
    ) {
        completion(.failure(PlaytimeImplError.notImplemented("Function is not implemented")))
    }
    
    func hasAcceptedUsagePermission(
        completion: @escaping (Result<Bool, any Error>) -> Void
    ) {
        completion(.failure(PlaytimeImplError.notImplemented("Function is not implemented")))
    }
    
    func getUserId(
        completion: @escaping (Result<String?, any Error>) -> Void
    ) {
        Task {
            do {
                let userId = try await PlaytimeMonetize.Playtime.getUserId()
                completion(.success(userId))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    func sendEvent(
        event: Int64,
        extra: String,
        params: PlaytimeParams?,
        completion: @escaping (Result<Void, any Error>) -> Void
    ) {
        completion(.failure(PlaytimeImplError.notImplemented("Function is not implemented")))
    }
    
    func getStatus(
        completion: @escaping (Result<PlaytimeStatus, any Error>) -> Void
    ) {
        Task {
            do {
                let statusResponse = try await PlaytimeMonetize.Playtime.getStatus()
                completion(.success(mapStatus(statusResponse)))
            } catch {
                completion(.failure(error))
            }
        }
    }
    
    private func mapStatus(_ status: PlaytimeMonetize.PlaytimeStatus) -> PlaytimeStatus {
        let details = PlaytimeStatusDetails(
            isFraud: status.details.isFraud,
            campaignsAvailable: status.details.campaignsAvailable,
            campaignsState: mapCampaignsState(status.details.campaignsState)
        )
        return PlaytimeStatus(isInitialized: status.isInitialized, details: details)
    }
    
    private func mapCampaignsState(_ states: [PlaytimeMonetize.PlaytimeCampaignsState]) -> [String?] {
        return states.map { state in
            switch state {
            case .blocked:
                return "BLOCKED"
            case .vpn:
                return "VPN_DETECTED"
            case .geoMismatch:
                return "GEO_MISMATCH"
            default:
                return "READY"
            }
        }
    }
    
    func setPlaytimeOptions(
        options: PlaytimeOptions, completion: @escaping (Result<Void, Error>) -> Void
    ) {
        Task {
            do {
                let jsonOptions = try JSONUtil.convertPlaytimeOptionsCodableToJson(options)
                try await PlaytimeMonetize.Playtime.setPlaytimeOptions(
                    options: PlaytimeMonetize.PlaytimeOptions(JSONObject: jsonOptions)
                )
                completion(.success(()))
            } catch {
                completion(.failure(error))
            }
        }
    }
}
