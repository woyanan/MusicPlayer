package com.atlasv.android.music.music_player.player

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.atlasv.android.music.music_player.provider.MediaResource
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.util.EventLogger

/**
 * Created by woyanan on 2020-02-10
 */
open class ExoPlayback internal constructor(
    var context: Context,
    private var cacheManager: CacheManager
) : Playback {
    private val trackSelectorParameters: DefaultTrackSelector.Parameters by lazy {
        DefaultTrackSelector.ParametersBuilder().build()
    }
    private val mEventListener by lazy {
        ExoPlayerEventListener()
    }
    private val sourceManager: ExoSourceManager by lazy {
        ExoSourceManager(context, cacheManager)
    }

    private var mPlayOnFocusGain: Boolean = false
    private var mCallback: PlaybackCallback? = null
    private var mExoPlayerNullIsStopped = false
    private var mExoPlayer: SimpleExoPlayer? = null

    companion object {
        private val TAG = ExoPlayback.javaClass.simpleName
        const val ACTION_CHANGE_VOLUME = "ACTION_CHANGE_VOLUME"
        const val ACTION_DERAILLEUR = "ACTION_DERAILLEUR"
    }

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

    override fun start() {
        // Nothing to do.
    }

    override fun stop(notifyListeners: Boolean) {
        releaseResources(true)
    }

    override fun updateLastKnownStreamPosition() {
        // Nothing to do. Position maintained by ExoPlayer.
    }

    override fun play(mediaResource: MediaResource, isPlayWhenReady: Boolean) {
        mPlayOnFocusGain = true
        val mediaId = mediaResource.getMediaId()
        if (mediaId.isNullOrEmpty()) {
            return
        }
        val mediaHasChanged = mediaId != currentMediaId
        if (mediaHasChanged) {
            currentMediaId = mediaId
        }
        Log.d(
            TAG,
            "Playback# resource is empty = " + mediaResource.getMediaUrl().isNullOrEmpty() +
                    " mediaHasChanged = " + mediaHasChanged +
                    " isPlayWhenReady = " + isPlayWhenReady
        )
        if (mediaHasChanged || mExoPlayer == null) {
            releaseResources(false)  // release everything except the player
            var source = mediaResource.getMediaUrl()
            if (source.isNullOrEmpty()) {
                mCallback?.onError("播放 url 为空")
                return
            }
            source = source.replace(" ".toRegex(), "%20") // Escape spaces for URLs

            if (mExoPlayer == null) {
                //轨道选择
                val trackSelectionFactory = AdaptiveTrackSelection.Factory()

                //使用扩展渲染器的模式
                @DefaultRenderersFactory.ExtensionRendererMode
                val extensionRendererMode = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                val renderersFactory = DefaultRenderersFactory(context, extensionRendererMode)

                //轨道选择
                val trackSelector = DefaultTrackSelector(trackSelectionFactory)
                trackSelector.parameters = trackSelectorParameters

                val drmSessionManager: DefaultDrmSessionManager<FrameworkMediaCrypto>? = null

                mExoPlayer = ExoPlayerFactory.newSimpleInstance(
                    context, renderersFactory,
                    trackSelector, drmSessionManager
                )

                mExoPlayer!!.addListener(mEventListener)
                mExoPlayer!!.addAnalyticsListener(EventLogger(trackSelector))

                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build()
                mExoPlayer!!.setAudioAttributes(audioAttributes, true) //第二个参数能使ExoPlayer自动管理焦点
            }
            val mediaSource = sourceManager.buildMediaSource(
                source,
                mediaResource.getMapHeadData(),
                cacheManager.isOpenCache(),
                cacheManager.getDownloadCache()
            )
            mExoPlayer!!.prepare(mediaSource)
        }

        if (isPlayWhenReady) {
            mExoPlayer!!.playWhenReady = true
        }
    }

    override fun pause() {
        mExoPlayer?.playWhenReady = false
        releaseResources(false)
    }

    override fun seekTo(position: Long) {
        mExoPlayer?.seekTo(position)
    }

    override fun onFastForward() {
        if (mExoPlayer != null) {
            val currSpeed = mExoPlayer!!.playbackParameters.speed
            val currPitch = mExoPlayer!!.playbackParameters.pitch
            val newSpeed = currSpeed + 0.5f
            mExoPlayer!!.playbackParameters = PlaybackParameters(newSpeed, currPitch)
        }
    }

    override fun onRewind() {
        if (mExoPlayer != null) {
            val currSpeed = mExoPlayer!!.playbackParameters.speed
            val currPitch = mExoPlayer!!.playbackParameters.pitch
            var newSpeed = currSpeed - 0.5f
            if (newSpeed <= 0) {
                newSpeed = 0f
            }
            mExoPlayer!!.playbackParameters = PlaybackParameters(newSpeed, currPitch)
        }
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
        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            // Nothing to do.
        }

        override fun onTracksChanged(
            trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?
        ) {
            // Nothing to do.
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            // Nothing to do.
        }

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

        override fun onPositionDiscontinuity(reason: Int) {
            // Nothing to do.
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            // Nothing to do.
        }

        override fun onSeekProcessed() {
            // Nothing to do.
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            // Nothing to do.
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            // Nothing to do.
        }
    }
}