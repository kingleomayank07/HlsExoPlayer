package com.naseeb.exoplayer

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ui.PlayerView

//TODO need to move getInstance() method to some other class. Here only player related method will be there
interface IPlayer {

    interface PlayerCallback {
        fun onBufferingEnded()
        fun onBufferingStarted()
        fun onPlayEnded()
        fun onMediaDurationFetched(videoDuration: Long)
        fun onPlayerNetworkError() { /* default implementation */
        }
    }

    companion object {
        fun getInstance(
            context: Context,
            uri: Uri,
            playerView: PlayerView,
            playerCallback: PlayerCallback?,
        ): IPlayer = PlayerImpl(context, uri, playerView, playerCallback)
    }

    fun play()
    fun pause()
    fun resume()
    fun seekTo(position: Long)
    fun release()
}