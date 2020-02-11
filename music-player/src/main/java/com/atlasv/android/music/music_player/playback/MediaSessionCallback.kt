package com.atlasv.android.music.music_player.playback

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat

/**
 * Created by woyanan on 2020-02-11
 */
class MediaSessionCallback(private val playbackManager: IPlaybackManager?) :
    MediaSessionCompat.Callback() {

    override fun onPrepare() {
        super.onPrepare()
    }

    override fun onPlay() {
        super.onPlay()
        println("------------------->onPlay: ")
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
        super.onPlayFromUri(uri, extras)
    }

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        super.onPlayFromMediaId(mediaId, extras)
        println("------------------->mediaId: $mediaId")
        mediaId?.apply {
            playbackManager?.play(true)
        }
    }
}