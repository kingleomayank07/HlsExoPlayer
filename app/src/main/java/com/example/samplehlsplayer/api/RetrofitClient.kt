package com.example.samplehlsplayer.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://api.twitch.tv/api/channels/"
//    const val STREAM_BASE_URL = "http://usher.twitch.tv/api/channel/"

    val instance: TwitchApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(TwitchApi::class.java)
    }

    /*val streamInstance: TwitchApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(STREAM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(TwitchApi::class.java)
    }
*/

}