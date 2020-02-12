package com.atlasv.android.music.music_player

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.atlasv.android.music.music_player.playback.IPlaybackManager
import com.atlasv.android.music.music_player.playback.PlaybackManager
import com.atlasv.android.music.music_player.player.ExoPlayback
import com.atlasv.android.music.music_player.player.Playback
import com.atlasv.android.music.music_player.service.MediaServiceConnection

/**
 * Created by woyanan on 2020-02-10
 */
class AudioPlayer(context: Context) {
    private val connection: MediaServiceConnection?
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

    fun setData(owner: LifecycleOwner, playList: ArrayList<MediaMetadataCompat>) {
//        connection?.let {
//            Transformations.map(it.isConnected) { isConnected ->
//                if (isConnected) {
//                    println("--------------------->connected")
//                    addQueueItem(playList)
//                } else {
//                    println("--------------------->observe")
//                    observe(owner, playList)
//                }
//            }
//        }
        observe(owner, playList)
    }

    private fun observe(owner: LifecycleOwner, playList: ArrayList<MediaMetadataCompat>) {
        connection?.isConnected?.observe(owner, Observer<Boolean> {
            if (it) {
                println("--------------------->observe connected")
                addQueueItem(playList)
            }
        })
    }

    private fun addQueueItem(playList: ArrayList<MediaMetadataCompat>) {
        playList.forEach {
            connection?.mediaController?.addQueueItem(it.description)
        }
    }

    fun onPlay(mediaId: String) {
        connection?.transportControls?.playFromMediaId(mediaId, null)
    }

    companion object {
        @Volatile
        private var instance: AudioPlayer? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: AudioPlayer(context).also { instance = it }
            }
    }
}