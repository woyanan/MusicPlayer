package com.atlasv.android.music.player

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.atlasv.android.music.music_player.AudioPlayer
import com.atlasv.android.music.music_player.utils.TimerTaskManager
import com.atlasv.android.music.player.bean.PlayInfo
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by woyanan on 2020-02-11
 */
class MainActivity : AppCompatActivity() {
    private var adapter: ListAdapter? = null
    private var timer = TimerTaskManager()
    private val audioPlayer = AudioPlayer.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recycleView.layoutManager = LinearLayoutManager(this)
        adapter = ListAdapter(this)
        recycleView.adapter = adapter
        getData()
        previous.setOnClickListener {
            audioPlayer.onSkipToPrevious()
        }
        next.setOnClickListener {
            audioPlayer.onSkipToNext()
        }
        pause.setOnClickListener {
            audioPlayer.onPause()
        }
        stop.setOnClickListener {
            audioPlayer.onStop()
        }
        observe()
    }

    private fun observe() {
        AudioPlayer.getInstance(this).getPlaybackState()
            ?.observe(this, Observer<PlaybackStateCompat> {
                when (it.state) {
                    PlaybackStateCompat.STATE_PLAYING -> {
                        timer.updateProgress()
                    }
                    PlaybackStateCompat.STATE_PAUSED -> {
                        timer.stopProgress()
                    }
                    PlaybackStateCompat.STATE_STOPPED -> {
                        timer.stopProgress()
                    }
                    PlaybackStateCompat.STATE_ERROR -> {
                        timer.stopProgress()
                    }
                }
            })

        timer.setUpdateProgress(Runnable {
            val position = audioPlayer.getCurrentStreamPosition()
            val duration = audioPlayer.getDuration()
            val buffered = audioPlayer.getBufferedPosition()
            if (seekBar.max.toLong() != duration) {
                seekBar.max = duration.toInt()
            }
            seekBar.progress = position.toInt()
            seekBar.secondaryProgress = buffered.toInt()
            progress.text = duration.toString()
            time.text = duration.toString()
        })

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                audioPlayer.onSeekTo(seekBar.progress)
            }
        })
    }

    private fun getData() {
        ApiRequest().getPlayInfoList(object : ApiRequest.RequestCallback {
            override fun onSuccess(list: List<PlayInfo>?) {
                runOnUiThread {
                    if (!list.isNullOrEmpty()) {
                        adapter?.setPlayInfoList(list)
                        val playList = transformMediaMetadataCompatList(list)
                        audioPlayer.setData(this@MainActivity, playList)
                    }
                }
            }
        })
    }

    private fun transformMediaMetadataCompatList(list: List<PlayInfo>): ArrayList<MediaMetadataCompat> {
        val metadataCompatList = ArrayList<MediaMetadataCompat>()
        list.forEach {
            val element = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, it.songId)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, it.songUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, it.songName)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, it.songCover)
                .build()
            metadataCompatList.add(element)
        }
        return metadataCompatList
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.release()
    }
}