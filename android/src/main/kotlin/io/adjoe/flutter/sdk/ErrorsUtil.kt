package io.adjoe.flutter.sdk

import io.adjoe.sdk.PlaytimeException
import io.adjoe.sdk.PlaytimeErrorType

object ErrorsUtil {
    fun getFunctionError(functionName: String, reason: String? = null): PlaytimeException {
        if (reason.isNullOrEmpty()) {
            return PlaytimeException(
                "Error occurred during $functionName",
                null,
                PlaytimeErrorType.UNKNOWN,
                false
            )
        }

        return PlaytimeException(
            "Error occurred during $functionName: $reason",
            null,
            PlaytimeErrorType.UNKNOWN,
            false
        )
    }

    fun getActivityIsNullError(): PlaytimeException {
        return PlaytimeException(
            "activity is null",
            null,
            PlaytimeErrorType.UNKNOWN,
            false
        )
    }

    fun campaingIsNullError(): PlaytimeException {
        return PlaytimeException(
            "campaign is null",
            null,
            PlaytimeErrorType.UNKNOWN,
            false
        )
    }

    fun toPlaytimeException(throwable: Throwable?): PlaytimeException {
        if (throwable is PlaytimeException) {
            return throwable
        }

        return PlaytimeException(
            throwable?.message ?: "Unknown error occurred",
            throwable,
            PlaytimeErrorType.UNKNOWN,
            false
        )
    }

    fun toFlutterError(exception: PlaytimeException): FlutterError {
        return FlutterError(
            exception.errorType.name,
            exception.message,
            mapOf(
                "code" to exception.code,
                "errorType" to exception.errorType.name,
                "isRetryable" to exception.isRetryable
            )
        )
    }

    /** Convenience: coerce any throwable and map it to the Pigeon transport. */
    fun toFlutterError(throwable: Throwable?): FlutterError {
        return toFlutterError(toPlaytimeException(throwable))
    }
}
