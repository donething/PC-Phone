package net.donething.pc_phone.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.reflect.TypeToken
import net.donething.pc_phone.entity.FileInfo
import net.donething.pc_phone.entity.Rest
import net.donething.pc_phone.ui.preferences.PreferencesRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.source
import java.io.File
import java.lang.Error
import java.net.URLEncoder
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
    val itag = this::class.simpleName

    private fun newClient(): OkHttpClient {
        // 默认的 OkHttpClient 的连接超时、读取超时都是10秒
        val builder = OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.HOURS)

        return builder.build()
    }

    /**
     * 执行请求
     */
    fun exec(builder: Request.Builder): Response {
        val client = newClient()
        var request = builder.build()

        // 如果访问的是本应用的后台 API，需要加上授权码
        if (request.url.host == PreferencesRepository.taskMode().pcLanIP &&
            request.url.port.toString() == PreferencesRepository.taskMode().pcServerPort &&
            request.url.encodedPath.startsWith("/api/")
        ) {
            val bearer = "Bearer ${PreferencesRepository.taskMode().securityAuth}"
            request = request.newBuilder().addHeader("Authorization", bearer).build()
        }

        // 建立、执行请求
        val resp = client.newCall(request).execute()
        if (!resp.isSuccessful) {
            throw Exception("响应码：${resp.code}")
        }

        if (resp.body == null) {
            throw Exception("响应体为空")
        }

        return resp
    }

    /**
     * GET JSON
     */
    inline fun <reified T> get(url: String): Rest<T> {
        val builder = Request.Builder().url(url)
        val resp = exec(builder)
        val text = resp.body!!.string()

        return parseJSON(text)
    }

    /**
     * POST JSON 数据
     */
    inline fun <reified T> postJSON(urlString: String, jsonObj: Any): Rest<T> {
        val json = Comm.gson.toJson(jsonObj)
        val body = json.toRequestBody("application/json;charset=utf-8".toMediaType())

        val builder = Request.Builder().url(urlString).post(body)
        val resp = exec(builder)

        val text = resp.body!!.string()

        return parseJSON(text)
    }

    /**
     * POST 上传文件
     */
    inline fun <reified T> postFiles(urlString: String, uris: List<Uri>, ctx: Context): Rest<T> {
        val bodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

        for (uri in uris) {
            Log.i(itag, "当前发送的文件：${uri.path}")
            val requestBody = uri.asRequestBody(ctx.contentResolver, "application/octet-stream")
            bodyBuilder.addFormDataPart("file", File(uri.path ?: "未知文件名").name, requestBody)
        }

        val builder = Request.Builder().url(urlString).post(bodyBuilder.build())

        val resp = exec(builder)

        val text = resp.body!!.string()

        return parseJSON(text)
    }

    /**
     * 下载文件或目录
     */
    fun downloadFileOrDir(fileInfo: FileInfo, dstDirFile: File, pcAddr: String) {
        // 为文件时，保存到 Android 本地
        if (!fileInfo.isDir) {
            val pathEncoded = URLEncoder.encode(fileInfo.path, "UTF-8")
            val builder = Request.Builder().url("$pcAddr/api/file/download?path=$pathEncoded")
            val resp = exec(builder)

            val inputStream = resp.body!!.byteStream()

            val dstPath = genUniqueFileName(dstDirFile.absolutePath + File.separator + fileInfo.name)
            Log.i(itag, "downloadFileOrDir: 保存文件到'${dstPath}'")

            val dstFile = File(dstPath)
            val parentFile = dstFile.parentFile!!
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }

            dstFile.outputStream().use {
                inputStream.copyTo(it)
            }

            return
        }

        // 为目录时，递归获取子文件

        // 创建子目录
        val dir = File(dstDirFile, fileInfo.name)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        // 获取目录下的子文件
        val pathEncoded = URLEncoder.encode(fileInfo.path, "UTF-8")
        val respList = get<Array<FileInfo>>("$pcAddr/api/file/list?path=$pathEncoded")

        val children = respList.data ?: throw Error("响应的子文件列表为 null")
        Log.i(itag, "downloadFileOrDir: 远程目录'${fileInfo.path}下有${children.size}个文件（夹）需要处理下载")

        // 递归下载
        for (child in children) {
            downloadFileOrDir(child, dir, pcAddr)
        }
    }

    /**
     * 解析 JSON 响应文本为对象
     *
     * 为了能用泛型解析复杂的 map，需要手动传递 typeToken：
     *
     * val type = object : TypeToken<Rest<FileInfo>>() {}.type
     *
     * val rest:Rest<FileInfo> = parseJSON(response, type)
     */
    inline fun <reified T> parseJSON(text: String): Rest<T> {
        val type = object : TypeToken<Rest<T>>() {}.type
        return Comm.gson.fromJson(text, type)
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
