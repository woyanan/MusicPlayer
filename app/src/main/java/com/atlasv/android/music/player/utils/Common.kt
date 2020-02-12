package com.atlasv.android.music.player.utils

/**
 * Created by woyanan on 2020-02-12
 */
object Common {

    fun formatPlayTime(duration: Long): String? {
        var time: String? = ""
        val minute = duration / 60000
        val seconds = duration % 60000
        val second = Math.round(seconds.toInt() / 1000.toFloat()).toLong()
        if (minute < 10) {
            time += "0"
        }
        time += "$minute:"
        if (second < 10) {
            time += "0"
        }
        time += second
        return time
    }
}