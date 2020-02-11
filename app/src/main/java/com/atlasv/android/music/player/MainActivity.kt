package com.atlasv.android.music.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.atlasv.android.music.player.bean.SongInfo
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by woyanan on 2020-02-11
 */
class MainActivity : AppCompatActivity() {
    private var mListAdapter: ListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recycleView.layoutManager = LinearLayoutManager(this)
        mListAdapter = ListAdapter(this)
        recycleView.adapter = mListAdapter
        getData()
    }

    private fun getData() {
        val request = ApiRequest()
        request.getMusicList(object : ApiRequest.RequestCallback {
            override fun onSuccess(list: List<SongInfo>?) {
                runOnUiThread {
                    if (!list.isNullOrEmpty()) {
                        mListAdapter?.setSongInfos(list)
                    }
                }
            }
        })
    }
}