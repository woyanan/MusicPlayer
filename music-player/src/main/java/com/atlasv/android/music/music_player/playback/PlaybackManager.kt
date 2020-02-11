package com.atlasv.android.music.music_player.playback

import com.atlasv.android.music.music_player.player.Playback
import com.atlasv.android.music.music_player.provider.MediaResource

/**
 * Created by woyanan on 2020-02-11
 */
class PlaybackManager constructor(
    private val playback: Playback
) : IPlaybackManager {

    override fun play(isPlayWhenReady: Boolean) {
        val mediaResource = MediaResource(
            "123",
            "http://audio04.dmhmusic.com/71_53_T10052953671_128_4_1_0_sdk-cpm/cn/0209/M00/E1/B8/ChR47F33J_yAHE_JACrgf2qqnyQ634.mp3?xcode=91ea4a9e9046987f5daae20ec2058044d218218"
        )
        playback.play(mediaResource, isPlayWhenReady)
    }

    override fun pause() {

    }

    override fun stop(withError: String?) {

    }
}