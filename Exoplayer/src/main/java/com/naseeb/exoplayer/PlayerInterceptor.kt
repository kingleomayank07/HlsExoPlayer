package com.naseeb.exoplayer

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody

class PlayerInterceptor : Interceptor {

    //region variables
    private val previousTargetDuration = "#EXT-X-TARGETDURATION:6"
    private val newTargetDuration = "#EXT-X-TARGETDURATION:2"
    //endregion

    override fun intercept(chain: Interceptor.Chain): Response {
        //getting response from the chain
        val response = chain.proceed(chain.request())
        // getting response body
        val changeResponse = response.body()?.string()
        //checking if change response contains #TARGETDURATION:6
        return if (changeResponse?.contains(previousTargetDuration)!!) {
            //replacing #EXT-X-TARGETDURATION:6 to #EXT-X-TARGETDURATION:2
            val responseNew = changeResponse.replaceFirst(
                previousTargetDuration,
                newTargetDuration
            )
            //creating new responseBody to give to exoplayer
            response.newBuilder().body(
                ResponseBody.create(
                    response.body()!!.contentType(),
                    responseNew
                )
            ).build()
        } else {
            //if not then proceed request chain.
            chain.proceed(chain.request())
        }
    }
}

