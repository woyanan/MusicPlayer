package com.atlasv.android.music.music_player.service

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

/**
 * Created by woyanan on 2020-02-11
 */
class MediaServiceConnection(context: Context) : IMediaConnection {
    private lateinit var mediaController: MediaControllerCompat
    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, MediaPlaybackService::class.java),
        mediaBrowserConnectionCallback, null
    ).apply { connect() }
    private var transportControls: MediaControllerCompat.TransportControls? = null

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            try {
                mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                    registerCallback(MediaControllerCallback())
                }
                transportControls = mediaController.transportControls
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onConnectionSuspended() {

        }

        override fun onConnectionFailed() {

        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

    override fun getTransportControls(): MediaControllerCompat.TransportControls? {
        return transportControls
    }
}