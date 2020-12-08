package com.naseeb.exoplayer

import android.content.Context
import android.net.Uri
import android.os.Handler
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.naseeb.log.LogUtil

class PlayerImpl(
    private val context: Context,
    private val uri: Uri,
    private val playerView: PlayerView,
    private val playerCallback: IPlayer.PlayerCallback?
) : IPlayer,
    Player.EventListener, AnalyticsListener, MediaSourceEventListener {

    private val TAG = PlayerImpl::class.java.canonicalName
    private var mPlayer: SimpleExoPlayer? = null
    private var mToken: Int? = null
    private var mMediaSource: MediaSource? = null
    private var mSimpleCache: SimpleCache? = null
    private var mIsStartedPlayingFirstTime: Boolean = true

    override fun pause() {
        LogUtil.debugLog(TAG, "pause")
        stopPlayer(false)
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
        mMediaSource!!.addEventListener(Handler(), this)
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
                /*HlsMediaSource.Factory(buildDataSourceFactory()).createMediaSource(uri)*/
                HlsMediaSource.Factory(buildDataSourceFactory())
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(
                        MediaItem.Builder()
                            .setUri(uri)
                            .build()
                    )
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
        return DefaultDataSourceFactory(
            context, DefaultHttpDataSourceFactory("app_name")
        )
    }

    @Override
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        LogUtil.debugLog(
            TAG, "PlayerEventListener onPlayerStateChanged playWhenReady : " +
                    playWhenReady + " playbackState : " + playbackState + " "
        )
        when (playbackState) {
            Player.STATE_READY -> {
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
        // super.onPlayerError(error)
        LogUtil.errorLog(TAG, "Entering onPlayerError() error: ${error.message}")
        when (error.type) {
            ExoPlaybackException.TYPE_SOURCE -> {
                LogUtil.errorLog(TAG, "TYPE_SOURCE: " + error.sourceException.message)
                playerCallback?.onPlayerNetworkError()
            }
            ExoPlaybackException.TYPE_RENDERER -> LogUtil.errorLog(
                TAG, "TYPE_RENDERER: " +
                        error.rendererException.message
            )
            ExoPlaybackException.TYPE_UNEXPECTED -> LogUtil.errorLog(
                TAG, "TYPE_UNEXPECTED: " +
                        error.unexpectedException.message
            )
            ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
                LogUtil.errorLog(
                    TAG, "TYPE_OUT_OF_MEMORY: " +
                            error.unexpectedException.message
                )
            }
            ExoPlaybackException.TYPE_REMOTE -> {
                LogUtil.errorLog(
                    TAG, "TYPE_REMOTE: " +
                            error.unexpectedException.message
                )
            }
            ExoPlaybackException.TYPE_TIMEOUT -> {
                LogUtil.errorLog(
                    TAG, "TYPE_TIMEOUT: " +
                            error.unexpectedException.message
                )
            }
        }

        LogUtil.errorLog(TAG, "Exiting onPlayerError() error: ${error.message}")
    }

    fun stop() {
        mPlayer!!.playWhenReady = false
        mPlayer!!.stop()
        mPlayer!!.release()
    }
}