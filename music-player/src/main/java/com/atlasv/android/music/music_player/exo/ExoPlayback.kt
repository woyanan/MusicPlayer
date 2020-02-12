package com.atlasv.android.music.music_player.exo

import android.content.Context
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes

/**
 * Created by woyanan on 2020-02-10
 */
open class ExoPlayback internal constructor(
    private val context: Context
) : IExoPlayback {
    private val eventListener by lazy {
        ExoPlayerEventListener()
    }
    private var cacheManager = CacheManager(context, true, null)
    private val sourceManager: ExoSourceManager by lazy {
        ExoSourceManager(context, cacheManager)
    }

    private var mPlayOnFocusGain: Boolean = false
    private var mExoPlayerNullIsStopped = false
    private var exoPlayer: SimpleExoPlayer? = null
    private var currentMediaId: String? = null

    override var state: Int
        get() = if (exoPlayer == null) {
            if (mExoPlayerNullIsStopped)
                PlaybackStateCompat.STATE_STOPPED
            else
                PlaybackStateCompat.STATE_NONE
        } else {
            when (exoPlayer!!.playbackState) {
                Player.STATE_IDLE -> PlaybackStateCompat.STATE_PAUSED
                Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                Player.STATE_READY -> {
                    if (exoPlayer!!.playWhenReady)
                        PlaybackStateCompat.STATE_PLAYING
                    else
                        PlaybackStateCompat.STATE_PAUSED
                }
                Player.STATE_ENDED -> PlaybackStateCompat.STATE_NONE
                else -> PlaybackStateCompat.STATE_NONE
            }
        }
        set(value) {}

    override val isPlaying: Boolean
        get() = mPlayOnFocusGain || (exoPlayer != null && exoPlayer!!.playWhenReady)

    override val currentStreamPosition: Long
        get() = exoPlayer?.currentPosition ?: 0

    override val bufferedPosition: Long
        get() = exoPlayer?.bufferedPosition ?: 0

    override val duration: Long
        get() = exoPlayer?.duration ?: -1

    override fun getAudioSessionId(): Int {
        return exoPlayer?.audioSessionId ?: 0
    }

    override fun play(mediaResource: MediaDescriptionCompat, isPlayWhenReady: Boolean) {
        mPlayOnFocusGain = true
        if (mediaResource.mediaId.isNullOrEmpty() || mediaResource.mediaUri == null) {
            return
        }
        val mediaHasChanged = mediaResource.mediaId != currentMediaId
        if (mediaHasChanged) {
            currentMediaId = mediaResource.mediaId!!
        }
        if (mediaHasChanged) {
            // release everything except the player
            releaseResources(false)

            if (exoPlayer == null) {
                createExoPlayer()
            }
            val mediaSource = sourceManager.buildMediaSource(
                mediaResource.mediaUri,
                cacheManager.isOpenCache(),
                cacheManager.getDownloadCache()
            )
            exoPlayer?.prepare(mediaSource)
        }

        if (isPlayWhenReady) {
            exoPlayer?.playWhenReady = true
        }
    }

    private fun createExoPlayer() {
        exoPlayer = SimpleExoPlayer.Builder(context).build()
        exoPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(), true
        )
        exoPlayer?.addListener(eventListener)
    }

    override fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    override fun pause() {
        exoPlayer?.playWhenReady = false
        releaseResources(false)
    }

    override fun stop() {
        releaseResources(true)
    }

    private fun releaseResources(releasePlayer: Boolean) {
        if (releasePlayer) {
            exoPlayer?.release()
            exoPlayer = null
            mExoPlayerNullIsStopped = true
            mPlayOnFocusGain = false
        }
    }

    private inner class ExoPlayerEventListener : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

        }

        override fun onPlayerError(error: ExoPlaybackException) {

        }
    }
}