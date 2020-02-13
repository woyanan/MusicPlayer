package com.atlasv.android.music.music_player

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.atlasv.android.music.music_player.exo.ExoPlayback
import com.atlasv.android.music.music_player.service.MediaPlaybackService

/**
 * Created by woyanan on 2020-02-10
 */
class AudioPlayer(private val context: Context) {
    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var mediaController: MediaControllerCompat
    private val transportControls: MediaControllerCompat.TransportControls?
        get() = mediaController.transportControls
    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val playback = ExoPlayback.getInstance(context)
    //是否连接Service成功
    val isConnected = MutableLiveData<Boolean>()
        .apply { postValue(false) }
    //播放状态
    val playbackState = MutableLiveData<PlaybackStateCompat>()
    //Service数据订阅成功后，返回给UI的数据列表
    var playList = MutableLiveData<MutableList<MediaDescriptionCompat>>()
    //当前播放数据
    var currentMetaData = MutableLiveData<MediaMetadataCompat>()

    fun init() {
        mediaBrowser = MediaBrowserCompat(
            context,
            ComponentName(context, MediaPlaybackService::class.java),
            mediaBrowserConnectionCallback, null
        ).apply { connect() }
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            transportControls?.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
            isConnected.postValue(true)
        }

        override fun onConnectionSuspended() {
            isConnected.postValue(false)
        }

        override fun onConnectionFailed() {
            isConnected.postValue(false)
        }
    }

    fun setData(owner: LifecycleOwner, playList: ArrayList<MediaMetadataCompat>) {
        if (isConnected.value == true) {
            addQueueItem(playList)
            subscribe()
        } else {
            isConnected.observe(owner, Observer<Boolean> {
                if (it) {
                    addQueueItem(playList)
                    subscribe()
                }
            })
        }
    }

    private fun subscribe() {
        mediaBrowser.subscribe(
            mediaBrowser.root,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    playList.postValue(children.map { mediaItem ->
                        mediaItem.description
                    } as MutableList<MediaDescriptionCompat>)
                }
            })
    }

    fun unsubscribe(callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(mediaBrowser.root, callback)
    }

    private fun addQueueItem(playList: ArrayList<MediaMetadataCompat>) {
        playList.forEach {
            mediaController.addQueueItem(it.description)
        }
    }

    fun onPlayFromMediaId(mediaId: String) {
        transportControls?.playFromMediaId(mediaId, null)
    }

    fun onSkipToPrevious() {
        transportControls?.skipToPrevious()
    }

    fun onSkipToNext() {
        transportControls?.skipToNext()
    }

    fun onSeekTo(progress: Int) {
        transportControls?.seekTo(progress.toLong())
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
        if (playbackState.value?.state == PlaybackStateCompat.STATE_PLAYING) {
            transportControls?.pause()
        } else {
            transportControls?.play()
        }
    }

    fun onStop() {
        transportControls?.stop()
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            currentMetaData.postValue(metadata)
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            playList.postValue(queue?.map { it.description } as MutableList<MediaDescriptionCompat>)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
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