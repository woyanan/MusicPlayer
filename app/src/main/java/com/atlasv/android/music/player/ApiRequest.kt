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
    fun getPlayInfoList(
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
                        info.songUrl = getUrl(info.songId)
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
    fun getUrl(id: String): String {
        val request = Request.Builder()
            .url(
                "http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.play&songid=$id"
            )
            .build()
        val response: Response? = client?.newCall(request)?.execute()
        try {
            val jsonObject = JSONObject(response?.body?.string()).getJSONObject("bitrate")
            return jsonObject.getString("file_link")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    interface RequestCallback {
        fun onSuccess(list: List<PlayInfo>?)
    }
}