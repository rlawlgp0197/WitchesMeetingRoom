package com.example.witchesmeeting.data

import com.google.gson.annotations.SerializedName

data class CalendarDTO(
    @SerializedName("start")
    val start : String?,
    @SerializedName("end")
    val end: String?,
    @SerializedName("peopleNum")
    val peopleNum : String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("department")
    val department : String?,
    @SerializedName("contents")
    val contents: String?,
    @SerializedName("year")
    val year: Int?,
    @SerializedName("month")
    val month: Int?,
    @SerializedName("day")
    val day: Int?,
    @SerializedName("createNm")
    val createNm: String?,
)

