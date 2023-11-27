package com.example.witchesmeeting.rest

import com.example.witchesmeeting.data.Reserve
import com.google.gson.GsonBuilder
import com.prolificinteractive.materialcalendarview.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


object Repository {
    private val witchesService: WitchesService by lazy {
        buildWitchesService()
    }

    fun buildWitchesService(): WitchesService {
        return Retrofit.Builder()
            .baseUrl("url")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(buildOkHttpClient())
            .build()
            .create(WitchesService::class.java)
    }

    suspend fun getMonthReserve(year: Int?, month: Int?): List<Reserve>? =
        witchesService.getMonthReserve(year, month).body()

    private fun buildOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()
}