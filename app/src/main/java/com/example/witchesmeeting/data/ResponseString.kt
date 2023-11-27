package com.example.witchesmeeting.data

import com.google.gson.annotations.SerializedName

data class ResponseString(
    @SerializedName("message")
    val message: String,
)
