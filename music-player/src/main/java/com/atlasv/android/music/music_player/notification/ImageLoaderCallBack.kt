package com.atlasv.android.music.music_player.notification

import android.app.Notification

/**
 * Created by woyanan on 2020-02-13
 */
interface ImageLoaderCallBack {
    fun onBitmapLoaded(notification: Notification)
}