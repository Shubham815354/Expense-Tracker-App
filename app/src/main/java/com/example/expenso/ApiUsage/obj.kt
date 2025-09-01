package com.example.expenso.ApiUsage

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object obj {
    private val Base_Url= "https://expensio-nkvc.onrender.com/"
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Base_Url)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
    fun <T> createService(service:Class<T>):T{
        return retrofit.create(service)
    }
}