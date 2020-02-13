package com.atlasv.android.music.music_player.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.atlasv.android.music.music_player.R
import com.atlasv.android.music.music_player.extensions.isPlayEnabled
import com.atlasv.android.music.music_player.extensions.isPlaying
import com.atlasv.android.music.music_player.extensions.isSkipToNextEnabled
import com.atlasv.android.music.music_player.extensions.isSkipToPreviousEnabled
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

/**
 * Created by woyanan on 2020-02-13
 */

const val NOW_PLAYING_CHANNEL: String = "com.atlasv.android.music.music_player.NOW_PLAYING"
const val NOW_PLAYING_NOTIFICATION: Int = 0xb339

class NotificationBuilder(private val context: Context) {
    private val platformNotificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val skipToPreviousAction = NotificationCompat.Action(
        R.drawable.ic_skip_previous,
        context.getString(R.string.label_previous),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
    )
    private val playAction = NotificationCompat.Action(
        R.drawable.ic_play,
        context.getString(R.string.label_play),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_PLAY
        )
    )
    private val pauseAction = NotificationCompat.Action(
        R.drawable.ic_pause,
        context.getString(R.string.label_pause),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_PAUSE
        )
    )
    private val skipToNextAction = NotificationCompat.Action(
        R.drawable.ic_skip_next,
        context.getString(R.string.label_next),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )
    )
    private val stopPendingIntent =
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_STOP
        )

    fun buildNotification(
        sessionToken: MediaSessionCompat.Token,
        callBack: ImageLoaderCallBack?
    ): Notification {
        if (shouldCreateNowPlayingChannel()) {
            createNowPlayingChannel()
        }

        val controller = MediaControllerCompat(context, sessionToken)
        val description = controller.metadata.description
        val playbackState = controller.playbackState

        val builder = NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL)

        var playPauseIndex = 0
        if (playbackState.isSkipToPreviousEnabled) {
            builder.addAction(skipToPreviousAction)
            ++playPauseIndex
        }
        if (playbackState.isPlaying) {
            builder.addAction(pauseAction)
        } else if (playbackState.isPlayEnabled) {
            builder.addAction(playAction)
        }
        if (playbackState.isSkipToNextEnabled) {
            builder.addAction(skipToNextAction)
        }

        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setCancelButtonIntent(stopPendingIntent)
            .setMediaSession(sessionToken)
            .setShowActionsInCompactView(playPauseIndex)
            .setShowCancelButton(true)

        builder.setContentIntent(controller.sessionActivity)
            .setContentText(description.subtitle)
            .setContentTitle(description.title)
            .setDeleteIntent(stopPendingIntent)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(mediaStyle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        description.iconUri?.let {
            resolveUriAsBitmap(it, builder, callBack)
        }
        return builder.build()
    }

    private fun shouldCreateNowPlayingChannel() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nowPlayingChannelExists() =
        platformNotificationManager.getNotificationChannel(NOW_PLAYING_CHANNEL) != null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNowPlayingChannel() {
        val notificationChannel = NotificationChannel(
            NOW_PLAYING_CHANNEL,
            context.getString(R.string.notification_channel),
            NotificationManager.IMPORTANCE_LOW
        )
            .apply {
                description = context.getString(R.string.notification_channel_description)
            }

        platformNotificationManager.createNotificationChannel(notificationChannel)
    }

    private fun resolveUriAsBitmap(
        uri: Uri,
        builder: NotificationCompat.Builder,
        callBack: ImageLoaderCallBack?
    ) {
        Glide.with(context).asBitmap().load(uri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    callBack?.onBitmapLoaded(builder.setLargeIcon(resource).build())
                }
            })
    }
}