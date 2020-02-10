package com.atlasv.android.music.music_player.player

/**
 * Created by woyanan on 2020-02-10
 */
interface PlaybackCallback {
    fun onCompletion()

    fun onPlaybackStatusChanged(state: Int)

    fun onError(error: String)

    fun setCurrentMediaId(mediaId: String)
}