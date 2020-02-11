package com.atlasv.android.music.music_player.playback

/**
 * Created by woyanan on 2020-02-11
 */
interface IPlaybackManager {

    /**
     * 播放
     */
    fun play(isPlayWhenReady: Boolean)

    /**
     * 暂停
     */
    fun pause()

    /**
     * 停止
     */
    fun stop(withError: String?)
}