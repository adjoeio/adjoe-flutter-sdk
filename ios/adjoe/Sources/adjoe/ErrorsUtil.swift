import Foundation
import PlaytimeSDK

extension PlaytimeErrorType {
    /// The stable UPPER_SNAKE_CASE wire name of the category.
    ///
    /// The ordinals are a cross-platform contract but are not self-describing, so the wrapper sends
    /// these names to keep the Dart payload identical to Android's `errorType.name`.
    var name: String {
        switch self {
        case .network: return "NETWORK"
        case .networkVPN: return "NETWORK_VPN"
        case .api: return "API"
        case .responseFormat: return "RESPONSE_FORMAT"
        case .sdkConfig: return "SDK_CONFIG"
        case .unknown: return "UNKNOWN"
        @unknown default: return "UNKNOWN"
        }
    }
}

enum ErrorsUtil {
    static func notImplementedError(_ message: String = "Function is not implemented") -> PlaytimeException {
        return PlaytimeException(
            code: "",
            errorType: .unknown,
            isRetryable: false,
            message: message
        )
    }

    static func toPlaytimeException(_ error: Error?) -> PlaytimeException {
        if let exception = error as? PlaytimeException {
            return exception
        }

        return PlaytimeException(
            code: "",
            errorType: .unknown,
            isRetryable: false,
            message: error?.localizedDescription ?? "Unknown error occurred"
        )
    }

    static func toPigeonError(_ exception: PlaytimeException) -> PigeonError {
        return PigeonError(
            code: exception.errorType.name,
            message: exception.message,
            details: [
                "code": exception.code,
                "errorType": exception.errorType.name,
                "isRetryable": exception.isRetryable,
            ] as [String: any Sendable]
        )
    }

    static func toPigeonError(_ error: Error?) -> PigeonError {
        return toPigeonError(toPlaytimeException(error))
    }
}
