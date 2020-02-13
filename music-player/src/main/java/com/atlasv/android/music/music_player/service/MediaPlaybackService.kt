package com.atlasv.android.music.music_player.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.atlasv.android.music.music_player.AudioPlayer
import com.atlasv.android.music.music_player.R
import com.atlasv.android.music.music_player.exo.ExoPlayback
import com.atlasv.android.music.music_player.notification.BecomingNoisyReceiver
import com.atlasv.android.music.music_player.notification.ImageLoaderCallBack
import com.atlasv.android.music.music_player.notification.NOW_PLAYING_NOTIFICATION
import com.atlasv.android.music.music_player.notification.NotificationBuilder
import com.atlasv.android.music.music_player.playback.PlaybackState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Created by woyanan on 2020-02-10
 */
class MediaPlaybackService : MediaBrowserServiceCompat() {
    companion object {
        private const val BROWSABLE_ROOT = "/"
        private const val EMPTY_ROOT = "@empty@"
    }

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var packageValidator: PackageValidator
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationBuilder: NotificationBuilder
    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
    //UI可能被销毁,Service需要保存播放列表,并处理循环模式
    private val playList = arrayListOf<MediaSessionCompat.QueueItem>()
    private val playbackState = PlaybackState()
    private var isForegroundService = false

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(this, "MusicService")
            .apply {
                setSessionActivity(getPendingIntent())
                isActive = true
            }
        sessionToken = mediaSession.sessionToken
        mediaSession.setSessionActivity(getPendingIntent())
        mediaSession.setCallback(MediaSessionCallback(this, mediaSession, playList, playbackState))
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
        packageValidator = PackageValidator(this, R.xml.allowed_media_browser_callers)

        notificationBuilder = NotificationBuilder(this)
        notificationManager = NotificationManagerCompat.from(this)
        becomingNoisyReceiver =
            BecomingNoisyReceiver(context = this, sessionToken = mediaSession.sessionToken)
        observeNotificationVO()
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        return PendingIntent.getActivity(this, 0, intent, 0)
    }

    private fun observeNotificationVO() {
        AudioPlayer.getInstance(this).notificationVO.observeForever {
            serviceScope.launch {
                updateNotification(it.metadata, it.state)
            }
        }
    }

    private fun updateNotification(
        metadata: MediaMetadataCompat?,
        state: PlaybackStateCompat?
    ) {
        val updatedState = state?.state
        val notification = if (metadata != null
            && updatedState != PlaybackStateCompat.STATE_NONE
        ) {
            notificationBuilder.buildNotification(mediaSession.sessionToken,
                object : ImageLoaderCallBack {
                    override fun onBitmapLoaded(notification: Notification) {
                        notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)
                    }
                })
        } else {
            null
        }

        when (updatedState) {
            PlaybackStateCompat.STATE_BUFFERING,
            PlaybackStateCompat.STATE_PLAYING -> {
                becomingNoisyReceiver.register()
                if (notification != null) {
                    notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)

                    if (!isForegroundService) {
                        ContextCompat.startForegroundService(
                            applicationContext,
                            Intent(applicationContext, this@MediaPlaybackService.javaClass)
                        )
                        startForeground(NOW_PLAYING_NOTIFICATION, notification)
                        isForegroundService = true
                    }
                }
            }
            else -> {
                becomingNoisyReceiver.unregister()
                if (isForegroundService) {
                    stopForeground(false)
                    isForegroundService = false

                    // If playback has ended, also stop the service.
                    if (updatedState == PlaybackStateCompat.STATE_NONE) {
                        stopSelf()
                    }

                    if (notification != null) {
                        notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)
                    } else {
                        stopForeground(true)
                    }
                }
            }
        }
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()
        val list = playList.map {
            MediaBrowserCompat.MediaItem(
                it.description,
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
            )
        }
        result.sendResult(list as MutableList<MediaBrowserCompat.MediaItem>?)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        val isKnownCaller = packageValidator.isKnownCaller(clientPackageName, clientUid)
        return if (isKnownCaller) {
            BrowserRoot(BROWSABLE_ROOT, null)
        } else {
            BrowserRoot(EMPTY_ROOT, null)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        ExoPlayback.getInstance(this).release()
        mediaSession.isActive = false
        mediaSession.release()
    }
}