package com.atlasv.android.music.music_player.player

import com.atlasv.android.music.music_player.provider.MediaResource

/**
 * Created by woyanan on 2020-02-10
 */
interface Playback {
    var state: Int
    val isConnected: Boolean
    val isPlaying: Boolean
    val currentStreamPosition: Long
    val bufferedPosition: Long
    val duration: Long
    var currentMediaId: String
    var volume: Float

    fun getAudioSessionId(): Int

    fun stop(notifyListeners: Boolean)

    fun play(mediaResource: MediaResource, isPlayWhenReady: Boolean)

    fun pause()

    fun seekTo(position: Long)

    fun setCallback(callback: PlaybackCallback)
}
