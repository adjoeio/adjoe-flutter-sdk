package io.adjoe.flutter.sdk

import android.text.TextUtils
import java.text.DateFormat
import java.text.ParseException
import java.util.Date

object OptionsUtil {
    fun mapOptions(options: PlaytimeOptions): io.adjoe.sdk.PlaytimeOptions {
        val playtimeOptions = io.adjoe.sdk.PlaytimeOptions()
        playtimeOptions.setUserId(options.userId)

        options.sdkHash?.let {
            playtimeOptions.setSDKHash(it)
        }

        playtimeOptions.setParams(mapParams(options.params))
        val extensions = mapExtensions(options.extensions)

        extensions?.let {
            playtimeOptions.setExtensions(it)
        }

        playtimeOptions.setUserProfile(mapUserProfile(options.userProfile))
        playtimeOptions.w("flutter")
        return playtimeOptions
    }

    fun mapUserProfile(userProfile: PlaytimeUserProfile?): io.adjoe.sdk.PlaytimeUserProfile? {
        userProfile?.let {
            val genderFlutter = userProfile.gender
            val gender = if (genderFlutter == "male")
                io.adjoe.sdk.PlaytimeGender.MALE
            else if (genderFlutter == "female")
                io.adjoe.sdk.PlaytimeGender.FEMALE
            else
                io.adjoe.sdk.PlaytimeGender.UNKNOWN

            var birthday: Date? = null

            if (!TextUtils.isEmpty(userProfile.birthday)) {
                try {
                    birthday = DateFormat.getDateInstance().parse(userProfile.birthday)
                } catch (ignore: ParseException) {
                }
            }

            return io.adjoe.sdk.PlaytimeUserProfile(gender, birthday)
        }

        return null
    }

    fun mapExtensions(extensions: PlaytimeExtensions?): io.adjoe.sdk.PlaytimeExtensions? {
        extensions?.let {
            return io.adjoe.sdk.PlaytimeExtensions.Builder()
                .setSubId1(extensions.subId1)
                .setSubId2(extensions.subId2)
                .setSubId3(extensions.subId3)
                .setSubId4(extensions.subId4)
                .setSubId5(extensions.subId5)
                .build()
        }

        return null
    }

    fun mapParams(params: PlaytimeParams?): io.adjoe.sdk.PlaytimeParams? {
        params?.let {
            return io.adjoe.sdk.PlaytimeParams.Builder()
                .setPlacement(params.placement)
                .setUaChannel(params.uaChannel)
                .setUaNetwork(params.uaNetwork)
                .setUaSubPublisherCleartext(params.uaSubPublisherCleartext)
                .setUaSubPublisherEncrypted(params.uaSubPublisherEncrypted)
                .setPromotionTag(params.promotionTag)
                .build()
        }

        return null
    }
}
