package com.example.samplehlsplayer.ui

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.samplehlsplayer.Coroutines
import com.example.samplehlsplayer.R
import com.example.samplehlsplayer.api.RetrofitClient
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import com.naseeb.exoplayer.IPlayer
import com.naseeb.exoplayer.PlayerImpl
import kotlinx.android.synthetic.main.activity_twitch_activty.*
import kotlinx.android.synthetic.main.ui_exoplayer.*
import org.json.JSONObject
import java.io.IOException


class TwitchActivity : AppCompatActivity(), IPlayer.PlayerCallback {

    //region variables
    private lateinit var trackSelector: TrackSelector
    private var count = 0
    private val TAG = TwitchActivity::class.simpleName
    private var playerInstance: PlayerImpl? = null
    private var mChannelName: String? = null
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_twitch_activty)

        exo_pause.setOnClickListener {
            playerInstance?.pause()
            exo_pause.visibility = View.GONE
            exo_play.visibility = View.VISIBLE
        }

        exo_forward.setOnClickListener {
            playerInstance?.pause()
            exo_pause.visibility = View.VISIBLE
            exo_play.visibility = View.GONE
        }

        exo_settings.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.menu.add("auto")
            customTrackSelection(trackSelector as DefaultTrackSelector, popupMenu)
        }

        exo_fullscreen.setOnClickListener {
            setFullScreen()
        }

        search.setOnClickListener {
            mChannelName = channelName.text.toString()
            if (mChannelName.isNullOrEmpty()) {
                Toast.makeText(this, "Enter channel name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                playerInstance?.stop()
                Coroutines.io {
                    getTwitchToken(mChannelName)
                }
            }
        }

    }

    private fun setFullScreen() {
        requestedOrientation = when (requestedOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                search.visibility = View.VISIBLE
                channelName.visibility = View.VISIBLE
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            else -> {
                search.visibility = View.GONE
                channelName.visibility = View.GONE
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
        when (requestedOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val controller = window.insetsController
                    controller?.hide(WindowInsets.Type.statusBars())
                    val params = player_view.layoutParams
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT
                    player_view.layoutParams = params
                } else {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                    window.decorView.systemUiVisibility =
                        (View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                    val params = player_view.layoutParams
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT
                    player_view.layoutParams = params
                }
            }
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val controller = window.insetsController
                    controller?.hide(WindowInsets.Type.statusBars())
                    val params = player_view.layoutParams
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = 800
                    player_view.layoutParams = params
                } else {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                    window.decorView.systemUiVisibility =
                        (View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                    val params = player_view.layoutParams
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = 800
                    player_view.layoutParams = params
                }
            }
        }
    }

    private fun hideStatusBar() {
        when (requestedOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val controller = window.insetsController
                    controller?.hide(WindowInsets.Type.statusBars())
                    val params = player_view.layoutParams
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT
                    player_view.layoutParams = params
                } else {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                    window.decorView.systemUiVisibility =
                        (View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                    val params = player_view.layoutParams
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT
                    player_view.layoutParams = params
                }
            }
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val controller = window.insetsController
                    controller?.hide(WindowInsets.Type.statusBars())
                    val params = player_view.layoutParams
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = 800
                    player_view.layoutParams = params
                } else {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                    window.decorView.systemUiVisibility =
                        (View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                    val params = player_view.layoutParams
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = 800
                    player_view.layoutParams = params
                }
            }
        }
    }

    private fun customTrackSelection(trackSelector: DefaultTrackSelector, popupMenu: PopupMenu) {
        getTrackNames(trackSelector, popupMenu)
        popupMenu.setOnMenuItemClickListener {
            val maxWidth = it.title
            if (maxWidth != "auto") {
                val getTitle = maxWidth.split(",").toTypedArray()
                val getHeightWidth = getTitle[0].split("×").toTypedArray()
                val params = trackSelector.buildUponParameters()
                    .setMaxVideoSize(
                        getHeightWidth[0].trim().toInt(),
                        getHeightWidth[1].trim().toInt()
                    )
                hideStatusBar()
                trackSelector.setParameters(params)
                playerInstance?.play()
            } else {
                val builder = trackSelector.buildUponParameters().clearVideoSizeConstraints()
                trackSelector.setParameters(builder)
                hideStatusBar()
            }
            true
        }
    }

    private fun getTrackNames(trackSelector: DefaultTrackSelector, popupMenu: PopupMenu) {
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo
        if (mappedTrackInfo != null) {
            val trackGroupArray = mappedTrackInfo.getTrackGroups(0)
            for (groupIndex in 0 until trackGroupArray.length) {
                for (trackIndex in 0 until trackGroupArray.get(groupIndex).length) {
                    val trackName = DefaultTrackNameProvider(resources).getTrackName(
                        trackGroupArray.get(groupIndex).getFormat(trackIndex)
                    )
//                    val rendererIndex = 0
                    /* val isTrackSupported = mappedTrackInfo.getTrackSupport(
                         rendererIndex,
                         groupIndex,
                         trackIndex
                     ) == RendererCapabilities.FORMAT_HANDLED*/
                    popupMenu.menu.add(trackName)
                    popupMenu.show()
                }
            }
        } else {
            Toast.makeText(this, "Unexpected error occurred!", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private suspend fun getTwitchToken(mChannelName: String?) {
        try {
            val response = RetrofitClient.instance.getToken(
                "kimne78kx3ncx6brgo4mv6wki5h1ko",
                mChannelName!!
            )
            val streams = JSONObject(response.string())
            Coroutines.main {
                getTwitchStreams(streams, mChannelName)
            }
        } catch (e: IOException) {
            count++
            if (count <= 5) {
                getTwitchToken(this.mChannelName)
            } else {
                Coroutines.main {
                    Toast.makeText(this, "Stream offline.", Toast.LENGTH_SHORT).show()
                }
                Coroutines.mJob.cancel()
            }
        }
    }

    private fun getTwitchStreams(response: JSONObject?, mChannelName: String) {
        if (response != null) {
            val random = (10000..99999).random()
            val url =
                "http://usher.twitch.tv/api/channel/hls/${mChannelName}.m3u8?player=twitchweb&token=${
                    response.getString("token")
                }&sig=${
                    response.getString("sig")
                }&allow_audio_only=false&allow_source=true&type=any&p=$random"



            Log.d("TAG", "getTwitchStreams: $url")

            playerInstance = PlayerImpl(
                context = applicationContext,
                uri = Uri.parse(url),
                playerView = player_view,
                playerCallback = this,
            )

            playerInstance!!.play()
            trackSelector = playerInstance!!.getTrackSelector()!!
            exo_settings.isClickable = true

        }
    }

    override fun onPause() {
        super.onPause()
        if (playerInstance != null) {
            playerInstance!!.stop()
        }
    }

    override fun onBufferingEnded() {
        exo_progress.visibility = View.GONE
        pg.visibility = View.INVISIBLE
        Log.d(TAG, "onBufferingEnded")
    }

    override fun onBufferingStarted() {
        pg.visibility = View.VISIBLE
        Log.d(TAG, "onBufferingStarted")
    }

    override fun onPlayEnded() {
        Log.d(TAG, "onPlayEnded")
    }

    override fun onMediaDurationFetched(videoDuration: Long) {
        Log.d(TAG, "onMediaDurationFetched: $videoDuration")
    }

    /* private fun getPlayList(response: ResponseBody) {
    val b = M3UParser().readPlaylist(response.string())
    val qualityList = readPlaylist(b)
 }*/

    /*//    private fun parsePlayList(hlsMediaSource: HlsMediaSource) {
//        trackSelector = DefaultTrackSelector(this)
//
//        trackSelector.setParameters(
//            trackSelector.buildUponParameters()
//                .setMaxVideoSize(1920, 1080)
//        )
//
//        val loadControl = DefaultLoadControl.Builder()
//            .setBufferDurationsMs(
//                MIN_BUFFER, MAX_BUFFER,
//                BUFFER_PLAYBACK, BUFFER_PLAYBACK_RE_BUFFER
//            )
//            .setBackBuffer(BACK_BUFFER_PLAYBACK, true)
//            .setPrioritizeTimeOverSizeThresholds(true)
//            .build()
//
//        mVideoPlayer = SimpleExoPlayer.Builder(this)
//            .experimentalSetThrowWhenStuckBuffering(true)
//            .setLoadControl(loadControl)
//            .setTrackSelector(trackSelector)
//            .setBandwidthMeter(
//                DefaultBandwidthMeter.Builder(this)
//                    .build()
//            )
//            .setHandleAudioBecomingNoisy(true)
//            .build()
//
//        mVideoSurfaceView = PlayerView(this)
//        mVideoSurfaceView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//        mVideoSurfaceView!!.useController = false
//        media_container!!.player = mVideoPlayer
//        mVideoSurfaceView!!.requestFocus()
//        mVideoSurfaceView!!.visibility = View.VISIBLE
//
//        mVideoPlayer.setMediaSource(hlsMediaSource)
//        mVideoPlayer.prepare()
//        mVideoPlayer.playWhenReady = true
//    }*/

    /* private fun readPlaylist(lineIterator: Iterator<String>): ArrayList<Quality> {
//but some examples allow empty lines before the tag.
var extM3uFound = false
while (lineIterator.hasNext() && !extM3uFound) {
 val line = lineIterator.next()
 if (EXTM3U == line) {
     extM3uFound = true
 } else if (!line.isEmpty()) {
     break // invalid line  found
 }
 // else: line is empty
}
if (!extM3uFound) {
 Toast.makeText(this, "Invalid playlist. Expected #EXTM3U.", Toast.LENGTH_SHORT).show()
}
while (lineIterator.hasNext()) {
 val line = lineIterator.next()
 if (line.startsWith("#EXT")) {
     val colonPosition = line.indexOf(':')
     val prefix =
         if (colonPosition > 0) line.substring(1, colonPosition) else line.substring(1)
     val attributes = if (colonPosition > 0) line.substring(colonPosition + 1) else ""
     if (attributes.contains("BANDWIDTH")) {
         mQualityName.add(attributes)
     }
 } else if (!(line.startsWith("#") || line.isEmpty())) {
     mList.add(line)
 }
}

val qualityList = ArrayList<Quality>()
qualityList.clear()

for (j in 0 until (mList.size)) {
 Log.d("TAG", "readPlaylist: ${mQualityName[j].split(",")[1] + " : " + mList[j]}")
 val quality = Quality(mQualityName[j].split(",")[1], mList[j])
 qualityList.add(quality)
}
return qualityList
}
*/

}
