package com.atlasv.android.music.music_player

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.atlasv.android.music.music_player.service.MediaServiceConnection

/**
 * Created by woyanan on 2020-02-10
 */
class AudioPlayer(context: Context) {
    private val connection: MediaServiceConnection?

    init {
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

    fun onPlayFromMediaId(mediaId: String) {
        connection?.transportControls?.playFromMediaId(mediaId, null)
    }

    fun onSkipToPrevious() {
        connection?.transportControls?.skipToPrevious()
    }

    fun onSkipToNext() {
        connection?.transportControls?.skipToNext()
    }

    fun onPause() {
//        if (mPlayStateLiveData.value?.state == PlaybackStateCompat.STATE_PLAYING) {
//            mMediaControllerCompat?.transportControls?.pause()
//        } else {
//            mMediaControllerCompat?.transportControls?.play()
//        }
    }

    fun onSeekTo(progress: Int) {
        connection?.transportControls?.seekTo(progress.toLong())
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