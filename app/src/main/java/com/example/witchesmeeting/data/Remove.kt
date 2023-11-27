package com.example.witchesmeeting.data

import com.google.gson.annotations.SerializedName


data class Remove(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("createNm")
    val createNm: String?,
    @SerializedName("cancleName")
    val cancleName:String?,
    @SerializedName("cancleReason")
    val cancleReason: String?,
    @SerializedName("loginId")
    val loginId: String?,
)
