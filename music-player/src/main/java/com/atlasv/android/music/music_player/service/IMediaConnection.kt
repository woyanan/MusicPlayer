package com.atlasv.android.music.music_player.service

import android.support.v4.media.session.MediaControllerCompat

/**
 * Created by woyanan on 2020-02-11
 */
interface IMediaConnection {
    /**
     * 获取播放控制器
     */
    fun getTransportControls(): MediaControllerCompat.TransportControls?
}