package com.example.witchesmeeting.data

import com.google.gson.annotations.SerializedName

data class Reserve(
    val id:Int,
    val year: Int,
    val month: Int,
    val day: Int,
    val start : String,
    val end: String,
    val peopleNum : Int,
    val name: String,
    val createNm: String,
    val department : String,
    val contents: String,
    var isMe:Boolean = false,
    var isClicked:Boolean = false,
)