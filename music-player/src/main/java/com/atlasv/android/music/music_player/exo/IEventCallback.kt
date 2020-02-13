package com.atlasv.android.music.music_player.exo

/**
 * Created by woyanan on 2020-02-13
 */
interface IEventCallback {
    fun onPlaybackStatusChanged(state: Int)

    fun onCompletion()

    fun onError()
}