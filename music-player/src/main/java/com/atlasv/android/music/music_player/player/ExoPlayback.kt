package com.atlasv.android.music.music_player.player

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import com.atlasv.android.music.music_player.provider.MediaResource
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes

/**
 * Created by woyanan on 2020-02-10
 */
open class ExoPlayback internal constructor(
    private val context: Context
) : Playback {
    private val mEventListener by lazy {
        ExoPlayerEventListener()
    }
    private var cacheManager = CacheManager(context, true, null)
    private val sourceManager: ExoSourceManager by lazy {
        ExoSourceManager(context, cacheManager)
    }

    private var mPlayOnFocusGain: Boolean = false
    private var mCallback: PlaybackCallback? = null
    private var mExoPlayerNullIsStopped = false
    private var mExoPlayer: SimpleExoPlayer? = null

    override var state: Int
        get() = if (mExoPlayer == null) {
            if (mExoPlayerNullIsStopped)
                PlaybackStateCompat.STATE_STOPPED
            else
                PlaybackStateCompat.STATE_NONE
        } else {
            when (mExoPlayer!!.playbackState) {
                Player.STATE_IDLE -> PlaybackStateCompat.STATE_PAUSED
                Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                Player.STATE_READY -> {
                    if (mExoPlayer!!.playWhenReady)
                        PlaybackStateCompat.STATE_PLAYING
                    else
                        PlaybackStateCompat.STATE_PAUSED
                }
                Player.STATE_ENDED -> PlaybackStateCompat.STATE_NONE
                else -> PlaybackStateCompat.STATE_NONE
            }
        }
        set(value) {}

    override val isConnected: Boolean
        get() = true

    override val isPlaying: Boolean
        get() = mPlayOnFocusGain || (mExoPlayer != null && mExoPlayer!!.playWhenReady)

    override val currentStreamPosition: Long
        get() = mExoPlayer?.currentPosition ?: 0

    override val bufferedPosition: Long
        get() = mExoPlayer?.bufferedPosition ?: 0

    override val duration: Long
        get() = mExoPlayer?.duration ?: -1

    override var currentMediaId: String = ""

    override var volume: Float
        get() = mExoPlayer?.volume ?: -1f
        set(value) {
            mExoPlayer?.volume = value
        }

    override fun getAudioSessionId(): Int {
        return mExoPlayer?.audioSessionId ?: 0
    }

    override fun stop(notifyListeners: Boolean) {
        releaseResources(true)
    }

    override fun play(mediaResource: MediaResource, isPlayWhenReady: Boolean) {
        mPlayOnFocusGain = true
        val mediaId = mediaResource.getMediaId()
        if (mediaId.isNullOrEmpty()) {
            return
        }
        if (mExoPlayer == null) {
            releaseResources(false)  // release everything except the player
            var source = mediaResource.getMediaUrl()
            if (source.isNullOrEmpty()) {
                mCallback?.onError("播放 url 为空")
                return
            }
            source = source.replace(" ".toRegex(), "%20") // Escape spaces for URLs

            if (mExoPlayer == null) {
                val uAmpAudioAttributes = AudioAttributes.Builder()
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build()

                mExoPlayer = ExoPlayerFactory.newSimpleInstance(context).apply {
                    setAudioAttributes(uAmpAudioAttributes, true)
                }
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build()
                mExoPlayer?.setAudioAttributes(audioAttributes, true)
                mExoPlayer?.addListener(mEventListener)
            }
            val mediaSource = sourceManager.buildMediaSource(
                source,
                null,
                cacheManager.isOpenCache(),
                cacheManager.getDownloadCache()
            )
            mExoPlayer?.prepare(mediaSource)
        }

        if (isPlayWhenReady) {
            mExoPlayer?.playWhenReady = true
        }
    }

    override fun pause() {
        mExoPlayer?.playWhenReady = false
        releaseResources(false)
    }

    override fun seekTo(position: Long) {
        mExoPlayer?.seekTo(position)
    }

    override fun setCallback(callback: PlaybackCallback) {
        this.mCallback = callback
    }

    private fun releaseResources(releasePlayer: Boolean) {
        if (releasePlayer) {
            mExoPlayer?.release()
            mExoPlayer?.removeListener(mEventListener)
            mExoPlayer = null
            mExoPlayerNullIsStopped = true
            mPlayOnFocusGain = false
        }
    }

    private inner class ExoPlayerEventListener : Player.EventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE, Player.STATE_BUFFERING, Player.STATE_READY ->
                    mCallback?.onPlaybackStatusChanged(state)
                Player.STATE_ENDED -> mCallback?.onCompletion()
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            val what: String = when (error.type) {
                ExoPlaybackException.TYPE_SOURCE -> error.sourceException.message.toString()
                ExoPlaybackException.TYPE_RENDERER -> error.rendererException.message.toString()
                ExoPlaybackException.TYPE_UNEXPECTED -> error.unexpectedException.message.toString()
                else -> "Unknown: $error"
            }
            mCallback?.onError("ExoPlayer error $what")
        }
    }
}