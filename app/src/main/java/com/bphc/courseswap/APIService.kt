package com.bphc.courseswap

import com.bphc.courseswap.app.Constants.Companion.CONTENT_TYPE
import com.bphc.courseswap.app.Constants.Companion.SEND_NOTIFICATION
import com.bphc.courseswap.app.Constants.Companion.SERVER_KEY
import com.bphc.courseswap.models.Notification
import com.squareup.okhttp.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST(SEND_NOTIFICATION)
    suspend fun postNotification(
        @Body notification: Notification
    ): Response<ResponseBody>

}