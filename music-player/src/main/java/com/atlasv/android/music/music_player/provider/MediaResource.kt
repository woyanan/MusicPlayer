package com.atlasv.android.music.music_player.provider

/**
 * Created by woyanan on 2020-02-10
 */
class MediaResource constructor() {
    private lateinit var mediaId: String
    private var queueId: Long = 0L
    private var mediaUrl: String? = ""
    private var mMapHeadData: Map<String, String>? = hashMapOf()
    private var mCacheMediaResource = hashMapOf<String, MediaResource>()

    constructor(
        mediaId: String,
        mediaUrl: String?,
        queueId: Long,
        headData: Map<String, String>?
    ) : this() {
        this.mediaId = mediaId
        this.queueId = queueId
        this.mediaUrl = mediaUrl
        this.mMapHeadData = headData
    }

    fun obtain(
        mediaId: String?, mediaUrl: String?, queueId: Long, headData: Map<String, String>?
    ): MediaResource {
        if (mediaId.isNullOrEmpty()) {
            throw IllegalStateException("songId is null")
        }
        var resource = mCacheMediaResource[mediaId]
        if (resource == null) {
            resource = MediaResource(mediaId, mediaUrl, queueId, headData)
            mCacheMediaResource[mediaId] = resource
        }
        if (resource.getMediaUrl() != mediaUrl) {
            resource.mediaUrl = mediaUrl
        }
        return resource
    }

    fun getMediaId(): String? {
        return mediaId
    }

    fun getQueueId(): Long {
        return queueId
    }

    fun getMediaUrl(): String? {
        return mediaUrl
    }

    fun getMapHeadData(): Map<String, String>? {
        return mMapHeadData
    }
}