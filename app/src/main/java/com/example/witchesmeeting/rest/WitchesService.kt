package com.example.witchesmeeting.rest

import com.example.witchesmeeting.data.CalendarDTO
import com.example.witchesmeeting.data.Remove
import com.example.witchesmeeting.data.Reserve
import com.example.witchesmeeting.data.ResponseString
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface WitchesService {

    @GET("FindScheduleList")
    suspend fun getMonthReserve(
        @Query("year") year: Int?,
        @Query("month") month: Int?
    ): Response<List<Reserve>>

    @FormUrlEncoded
    @POST("/login")
    fun userLogin(
        @Field("loginId") loginId: String?,
        @Field("sns") sns: String?
    ): Call<String>?

    @POST("insertSchedule")
    fun postData(@Body calendarDTO: CalendarDTO?): Call<String?>?
//    fun postData(@Body calendarDTO: String?): Call<String?>?

    @PATCH("cancleSchedule")
    fun deleteData(@Body calendarDTO: Remove?): Call<String?>?

}
