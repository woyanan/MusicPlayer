package com.atlasv.android.music.player

import com.atlasv.android.music.player.bean.PlayInfo
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * Created by woyanan on 2020-02-11
 */
class ApiRequest {
    private var client: OkHttpClient? = null

    init {
        val builder = OkHttpClient().newBuilder()
        builder.addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val newRequest = chain.request().newBuilder()
                    .removeHeader("User-Agent")
                    .addHeader(
                        "User-Agent",
                        "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:0.9.4)"
                    )
                    .build()
                return chain.proceed(newRequest)
            }
        })
        client = builder.build()
    }

    /**
     * 获取数据
     */
    fun getMusicList(
        callback: RequestCallback
    ) {
        val request = Request.Builder()
            .url(
                "http://tingapi.ting.baidu.com/v1/restserver/ting?" +
                        "format=json" +
                        "&calback=" +
                        "&from=webapp_music" +
                        "&method=baidu.ting.billboard.billList" +
                        "&type=2" +
                        "&size=20" +
                        "&offset=0"
            )
            .build()
        client?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("======>接口请求失败, e: $e")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    val json = response.body!!.string()
                    val jsonObject = JSONObject(json)
                    val jsonArray = jsonObject.getJSONArray("song_list")
                    val list: MutableList<PlayInfo> = ArrayList()
                    for (i in 0 until jsonArray.length()) {
                        val `object` = jsonArray.getJSONObject(i)
                        val info =
                            PlayInfo()
                        info.songId = `object`.getString("song_id")
                        info.songCover = `object`.getString("pic_big")
                        info.songName = `object`.getString("title")
                        info.artist = `object`.getString("author")
                        info.songUrl =
                            "http://audio04.dmhmusic.com/71_53_T10052953671_128_4_1_0_sdk-cpm/cn/0209/M00/E1/B8/ChR47F33J_yAHE_JACrgf2qqnyQ634.mp3?xcode=91ea4a9e9046987f5daae20ec2058044d218218"
                        list.add(info)
                    }
                    callback.onSuccess(list)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    /**
     * 获取音频url
     */
    fun getSongInfoDetail(songId: String, callback: RequestInfoCallback) {
        val request = Request.Builder()
            .url(
                "http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.play&songid=" +
                        songId
            )
            .build()
        client!!.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonObject =
                        JSONObject(response.body!!.string()).getJSONObject("bitrate")
                    val url = jsonObject.getString("file_link")
                    callback.onSuccess(url)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    interface RequestCallback {
        fun onSuccess(list: List<PlayInfo>?)
    }

    interface RequestInfoCallback {
        fun onSuccess(songUrl: String?)
    }
}