package com.atlasv.android.music.music_player.provider

/**
 * Created by woyanan on 2020-02-10
 */
class MediaResource constructor() {
    private lateinit var mediaId: String
    private var mediaUrl: String? = ""

    constructor(
        mediaId: String,
        mediaUrl: String?
    ) : this() {
        this.mediaId = mediaId
        this.mediaUrl = mediaUrl
    }

    fun getMediaId(): String? {
        return mediaId
    }

    fun getMediaUrl(): String? {
        return mediaUrl
    }
}