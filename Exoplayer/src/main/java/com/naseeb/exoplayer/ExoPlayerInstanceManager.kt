package com.naseeb.exoplayer

import android.content.Context
import com.google.android.exoplayer2.SimpleExoPlayer

interface ExoPlayerInstanceManager {
    val freeInstanceOfExoPlayer: SimpleExoPlayerWrapper?
    fun release(token: Int?)
    fun destroy()

    object Instance {
        fun getInstance(c: Context?): ExoPlayerInstanceManager {
            return ExoPlayerInstanceManagerImpl.getInstance(c!!)
        }
    }

    class SimpleExoPlayerWrapper {
        var simpleExoPlayer: SimpleExoPlayer? = null
        var token: Int? = null
    }
}