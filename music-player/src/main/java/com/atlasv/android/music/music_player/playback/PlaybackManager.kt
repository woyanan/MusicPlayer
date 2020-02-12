package com.atlasv.android.music.music_player.playback

import android.support.v4.media.MediaDescriptionCompat
import com.atlasv.android.music.music_player.player.Playback

/**
 * Created by woyanan on 2020-02-11
 */
class PlaybackManager constructor(
    private val playback: Playback
) : IPlaybackManager {

    override fun play(mediaResource: MediaDescriptionCompat, isPlayWhenReady: Boolean) {
        playback.play(mediaResource, isPlayWhenReady)
    }

    override fun pause() {

    }

    override fun stop(withError: String?) {

    }
}