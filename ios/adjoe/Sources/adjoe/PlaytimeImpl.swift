import Foundation
import PlaytimeSDK

class PlaytimeImpl: Playtime {
    func initPlaytime(
        options: InitOptions,
        completion: @escaping (Result<Void, Error>) -> Void
    ) {
        Task {
            do {
                let jsonOptions = try JSONUtil.convertInitOptionsCodableToJson(options)
                try await PlaytimeSDK.Playtime.initPlaytime(
                    options: PlaytimeSDK.InitOptions(JSONObject: jsonOptions as Any)
                )
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func showCatalog(
        catalogOptions: CatalogOptions?,
        completion: @escaping (Result<Void, Error>) -> Void
    ) {
        Task {
            do {
                guard let catalogOptions else {
                    try await PlaytimeSDK.Playtime.showCatalog()
                    completion(.success(()))
                    return
                }

                let jsonOptions = try JSONUtil.convertCodableToJson(catalogOptions)
                try await PlaytimeSDK.Playtime.showCatalog(
                    catalogOptions: PlaytimeSDK.CatalogOptions(JSONObject: jsonOptions as Any)
                )
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func getStatus(
        completion: @escaping (Result<PlaytimeStatus, Error>) -> Void
    ) {
        Task {
            do {
                let statusResponse = try await PlaytimeSDK.Playtime.getStatus()

                var testGroup: Int64? = nil
                if let statusResponseTestGroup = statusResponse.details.testGroup {
                    testGroup = Int64(statusResponseTestGroup)
                }

                let details = PlaytimeStatusDetails(
                    isFraud: statusResponse.details.isFraud,
                    campaignsAvailable: statusResponse.details.campaignsAvailable,
                    campaignsState: statusResponse.details.campaignsState.map { state in
                        switch state {
                        case .blocked:
                            return .bLOCKED
                        case .vpnDetected:
                            return .vPNDETECTED
                        case .geoMismatch:
                            return .gEOMISMATCH
                        case .notInitialized:
                            return .nOTINITIALIZED
                        default:
                            return .rEADY
                        }
                    },
                    testGroup: testGroup
                )

                let status = PlaytimeStatus(
                    version: statusResponse.version,
                    isInitialized: statusResponse.isInitialized,
                    userId: statusResponse.userId,
                    isTosAccepted: statusResponse.isTosAccepted,
                    details: details,
                    isUsagePermissionAccepted: nil
                )

                completion(.success(status))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func teardown(completion: @escaping (Result<Void, Error>) -> Void) {
        Task {
            do {
                try await PlaytimeSDK.Playtime.teardown()
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func addEventListener(completion: @escaping (Result<Void, Error>) -> Void) {
        Task {
            do {
                try await PlaytimeSDK.Playtime.addEventListener(
                    eventListener: PlaytimeEventForwarder.shared
                )
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func removeEventListener(completion: @escaping (Result<Void, Error>) -> Void) {
        Task {
            do {
                try await PlaytimeSDK.Playtime.removeEventListener()
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }

    func executeImpressionEvent(
        catalogPlacement: CatalogPlacement,
        completion: @escaping (Result<Void, Error>) -> Void
    ) {
        Task {
            do {
                let jsonPlacement = try JSONUtil.convertCodableToJson(catalogPlacement)
                let nativePlacement = try PlaytimeSDK.CatalogPlacement(
                    JSONObject: jsonPlacement as Any
                )
                try await PlaytimeSDK.Playtime.executeImpressionEvent(
                    catalogPlacement: nativePlacement
                )
                completion(.success(()))
            } catch {
                completion(.failure(ErrorsUtil.toPigeonError(error)))
            }
        }
    }
}
