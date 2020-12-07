package com.example.samplehlsplayer.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.samplehlsplayer.R
import com.example.samplehlsplayer.api.RetrofitClient
import com.google.android.exoplayer2.RendererCapabilities
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import com.naseeb.exoplayer.IPlayer
import com.naseeb.exoplayer.PlayerImpl
import kotlinx.android.synthetic.main.activity_twitch_activty.*
import kotlinx.android.synthetic.main.ui_exoplayer.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException


class TwitchActivity : AppCompatActivity(), IPlayer.PlayerCallback {

    //region variables
    private lateinit var trackSelector: TrackSelector
    private var count = 0
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_twitch_activty)

        //region getTwitch Token
        CoroutineScope(Dispatchers.Main).launch {
            getTwitchToken()
        }
        //endregion

        exo_settings.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            customTrackSelection(trackSelector as DefaultTrackSelector, popupMenu)
        }
    }

    private fun customTrackSelection(trackSelector: DefaultTrackSelector, popupMenu: PopupMenu) {
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo
        if (mappedTrackInfo != null) {
            val trackGroupArray = mappedTrackInfo!!.getTrackGroups(0)
            for (groupIndex in 0 until trackGroupArray.length) {
                for (trackIndex in 0 until trackGroupArray.get(groupIndex).length) {
                    val trackName = DefaultTrackNameProvider(resources).getTrackName(
                        trackGroupArray.get(groupIndex).getFormat(trackIndex)
                    )
                    val rendererIndex = 0
                    val isTrackSupported = mappedTrackInfo.getTrackSupport(
                        rendererIndex,
                        groupIndex,
                        trackIndex
                    ) == RendererCapabilities.FORMAT_HANDLED
                    popupMenu.menu.add(trackName)
                    popupMenu.show()
                }
            }
        } else {
            Toast.makeText(this, "Unexpected error occurred!", Toast.LENGTH_SHORT).show()
            return
        }

        popupMenu.setOnMenuItemClickListener {
            val maxWidth = it.title
            val getTitle = maxWidth.split(",").toTypedArray()
            val getHeightWidth = getTitle[0].split("×").toTypedArray()
            val builder = trackSelector.buildUponParameters()
                .setMaxVideoSize(
                    getHeightWidth[0].trim().toInt(),
                    getHeightWidth[1].trim().toInt()
                )
            trackSelector.setParameters(builder)
            true
        }
    }


    private suspend fun getTwitchToken() {
        try {
            val response = RetrofitClient.instance.getToken(
                "kimne78kx3ncx6brgo4mv6wki5h1ko",
                "karlaplan"
            )
            val jsonObject = JSONObject(response.string())
            CoroutineScope(Dispatchers.Main).launch {
                getTwitchStreams(jsonObject)
            }
        } catch (e: IOException) {
            count++
            if (count <= 5) {
                getTwitchToken()
            } else {
                Toast.makeText(this, "Stream offline.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getTwitchStreams(jsonObject: JSONObject?) {
        if (jsonObject != null) {
            val url =
                "http://usher.twitch.tv/api/channel/hls/karlaplan.m3u8?player=twitchweb&token=${
                    jsonObject.getString("token")
                }&sig=${jsonObject.getString("sig")}&allow_audio_only=true&allow_source=true&type=any&p=39114"

            val player = PlayerImpl(this, Uri.parse(url), media_container, this)
            player.play()
            trackSelector = player.getTrackSelector()!!
            exo_settings.isClickable = true
        }
    }

    override fun onBufferingEnded() {
    }

    override fun onBufferingStarted() {
    }

    override fun onPlayEnded() {
    }

    override fun onMediaDurationFetched(videoDuration: Long) {
        Log.d("TAG", "onMediaDurationFetched: $videoDuration")
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
     // but some examples allow empty lines before the tag.
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

