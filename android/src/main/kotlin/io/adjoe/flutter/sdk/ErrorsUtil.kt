package io.adjoe.flutter.sdk

object ErrorsUtil {
    fun getFunctionError(functionName: String, reason: String? = null): Exception {
        if (reason.isNullOrEmpty()) {
            return Exception("Error occurred during $functionName")
        }

        return Exception("Error occurred during $functionName: $reason")
    }

    fun getActivityIsNullError(): Exception {
        return Exception("activity is null")
    }
}
