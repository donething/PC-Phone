package net.donething.pc_phone.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import net.donething.pc_phone.entity.Rest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

// 表单
data class Form(val op: String?, val data: Any?)

object Http {
    private val itag = this::class.simpleName

    private fun newClient(): OkHttpClient {
        // 默认的 OkHttpClient 的连接超时、读取超时都是10秒
        val builder = OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(1, TimeUnit.HOURS)
        return builder.build()
    }

    fun <T> get(urlString: String): Rest<T> {
        val client = newClient()
        val request = Request.Builder().url(urlString).build()

        val resp = client.newCall(request).execute()
        if (!resp.isSuccessful) {
            throw Exception("响应码：${resp.code}")
        }

        if (resp.body == null) {
            throw Exception("响应体为空")
        }

        val text = resp.body!!.string()

        return parseJSON(text)
    }

    fun <T> postJSON(urlString: String, jsonObj: Any): Rest<T> {
        val client = newClient()
        val json = Comm.gson.toJson(jsonObj)
        val body = json.toRequestBody("application/json;charset=utf-8".toMediaType())
        val request = Request.Builder().url(urlString).post(body).build()

        val resp = client.newCall(request).execute()
        if (!resp.isSuccessful) {
            throw Exception("响应码：${resp.code}")
        }

        if (resp.body == null) {
            throw Exception("响应体为空")
        }

        val text = resp.body!!.string()

        return parseJSON(text)
    }

    fun <T> postFiles(urlString: String, uris: List<Uri>, ctx: Context): Rest<T> {
        val client = newClient()
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

        val tmpFiles = ArrayList<File>()
        for (uri in uris) {
            Log.i(itag, "当前发送的文件：${uri.path}")

            val inputStream = ctx.contentResolver.openInputStream(uri)
            val file = File(ctx.cacheDir, uri.lastPathSegment ?: System.currentTimeMillis().toString())
            file.createNewFile()
            file.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            inputStream?.close()

            val requestBody = file.asRequestBody("application/octet-stream".toMediaType())
            builder.addFormDataPart("file", file.name, requestBody)
            tmpFiles.add(file)
        }

        val request = Request.Builder().url(urlString).post(builder.build()).build()

        val resp = client.newCall(request).execute()

        if (!resp.isSuccessful) {
            throw Exception("响应码：${resp.code}")
        }

        if (resp.body == null) {
            throw Exception("响应体为空")
        }

        val text = resp.body!!.string()

        // 删除所有临时文件
        for (file in tmpFiles) {
            file.delete()
        }

        return parseJSON(text)
    }

    private fun <T> parseJSON(text: String): Rest<T> {
        return Comm.gson.fromJson<Rest<T>>(text, Rest::class.java)
    }
}