package com.atlasv.android.music.music_player.exo

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.cache.*
import java.io.File

/**
 * Created by woyanan on 2020-02-10
 */
class CacheManager constructor(
    private val context: Context,
    private val isOpenCache: Boolean,
    private val cacheDestFileDir: String?
) {
    private var downloadDirectory: File? = null
    private var downloadCache: Cache? = null

    fun isOpenCache(): Boolean {
        return isOpenCache
    }

    /**
     * 创建缓存文件夹
     */
    private fun getDownloadDirectory(context: Context): File {
        if (!cacheDestFileDir.isNullOrEmpty()) {
            downloadDirectory = File(cacheDestFileDir)
            if (!downloadDirectory!!.exists()) {
                downloadDirectory!!.mkdirs()
            }
        }
        if (downloadDirectory == null) {
            downloadDirectory = context.getExternalFilesDir(null)
            if (downloadDirectory == null) {
                downloadDirectory = context.filesDir
            }
        }
        return downloadDirectory!!
    }

    /**
     * 获取缓存实例
     */
    @Synchronized
    fun getDownloadCache(): Cache? {
        if (downloadCache == null) {
            val path = getDownloadDirectory(context).absolutePath
            val downloadContentDirectory = getDownloadDirectory(context)
            val isLocked = SimpleCache.isCacheFolderLocked(File(path))
            if (!isLocked) {
                downloadCache = SimpleCache(
                    downloadContentDirectory,
                    LeastRecentlyUsedCacheEvictor(ExoSourceManager.DEFAULT_MAX_SIZE.toLong())
                )
            }
        }
        return downloadCache
    }

    /**
     * 根据缓存块判断是否缓存成功
     */
    fun resolveCacheState(cache: Cache? = getDownloadCache(), url: String?): Boolean {
        var isCache = true
        if (!url.isNullOrEmpty()) {
            val key = CacheUtil.generateKey(Uri.parse(url))
            if (!key.isNullOrEmpty()) {
                val cachedSpans = cache?.getCachedSpans(key)
                if (cachedSpans?.size == 0) {
                    isCache = false
                } else {
                    isCache = cache?.let {
                        val contentLength =
                            cache.getContentMetadata(key)["exo_len", C.LENGTH_UNSET.toLong()]
                        var currentLength: Long = 0
                        for (cachedSpan in cachedSpans ?: hashSetOf<CacheSpan>()) {
                            currentLength += cache.getCachedLength(
                                key, cachedSpan.position,
                                cachedSpan.length
                            )
                        }
                        return currentLength >= contentLength
                    } ?: false
                }
            } else {
                isCache = false
            }
        }
        return isCache
    }
}