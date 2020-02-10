package com.atlasv.android.music.music_player.service

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat

/**
 * Created by woyanan on 2020-02-10
 */
class MediaPlaybackService : MediaBrowserServiceCompat() {
    companion object {
        private const val BROWSER_ROOT_ID = "/"
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        //no-op
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(BROWSER_ROOT_ID, null)
    }
}