package com.atlasv.android.music.player

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.atlasv.android.music.music_player.MusicPlayer
import com.atlasv.android.music.player.bean.SongInfo
import com.bumptech.glide.Glide
import java.util.*

/**
 * Created by woyanan on 2020-02-11
 */
class ListAdapter(private val context: Context) :
    RecyclerView.Adapter<ListAdapter.Holder>() {
    private val mSongInfos: ArrayList<SongInfo> = ArrayList()

    fun setSongInfos(songInfos: List<SongInfo>) {
        mSongInfos.clear()
        mSongInfos.addAll(songInfos)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return mSongInfos.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val songInfo = mSongInfos[position]
        Glide.with(context).load(songInfo.songCover).into(holder.cover)
        holder.title.text = songInfo.songName
        holder.itemView.setOnClickListener {
            MusicPlayer.getInstance(holder.itemView.context).onPlay(songInfo.songId)
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
//        val songInfo = mSongInfos[position]
//        if (payloads.isEmpty()) {
//            onBindViewHolder(holder, position)
//        } else {
//
//        }
    }

    class Holder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cover = itemView.findViewById<ImageView>(R.id.cover)
        val title = itemView.findViewById<TextView>(R.id.title)
        val state = itemView.findViewById<TextView>(R.id.state)
    }
}