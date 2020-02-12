package com.atlasv.android.music.music_player.service

import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.atlasv.android.music.music_player.exo.IPlayback
import com.atlasv.android.music.music_player.playback.PlaybackState

/**
 * Created by woyanan on 2020-02-12
 */
class MediaSessionCallback(
    private val mediaSession: MediaSessionCompat,
    private val playback: IPlayback
) : MediaSessionCompat.Callback() {

    //UI可能被销毁,Service需要保存播放列表,并处理循环模式
    private val playList = arrayListOf<MediaSessionCompat.QueueItem>()
    private var currentPosition: Int = 0
    private val playbackState = PlaybackState()

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
            playList.forEachIndexed { index, queueItem ->
                if (queueItem.description.mediaId == mediaId) {
                    playback.play(queueItem.description, true)
                    currentPosition = index
                    playbackState.setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        playback.currentStreamPosition, mediaSession
                    )
                    return
                }
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
//        mediaSession.setQueue(playList)
    }

    override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
        super.onRemoveQueueItem(description)
        playList.remove(
            MediaSessionCompat.QueueItem(description, description.hashCode().toLong())
        )
//        mediaSession.setQueue(playList)
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        currentPosition = if (currentPosition > 0) currentPosition - 1 else playList.size - 1
        playback.play(playList[currentPosition].description, true)
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        currentPosition = (++currentPosition % playList.size)
        playback.play(playList[currentPosition].description, true)
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
}