package com.atlasv.android.music.music_player.notification

import android.os.Parcelable
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import kotlinx.android.parcel.Parcelize

/**
 * Created by woyanan on 2020-02-13
 */
@Parcelize
class NotificationVO(var metadata: MediaMetadataCompat?, var state: PlaybackStateCompat?) :
    Parcelable