package com.atlasv.android.music.player.bean

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by woyanan on 2020-02-11
 */
@Parcelize
class SongInfo(
    var songId: String = "", //音乐id
    var songName: String = "",  //音乐标题
    var songCover: String = "",  //音乐封面
    var songNameKey: String = "",
    var songCoverBitmap: Bitmap? = null,
    var songUrl: String = "",  //音乐播放地址
    var genre: String = "",  //类型（流派）
    var size: String = "", //音乐大小
    var duration: Long = -1, //音乐长度
    var artist: String = "",  //音乐艺术家
    var artistKey: String = "",
    var trackNumber: Int = 0, //媒体的曲目号码（序号：1234567……）
    var publishTime: String = "", //发布时间
    var year: String = "",  //录制音频文件的年份
    var modifiedTime: String = "",  //最后修改时间
    var description: String = "",  //音乐描述
    var mimeType: String = "",
    var albumId: String = "",     //专辑id
    var albumName: String = "",   //专辑名称
    var albumNameKey: String = "",
    var albumCover: String = "",  //专辑封面
    var albumSongCount: Int = 0,     //专辑音乐数
    var mMapHeadData: Map<String, String>? = hashMapOf() //header 信息
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SongInfo

        if (songId != other.songId) return false
        if (songName != other.songName) return false
        if (songCover != other.songCover) return false
        if (songNameKey != other.songNameKey) return false
        if (songCoverBitmap != other.songCoverBitmap) return false
        if (songUrl != other.songUrl) return false
        if (genre != other.genre) return false
        if (size != other.size) return false
        if (duration != other.duration) return false
        if (artist != other.artist) return false
        if (artistKey != other.artistKey) return false
        if (trackNumber != other.trackNumber) return false
        if (publishTime != other.publishTime) return false
        if (year != other.year) return false
        if (modifiedTime != other.modifiedTime) return false
        if (description != other.description) return false
        if (mimeType != other.mimeType) return false
        if (albumId != other.albumId) return false
        if (albumName != other.albumName) return false
        if (albumNameKey != other.albumNameKey) return false
        if (albumCover != other.albumCover) return false
        if (albumSongCount != other.albumSongCount) return false
        if (mMapHeadData != other.mMapHeadData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = songId.hashCode()
        result = 31 * result + songName.hashCode()
        result = 31 * result + songCover.hashCode()
        result = 31 * result + songNameKey.hashCode()
        result = 31 * result + (songCoverBitmap?.hashCode() ?: 0)
        result = 31 * result + songUrl.hashCode()
        result = 31 * result + genre.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + artistKey.hashCode()
        result = 31 * result + trackNumber
        result = 31 * result + publishTime.hashCode()
        result = 31 * result + year.hashCode()
        result = 31 * result + modifiedTime.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + albumId.hashCode()
        result = 31 * result + albumName.hashCode()
        result = 31 * result + albumNameKey.hashCode()
        result = 31 * result + albumCover.hashCode()
        result = 31 * result + albumSongCount
        result = 31 * result + (mMapHeadData?.hashCode() ?: 0)
        return result
    }
}
