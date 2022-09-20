package cn.numeron.study03

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class WxAuthorRepository {

    suspend fun getWxAuthorList(listener: (Float) -> Unit): List<WxAuthor> {
        return withContext(Dispatchers.IO) {
            val wxAuthorJson = getWxAuthorJson(listener)
            deserializer(wxAuthorJson)
        }
    }

    private suspend fun getWxAuthorJson(listener: (Float) -> Unit): String {
        return suspendCancellableCoroutine {
            val url = URL("https://wanandroid.com/wxarticle/chapters/json")
            val httpUrlConnection = url.openConnection() as HttpURLConnection
            try {
                httpUrlConnection.requestMethod = "GET"
                httpUrlConnection.connectTimeout = 8000
                httpUrlConnection.readTimeout = 8000
                httpUrlConnection.connect()
                val contentLength = httpUrlConnection.getHeaderFieldInt("content-length", -1)
                val responseCode = httpUrlConnection.responseCode
                if (responseCode in 200..299) {
                    val json = httpUrlConnection
                        .inputStream
                        .bufferedReader()
                        .progressReader(contentLength, listener)
                        .use(Reader::readText)
                    it.resume(json)
                } else {
                    val json = httpUrlConnection
                        .errorStream
                        .bufferedReader()
                        .use(BufferedReader::readText)
                    throw RuntimeException(json)
                }
            } catch (throwable: Throwable) {
                it.resumeWithException(throwable)
            } finally {
                httpUrlConnection.disconnect()
            }
        }
    }

    private fun Reader.progressReader(contentLength: Int, listener: (Float) -> Unit): Reader {
        return object : BufferedReader(this) {
            var total = 0f
            override fun read(cbuf: CharArray?): Int {
                val read = super.read(cbuf)
                total += read
                if (contentLength > 0) {
                    listener(total / contentLength)
                }
                return read
            }
        }
    }

    private suspend fun deserializer(json: String): List<WxAuthor> {
        return suspendCoroutine { continuation ->
            try {
                val list = mutableListOf<WxAuthor>()
                val jsonObject = JSONObject(json)
                val data = jsonObject.getJSONArray("data")
                repeat(data.length()) { index ->
                    val wxAuthorJsonObject = data.getJSONObject(index)
                    val id = wxAuthorJsonObject.getLong("id")
                    val name = wxAuthorJsonObject.getString("name")
                    list.add(WxAuthor(id, name))
                }
                continuation.resume(list)
            } catch (throwable: Throwable) {
                continuation.resumeWithException(throwable)
            }
        }
    }

    companion object {
        private const val TAG = "WxAuthorRepository"
    }

}