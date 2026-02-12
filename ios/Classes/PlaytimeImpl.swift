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
                let statusResponseData = try JSONSerialization.data(
                    withJSONObject: statusResponse.toJSONObject()
                )
                
                completion(.success(try JSONUtil.decoder.decode(
                    PlaytimeStatus.self, from: statusResponseData
                )))
            } catch {
                completion(.failure(error))
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
