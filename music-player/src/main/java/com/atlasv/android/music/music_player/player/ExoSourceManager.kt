package com.atlasv.android.music.music_player.player

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util

/**
 * Created by woyanan on 2020-02-10
 */
class ExoSourceManager constructor(
    private val context: Context, private val cacheManager: CacheManager
) {
    private var dataSource: String = ""
    private var mMapHeadData: Map<String, String>? = hashMapOf()
    private var cache: Cache? = null
    private var isCached = false

    companion object {
        const val DEFAULT_MAX_SIZE = 512 * 1024 * 1024
    }

    fun buildMediaSource(
        dataSource: String,
        mapHeadData: Map<String, String>?,
        cacheEnable: Boolean,
        cache: Cache?
    ): MediaSource {
        this.dataSource = dataSource
        this.mMapHeadData = mapHeadData
        this.cache = cache
        val contentUri = Uri.parse(dataSource)
        val dataSourceFactory = getDataSourceFactoryCache(cacheEnable)
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(contentUri)
    }

    private fun getDataSourceFactoryCache(
        cacheEnable: Boolean
    ): DataSource.Factory {
        return if (cacheEnable) {
            if (cache != null) {
                isCached = cacheManager.resolveCacheState(cache, dataSource)
            }
            CacheDataSourceFactory(
                cache, getDataSourceFactory(),
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
            )
        } else {
            getDataSourceFactory()
        }
    }

    private fun getDataSourceFactory(): DataSource.Factory {
        return DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, context.applicationInfo?.name)
        )
    }
}