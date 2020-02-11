package com.atlasv.android.music.music_player

import android.content.Context
import com.atlasv.android.music.music_player.playback.IPlaybackManager
import com.atlasv.android.music.music_player.playback.PlaybackManager
import com.atlasv.android.music.music_player.player.ExoPlayback
import com.atlasv.android.music.music_player.player.Playback
import com.atlasv.android.music.music_player.service.IMediaConnection
import com.atlasv.android.music.music_player.service.MediaServiceConnection

/**
 * Created by woyanan on 2020-02-10
 */
class MusicPlayer(context: Context) {
    private val connection: IMediaConnection
    private val playback: Playback
    private val playbackManager: IPlaybackManager

    init {
        connection = MediaServiceConnection(context)
        playback = ExoPlayback(context)
        playbackManager = PlaybackManager(playback)
    }

    fun getPlaybackManager(): IPlaybackManager? {
        return playbackManager
    }

    fun onPlay(mediaId: String) {
        connection.getTransportControls()?.playFromMediaId(mediaId, null)
    }

    companion object {
        @Volatile
        private var instance: MusicPlayer? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: MusicPlayer(context).also { instance = it }
            }
    }
}