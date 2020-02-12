package com.atlasv.android.music.player

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.atlasv.android.music.music_player.AudioPlayer
import com.atlasv.android.music.player.bean.PlayInfo
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by woyanan on 2020-02-11
 */
class MainActivity : AppCompatActivity() {
    private var adapter: ListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recycleView.layoutManager = LinearLayoutManager(this)
        adapter = ListAdapter(this)
        recycleView.adapter = adapter
        getData()
        previous.setOnClickListener {
            AudioPlayer.getInstance(this@MainActivity).onSkipToPrevious()
        }
        next.setOnClickListener {
            AudioPlayer.getInstance(this@MainActivity).onSkipToNext()
        }
    }

    private fun getData() {
        ApiRequest().getPlayInfoList(object : ApiRequest.RequestCallback {
            override fun onSuccess(list: List<PlayInfo>?) {
                runOnUiThread {
                    if (!list.isNullOrEmpty()) {
                        adapter?.setPlayInfoList(list)
                        val playList = transformMediaMetadataCompatList(list)
                        AudioPlayer.getInstance(this@MainActivity)
                            .setData(this@MainActivity, playList)
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
}