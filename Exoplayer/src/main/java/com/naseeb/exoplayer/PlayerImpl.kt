package com.naseeb.exoplayer

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.naseeb.log.LogUtil
import okhttp3.OkHttpClient
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.ExperimentalTime


class PlayerImpl(
    private val context: Context,
    private val uri: Uri,
    private val playerView: PlayerView,
    private val playerCallback: IPlayer.PlayerCallback?
) : IPlayer, Player.EventListener, AnalyticsListener,
    MediaSourceEventListener {

    //region variables
    private val TAG = PlayerImpl::class.java.canonicalName
    private var mPlayer: SimpleExoPlayer? = null
    private var mToken: Int? = null
    private var mMediaSource: MediaSource? = null
    private var mSimpleCache: SimpleCache? = null
    private var mTimeLine = MutableLiveData<String>()
    private var mIsStartedPlayingFirstTime: Boolean = true
    //endregion

    override fun pause() {
        LogUtil.debugLog(TAG, "pause")
        if (mPlayer?.isPlaying!!) {
            mPlayer!!.playWhenReady = false
            mPlayer!!.pause()
        } else {
            mPlayer!!.play()
            mPlayer!!.playWhenReady = true
        }
    }

    private fun stopPlayer(isReset: Boolean) {
        LogUtil.debugLog(TAG, "stopPlayer isReset : $isReset mExoPlayer : $mPlayer")
        if (mPlayer != null) {
            if (mMediaSource != null) {
                mMediaSource!!.removeEventListener(this)
            }
            mMediaSource = null
            if (isReset) { //If player is getting reset
                //Removing analytics listener
                mPlayer!!.removeAnalyticsListener(this)
                //Removing player event listener
                mPlayer!!.removeListener(this)
            }
            mPlayer!!.stop()
            if (isReset) {
                mSimpleCache?.release()
                ExoPlayerInstanceManager.Instance.getInstance(context).release(mToken)
                mPlayer = null
                mToken = null
            }
        }
    }

    override fun resume() {
        LogUtil.debugLog(TAG, "resume")
        play()
    }

    override fun seekTo(position: Long) {
        mPlayer?.seekTo(position)
    }

    override fun release() {
        LogUtil.debugLog(TAG, "release")
        stopPlayer(true)
    }

    fun getTrackSelector(): TrackSelector? {
        return mPlayer?.trackSelector
    }

    fun getTime(): LiveData<String?> {
        return mTimeLine
    }

    override fun play() {
        LogUtil.debugLog(TAG, "play uri : $uri")
        //Initializing exoplayer
        if (mPlayer == null) {
            LogUtil.debugLog(TAG, "play initializing player as it is null")
            val simpleExoPlayerWrapper =
                ExoPlayerInstanceManager.Instance.getInstance(context).freeInstanceOfExoPlayer
            mPlayer = simpleExoPlayerWrapper?.simpleExoPlayer
            mToken = simpleExoPlayerWrapper?.token

            mPlayer?.addListener(this)
            mPlayer?.addAnalyticsListener(this)
            playerView.setKeepContentOnPlayerReset(true)
            playerView.player = mPlayer
        }
        //Playing content in exo player
        mMediaSource = buildMediaSource(uri)
        mMediaSource!!.addEventListener(Handler(Looper.myLooper()!!), this)
        mPlayer!!.setMediaSource(mMediaSource!!)
        mPlayer!!.prepare()
        mPlayer!!.playWhenReady = true
        mIsStartedPlayingFirstTime = true
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        LogUtil.debugLog(TAG, "buildMediaSource uri : $uri")
        return when (@C.ContentType val type = Util.inferContentType(uri)) {
            C.TYPE_DASH -> {
                LogUtil.debugLog(TAG, "buildMediaSource TYPE_DASH")
                DashMediaSource.Factory(
                    DefaultDashChunkSource.Factory(buildDataSourceFactory()),
                    buildDataSourceFactory()
                ).createMediaSource(uri)
            }
            C.TYPE_SS -> {
                LogUtil.debugLog(TAG, "buildMediaSource TYPE_SS")
                SsMediaSource.Factory(
                    DefaultSsChunkSource.Factory(buildDataSourceFactory()),
                    buildDataSourceFactory()
                ).createMediaSource(uri)
            }
            C.TYPE_HLS -> {
                LogUtil.debugLog(TAG, "buildMediaSource TYPE_HLS")

                //creating okHttpDataSourceFactory with network interceptor
                val okHttpDataSourceFactory = OkHttpDataSourceFactory(
                    OkHttpClient().newBuilder()
                        //adding Interceptor
                        .addInterceptor(PlayerInterceptor())
                        .build(),
                    "app_name",
                    DefaultBandwidthMeter.Builder(context).build()
                )


                //creating HlsMediaSource with okHttpDataSourceFactory
                HlsMediaSource.Factory(okHttpDataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(MediaItem.Builder().setUri(uri).build())
            }
            C.TYPE_OTHER -> {
                LogUtil.debugLog(TAG, "buildMediaSource TYPE_OTHER")
                ProgressiveMediaSource.Factory(buildDataSourceFactory()).createMediaSource(uri)
            }
            else -> {
                LogUtil.debugLog(TAG, "buildMediaSource default")
                throw IllegalStateException("Unsupported type: $type")
            }
        }
    }

    private fun buildDataSourceFactory(): DataSource.Factory {
        LogUtil.debugLog(TAG, "buildDataSourceFactory")
        // Specify cache folder, my cache folder named media which is inside getCacheDir.
        /*val cacheFolder = File(context.cacheDir, "exoplayer_cache")
        LogUtil.debugLog(TAG, "buildDataSourceFactory cacheFolder : $cacheFolder")
        val cacheSize = 200L
        LogUtil.debugLog(TAG, "buildDataSourceFactory cache size : $cacheSize MB")
        // Specify cache size and removing policies*/
        // My cache size will be cacheSize * 1MB and it will automatically remove least recently used files if the size is
        // reached out.
//        val cacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize * 1024 * 1024)
        //Database provider
//        val databaseProvider: DatabaseProvider = ExoDatabaseProvider(context)
        // Build cache
        /* mSimpleCache = SimpleCache(cacheFolder, cacheEvictor, databaseProvider)
         // Build data source factory with cache enabled, if data is available in cache it will return immediately,
         // otherwise it will open a new connection to get the data.

         *//*return CacheDataSourceFactory(

            mSimpleCache!!, DefaultDataSourceFactory(
                context, DefaultHttpDataSourceFactory
                    ("app_name")
            )
        )*/
        return DefaultDataSourceFactory(context)
    }

    @Override
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

        LogUtil.debugLog(
            TAG, "PlayerEventListener onPlayerStateChanged playWhenReady : " +
                    playWhenReady + " playbackState : " + playbackState + " "
        )
        when (playbackState) {
            Player.STATE_READY -> {
                playerCallback?.onBufferingEnded()
                Log.d(TAG, "onBufferingStarted: ${mPlayer?.totalBufferedDuration}")
                LogUtil.debugLog(
                    TAG, "PlayerEventListener onPlayerStateChanged Player.STATE_READY " +
                            "mIsStartedPlayingFirstTime : $mIsStartedPlayingFirstTime"
                )
                //We have to update video duration only first time when a video starts playing
                if (mIsStartedPlayingFirstTime) {
                    mIsStartedPlayingFirstTime = false
                    if (mPlayer != null) {
                        playerCallback?.onMediaDurationFetched(mPlayer!!.duration)
                    } else {
                        LogUtil.debugLog(
                            TAG, "PlayerEventListener onPlayerStateChanged Player.STATE_READY " +
                                    "playerCallback onVideoDurationFetched player instance is null"
                        )
                    }
                } else {
                    playerCallback?.onBufferingEnded()
                }
            }
            Player.STATE_ENDED -> {
                playerCallback?.onPlayEnded()
            }
            Player.STATE_BUFFERING -> {
                playerCallback?.onBufferingStarted()
            }
            Player.STATE_IDLE -> {

            }
        }
    }


    override fun onPlayerError(error: ExoPlaybackException) {
        LogUtil.errorLog(TAG, "Entering onPlayerError() error: ${error.message}")

        when (error.type) {
            ExoPlaybackException.TYPE_SOURCE -> {
                LogUtil.errorLog(TAG, "TYPE_SOURCE: " + error.sourceException.message)
                playerCallback?.onPlayerNetworkError()
            }
            ExoPlaybackException.TYPE_UNEXPECTED -> {
                LogUtil.errorLog(TAG, "TYPE_UNEXPECTED: " + error.unexpectedException.message)
            }
            ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
                LogUtil.errorLog(TAG, "TYPE_OUT_OF_MEMORY: " + error.unexpectedException.message)
            }
            ExoPlaybackException.TYPE_REMOTE -> {
                LogUtil.errorLog(TAG, "TYPE_REMOTE: " + error.unexpectedException.message)
            }
        }
        LogUtil.errorLog(TAG, "Exiting onPlayerError() error: ${error.message}")
    }

    @SuppressLint("SimpleDateFormat")
    override fun onMetadata(eventTime: AnalyticsListener.EventTime, metadata: Metadata) {
        for (i in 0 until metadata.length()) {
            if (metadata[i].toString().contains("2020-")) {
                val sdf: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")
                val formattedDate: Date = sdf.parse(metadata[i].toString().split("value=")[1])
                val cal = Calendar.getInstance()
                cal.time = sdf.parse(metadata[i].toString().split("value=")[1])
                cal.add(Calendar.HOUR, -1)
                val oneHourBack = cal.time
                sdf.timeZone = TimeZone.getTimeZone("IST")
                mTimeLine.postValue(sdf.format(oneHourBack))
                Log.d(TAG, "onMetadata: ${sdf.format(formattedDate)}")
            }
        }
    }


    fun stop() {
        LogUtil.debugLog(TAG, "stop")
        mPlayer?.stop()
        mPlayer?.release()
    }

    fun goLive() {
        mPlayer?.seekTo(C.TIME_UNSET)
    }
}
