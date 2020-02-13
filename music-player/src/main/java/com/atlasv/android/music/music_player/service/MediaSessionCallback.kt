package com.atlasv.android.music.music_player.service

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.atlasv.android.music.music_player.exo.ExoPlayback
import com.atlasv.android.music.music_player.playback.PlaybackState
import java.util.*

/**
 * Created by woyanan on 2020-02-12
 */
class MediaSessionCallback(
    context: Context,
    private val mediaSession: MediaSessionCompat,
    private val playList: ArrayList<MediaSessionCompat.QueueItem>,
    private val playbackState: PlaybackState
) : MediaSessionCompat.Callback() {

    private var currentPosition: Int = 0
    private val playback = ExoPlayback.getInstance(context)

    override fun onPlay() {
        super.onPlay()
        playback.play(playList[currentPosition].description, true)
        playbackState.setState(
            PlaybackStateCompat.STATE_PLAYING,
            playback.currentStreamPosition, mediaSession
        )
    }

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        super.onPlayFromMediaId(mediaId, extras)
        mediaId?.apply {
            playList.find { mediaId == it.description.mediaId }?.let {
                playback.play(it.description, true)
                currentPosition = playList.indexOf(it)
                playbackState.setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    playback.currentStreamPosition, mediaSession
                )
                setMetadata(it.description)
            }
        }
    }

    override fun onAddQueueItem(description: MediaDescriptionCompat?) {
        super.onAddQueueItem(description)
        if (playList.find { it.description.mediaId == description?.mediaId } == null) {
            playList.add(
                MediaSessionCompat.QueueItem(description, description.hashCode().toLong())
            )
        }
        mediaSession.setQueue(playList)
    }

    override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
        super.onRemoveQueueItem(description)
        playList.remove(
            MediaSessionCompat.QueueItem(description, description.hashCode().toLong())
        )
        mediaSession.setQueue(playList)
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        currentPosition = if (currentPosition > 0) currentPosition - 1 else playList.size - 1
        val description = playList[currentPosition].description
        playback.play(description, true)
        setMetadata(description)
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        currentPosition = (++currentPosition % playList.size)
        val description = playList[currentPosition].description
        playback.play(description, true)
        setMetadata(description)
    }

    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)
        playback.seekTo(pos)
    }

    override fun onPause() {
        super.onPause()
        playback.pause()
        playbackState.setState(
            PlaybackStateCompat.STATE_PAUSED,
            playback.currentStreamPosition, mediaSession
        )
    }

    override fun onStop() {
        super.onStop()
        playback.stop()
        playbackState.setState(
            PlaybackStateCompat.STATE_STOPPED,
            playback.currentStreamPosition, mediaSession
        )
    }

    private fun setMetadata(description: MediaDescriptionCompat?) {
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, description?.title.toString())
                .putString(
                    MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                    description?.iconUri.toString()
                )
                .build()
        )
    }
}