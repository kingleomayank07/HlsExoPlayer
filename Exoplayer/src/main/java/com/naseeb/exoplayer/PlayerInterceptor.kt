package com.naseeb.exoplayer

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody

class PlayerInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val changeResponse = response.body()?.string()
        return if (changeResponse?.contains("#EXT-X-TARGETDURATION:6")!!) {
            val responseNew =
                changeResponse.replaceFirst(
                    "#EXT-X-TARGETDURATION:6",
                    "#EXT-X-TARGETDURATION:2"
                )
            Log.d("TAG", "intercept: $responseNew")
            response.newBuilder().body(
                ResponseBody.create(
                    response.body()!!.contentType(),
                    responseNew
                )
            ).build()
        } else {
            chain.proceed(chain.request())
        }
    }
}

