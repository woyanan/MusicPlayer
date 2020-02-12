package com.atlasv.android.music.music_player.playback

import android.support.v4.media.MediaDescriptionCompat

/**
 * Created by woyanan on 2020-02-11
 */
interface IPlaybackManager {

    /**
     * 播放
     */
    fun play(mediaResource: MediaDescriptionCompat, isPlayWhenReady: Boolean)

    /**
     * 暂停
     */
    fun pause()

    /**
     * 停止
     */
    fun stop(withError: String?)
}