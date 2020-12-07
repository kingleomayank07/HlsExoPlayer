package com.example.samplehlsplayer

import com.google.gson.annotations.SerializedName

data class TwitchStreamResponse(
    @SerializedName("success")
    val success: Boolean?,
    @SerializedName("urls")
    val urls: TwitchUrl?
)

data class TwitchUrl(
    @SerializedName("audio_only")
    val audio_only: String?,
    @SerializedName("160p")
    val Q160p: String?,
    @SerializedName("360p")
    val Q360p: String?,
    @SerializedName("480p")
    val Q480p: String?,
    @SerializedName("720p")
    val Q720p: String?,
    @SerializedName("1080p")
    val Q1080p: String?,
    @SerializedName("1440p")
    val Q1440p: String?,
)