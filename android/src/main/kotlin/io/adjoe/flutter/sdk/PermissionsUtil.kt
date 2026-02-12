package io.adjoe.flutter.sdk

object PermissionsUtil {
    fun mapPlaytimePermissionsResponse(
        playtimePermissionsResponse: io.adjoe.sdk.studio.PlaytimePermissionsResponse
    ): PlaytimePermissionsResponse {
       return PlaytimePermissionsResponse(
           permissions = mapPlaytimePermissions(playtimePermissionsResponse.permissions)
       )
    }

    private fun mapPlaytimePermissions(
        playtimePermissions: io.adjoe.sdk.studio.PlaytimePermissions
    ): PlaytimePermissions {
       return PlaytimePermissions(
           isTOSAccepted = playtimePermissions.isTOSAccepted,
           isUsagePermissionAccepted = playtimePermissions.isUsagePermissionAccepted
       )
    }
}
