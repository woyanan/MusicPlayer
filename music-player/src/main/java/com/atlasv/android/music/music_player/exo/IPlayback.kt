package com.atlasv.android.music.music_player.exo

import android.support.v4.media.MediaDescriptionCompat

/**
 * Created by woyanan on 2020-02-10
 */
interface IPlayback {
    var state: Int
    val isPlaying: Boolean
    val currentStreamPosition: Long
    val bufferedPosition: Long
    val duration: Long

    fun getAudioSessionId(): Int

    fun play(mediaResource: MediaDescriptionCompat, isPlayWhenReady: Boolean)

    fun seekTo(position: Long)

    fun pause()

    fun stop()
}
