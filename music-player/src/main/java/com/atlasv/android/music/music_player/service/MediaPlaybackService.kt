package com.atlasv.android.music.music_player.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.atlasv.android.music.music_player.R
import com.atlasv.android.music.music_player.playback.PlaybackState

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
    //UI可能被销毁,Service需要保存播放列表,并处理循环模式
    private val playList = arrayListOf<MediaSessionCompat.QueueItem>()
    private val playbackState = PlaybackState()

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
        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                    or MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
                    or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )

        packageValidator = PackageValidator(this, R.xml.allowed_media_browser_callers)
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        return PendingIntent.getActivity(this, 0, intent, 0)
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}