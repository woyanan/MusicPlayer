package com.atlasv.android.music.music_player

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.atlasv.android.music.music_player.exo.ExoPlayback
import com.atlasv.android.music.music_player.service.MediaServiceConnection

/**
 * Created by woyanan on 2020-02-10
 */
class AudioPlayer(val context: Context) {
    private lateinit var connection: MediaServiceConnection
    private val playback = ExoPlayback.getInstance(context)

    fun init() {
        connection = MediaServiceConnection(context)
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
        connection.isConnected.observe(owner, Observer<Boolean> {
            if (it) {
                println("--------------------->observe connected")
                addQueueItem(playList)
            }
        })
    }

    fun getPlaybackState(): MutableLiveData<PlaybackStateCompat>? {
        return connection.playbackState
    }

    private fun addQueueItem(playList: ArrayList<MediaMetadataCompat>) {
        playList.forEach {
            connection.mediaController?.addQueueItem(it.description)
        }
    }

    fun onPlayFromMediaId(mediaId: String) {
        connection.transportControls?.playFromMediaId(mediaId, null)
    }

    fun onSkipToPrevious() {
        connection.transportControls?.skipToPrevious()
    }

    fun onSkipToNext() {
        connection.transportControls?.skipToNext()
    }

    fun onSeekTo(progress: Int) {
        connection?.transportControls?.seekTo(progress.toLong())
    }

    fun getCurrentStreamPosition(): Long {
        return playback.currentStreamPosition
    }

    fun getBufferedPosition(): Long {
        return playback.bufferedPosition
    }

    fun getDuration(): Long {
        return playback.duration
    }

    fun onPause() {
        println("--------------------->state: " + connection.playbackState.value?.state)
        if (connection.playbackState.value?.state == PlaybackStateCompat.STATE_PLAYING) {
            connection.transportControls?.pause()
        } else {
            connection.transportControls?.play()
        }
    }

    fun onStop() {
        connection.transportControls?.stop()
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