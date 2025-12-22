package com.example.adminblinkitclone.api

import android.app.Notification
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {

    @Headers(
        "Content-Type: application/json",
        "Authorization: key="
    )

    @POST("fcm/send")
    fun sendNotification(@Body notification: Notification) : Call<Notification>

}