package net.donething.pc_phone.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import net.donething.pc_phone.entity.Rest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.source
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

// 表单
data class Form<T>(val op: String?, val data: T?)

// Uri to RequestBody. 适配大文件，不全部读到内存
fun Uri.asRequestBody(contentResolver: ContentResolver, mimeType: String): RequestBody {
    return object : RequestBody() {
        override fun contentType() = mimeType.toMediaTypeOrNull()

        override fun writeTo(sink: BufferedSink) {
            contentResolver.openInputStream(this@asRequestBody)?.use { inputStream ->
                sink.writeAll(inputStream.source())
            }
        }
    }
}

object Http {
    private val itag = this::class.simpleName

    private fun newClient(): OkHttpClient {
        // 默认的 OkHttpClient 的连接超时、读取超时都是10秒
        val builder = OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.HOURS)

        return builder.build()
    }

    /**
     * GET JSON
     */
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

    /**
     * GET 文本或文件（获取 PC 剪贴板时）
     */
    fun <T> getTextOrFile(urlString: String): Rest<T?> {
        val client = newClient()
        val request = Request.Builder().url(urlString).build()

        val resp = client.newCall(request).execute()
        if (!resp.isSuccessful) {
            throw Exception("响应码：${resp.code}")
        }

        if (resp.body == null) {
            throw Exception("响应体为空")
        }

        // 返回 JSON 文本
        if (resp.body!!.contentType().toString().contains("application/json")) {
            val text = resp.body!!.string()

            return parseJSON(text)
        }

        // 返回文件，保存到应用目录
        if (resp.body!!.contentType().toString().contains("application/octet-stream")) {
            val inputStream = resp.body!!.byteStream()
            val filename = getFilename(resp)
            val dstDirFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val tmpPath = dstDirFile.absolutePath + File.separator + filename

            val dstPath = genUniqueFileName(tmpPath)
            Log.i(itag, "保存文件到：$dstPath")

            val outputStream = FileOutputStream(dstPath)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()

            return Rest(0, "收到文件，已保存到应用目录", null)
        }

        // throw Exception("：" + resp.body!!.contentType().toString())
        return Rest(0, "接收 PC 剪贴板中的文件出错，未知的响应类型：${resp.body!!.contentType().toString()}")
    }

    /**
     * POST JSON 数据
     */
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

    /**
     * POST 上传文件
     */
    fun <T> postFiles(urlString: String, uris: List<Uri>, ctx: Context): Rest<T> {
        val client = newClient()
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

        for (uri in uris) {
            Log.i(itag, "当前发送的文件：${uri.path}")
            val requestBody = uri.asRequestBody(ctx.contentResolver, "application/octet-stream")
            builder.addFormDataPart("file", File(uri.path ?: "未知文件名").name, requestBody)
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

        return parseJSON(text)
    }

    private fun <T> parseJSON(text: String): Rest<T> {
        return Comm.gson.fromJson<Rest<T>>(text, Rest::class.java)
    }

    /**
     * 保存文件时，不覆盖已存在的同名文件
     */
    private fun genUniqueFileName(dstPath: String): String {
        // 判断文件在本地是否已存在
        val fileCk = File(dstPath)
        // 不存在则不用取新名
        if (!fileCk.exists()) {
            return dstPath
        }

        // 存在则重命名新文件（加上时间）
        val filename = fileCk.name
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val basename = filename.substringBeforeLast('.')
        val ext = filename.substringAfterLast('.')
        val fullname = "${basename}__$time.$ext"

        return fileCk.parent!! + File.separator + fullname
    }

    /**
     * 解析文件名
     * Content-Disposition:attachment;filename=FileName.txt
     * Content-Disposition: attachment; filename*="UTF-8''%E6%9B%BF%E6%8D%A2%E5%AE.pdf"
     * @link https://blog.csdn.net/EaskShark/article/details/79180344
     */
    private fun getFilename(response: Response): String {
        var filename = ""
        var dispositionHeader = response.header("Content-Disposition") ?: return ""

        dispositionHeader.replace("attachment;filename=", "")
        dispositionHeader.replace("filename*=utf-8", "")

        val strings = dispositionHeader.split("; ")
        if (strings.size > 1) {
            dispositionHeader = strings[1].replace("filename=", "")
            dispositionHeader = dispositionHeader.replace("\"", "")
            filename = dispositionHeader
        }

        if (filename == "") {
            filename = System.currentTimeMillis().toString()
        }

        return filename
    }
}
