package com.naseeb.exoplayer

import android.content.Context
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.naseeb.log.LogUtil
import java.util.*

class ExoPlayerInstanceManagerImpl private constructor(c: Context) : ExoPlayerInstanceManager {

    private var c: Context?
    private val tokenExoPlayerMap: MutableMap<Int, SimpleExoPlayer?> = HashMap()
    private val userInstances = ArrayList<Int>()
    private var single: SimpleExoPlayer? = null
    private val MIN_BUFFER = 2000
    private val MAX_BUFFER = 6000
    private val BUFFER_PLAYBACK = 2000
    private val BUFFER_PLAYBACK_RE_BUFFER = 2000

    @Synchronized
    override fun destroy() {
        LogUtil.debugLog(TAG, "destroy")
        if (tokenExoPlayerMap.isNotEmpty()) {
            for (e in tokenExoPlayerMap.values) {
                e!!.release()
            }
        }
        tokenExoPlayerMap.clear()
        userInstances.clear()
        if (single != null) single!!.release()
        single = null
        c = null
        sInstance = null
    }// players in use

    // not in use
    @get:Synchronized
    override val freeInstanceOfExoPlayer: ExoPlayerInstanceManager.SimpleExoPlayerWrapper
        get() {
            var e: SimpleExoPlayer? = null
            var token: Int? = null
            if (userInstances.isEmpty()) {
                // not in use
                LogUtil.debugLog(TAG, "getFreeInstanceOfExoPlayer userInstances.isEmpty()")
                if (single == null) {
                    single = newInstance()
                    LogUtil.debugLog(
                        TAG,
                        "getFreeInstanceOfExoPlayer userInstances.isEmpty() newInstance"
                    )
                }
                e = single
                token = 0
                userInstances.add(token)
                tokenExoPlayerMap[token] = single
            } else {
                LogUtil.debugLog(
                    TAG,
                    "getFreeInstanceOfExoPlayer !userInstances.isEmpty() size is " + userInstances.size
                )
                // players in use
                e = newInstance()
                token = userInstances.size
                userInstances.add(token)
                tokenExoPlayerMap[token] = e
            }
            val s = ExoPlayerInstanceManager.SimpleExoPlayerWrapper()
            s.simpleExoPlayer = e
            s.token = token
            LogUtil.debugLog(TAG, "getFreeInstanceOfExoPlayer player : $e")
            return s
        }

    private fun newInstance(): SimpleExoPlayer {
        LogUtil.debugLog(TAG, "newInstance")
        // 1. Create a default TrackSelector
//        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory
//                (new DefaultBandwidthMeter());
        // 2. Create the player
//        return ExoPlayerFactory.newSimpleInstance(c!!, trackSelector)
        val trackSelector: TrackSelector = DefaultTrackSelector(c!!)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                MIN_BUFFER, MAX_BUFFER,
                BUFFER_PLAYBACK, BUFFER_PLAYBACK_RE_BUFFER
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        return SimpleExoPlayer.Builder(c!!)
            .setLoadControl(loadControl)
            .experimentalSetThrowWhenStuckBuffering(true)
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(
                DefaultBandwidthMeter.Builder(c!!)
                    .build()
            )
            .build()
    }


    @Synchronized
    override fun release(token: Int?) {
        if (token == null) {
            LogUtil.debugLog(TAG, "release not releasing player since token is null")
            return
        }
        if (userInstances.contains(token)) {
            LogUtil.debugLog(TAG, "release token : $token")
            // release the instance
            userInstances.remove(token)
            val e = tokenExoPlayerMap.remove(token)
            if (userInstances.size == 0) {
                LogUtil.debugLog(TAG, "release userInstances.size() == 0 exoplayer : $e")
                single = e
            } else {
                e!!.release()
                LogUtil.debugLog(
                    TAG, "release userInstances.size() is " + userInstances.size +
                            " exoplayer : " + e
                )
            }
        } else {
            LogUtil.debugLog(TAG, "release not contain token : $token")
        }
    }


    companion object {
        private val TAG = ExoPlayerInstanceManagerImpl::class.java.canonicalName
        private var sInstance: ExoPlayerInstanceManager? = null

        @Synchronized
        fun getInstance(c: Context): ExoPlayerInstanceManager {
            if (sInstance == null) {
                sInstance = ExoPlayerInstanceManagerImpl(c)
            }
            return sInstance!!
        }

    }

    init {
        this.c = c.applicationContext
    }
}