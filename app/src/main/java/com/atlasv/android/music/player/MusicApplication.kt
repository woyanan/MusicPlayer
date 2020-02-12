package com.atlasv.android.music.player

import android.app.Application
import com.atlasv.android.music.music_player.AudioPlayer

/**
 * Created by woyanan on 2020-02-12
 */
class MusicApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AudioPlayer.getInstance(this).init()
    }
}