package net.donething.pc_phone.utils

import android.content.Context
import android.net.Uri
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.entity.Rest
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL

// 表单
data class Form(val op: String?, val data: Any?)

object Http {
    fun <T> get(urlString: String): Rest<T> {

        val url = URL(urlString)
        val httpURLConnection = url.openConnection() as HttpURLConnection
        httpURLConnection.requestMethod = "GET"

        val inputStream = BufferedInputStream(httpURLConnection.inputStream)
        val text = readStream(inputStream)

        return parseJSON(text)
    }

    fun <T> postJSON(urlString: String, jsonObj: Any): Rest<T> {
        val url = URL(urlString)
        val json = Comm.gson.toJson(jsonObj)
        val httpURLConnection = url.openConnection() as HttpURLConnection
        httpURLConnection.requestMethod = "POST"
        httpURLConnection.setRequestProperty("Content-Type", "application/json")

        val outputStream = BufferedOutputStream(httpURLConnection.outputStream)
        outputStream.write(json.toByteArray(Charsets.UTF_8))
        outputStream.flush()

        if (httpURLConnection.responseCode != HttpURLConnection.HTTP_OK) {
            throw Exception("响应码：${httpURLConnection.responseCode}")
        }

        val inputStream = BufferedInputStream(httpURLConnection.inputStream)
        val text = readStream(inputStream)
        outputStream.close()

        return parseJSON(text)
    }

    fun <T> postFiles(urlString: String, uris: List<Uri>, ctx: Context): Rest<T> {
        val boundary = "===" + System.currentTimeMillis() + "==="
        val lineEnd = "\r\n"

        val url = URL(urlString)
        val httpURLConnection = url.openConnection() as HttpURLConnection
        httpURLConnection.requestMethod = "POST"
        httpURLConnection.setRequestProperty("Connection", "Keep-Alive")
        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

        val outputStream = BufferedOutputStream(httpURLConnection.outputStream)
        val writer = PrintWriter(OutputStreamWriter(outputStream, "UTF-8"), true)

        uris.forEach { uri ->
            val inputStream = ctx.contentResolver.openInputStream(uri)
            val fileName = uri.lastPathSegment
            val contentType = "application/octet-stream"

            if (inputStream != null) {
                writer.append("--$boundary").append(lineEnd)
                    .append("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"").append(lineEnd)
                    .append("Content-Type: $contentType").append(lineEnd).append(lineEnd).flush()

                inputStream.copyTo(outputStream)

                writer.append(lineEnd).flush()
                inputStream.close()
            }
        }

        writer.append("--$boundary--").append(lineEnd).flush()

        if (httpURLConnection.responseCode != HttpURLConnection.HTTP_OK) {
            throw Exception("响应码：${httpURLConnection.responseCode}")
        }

        val inputStream = BufferedInputStream(httpURLConnection.inputStream)
        val text = readStream(inputStream)

        outputStream.close()
        writer.close()

        return parseJSON(text)
    }

    private fun readStream(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }

        return stringBuilder.toString()
    }

    private fun <T> parseJSON(text: String): Rest<T> {
        return Comm.gson.fromJson<Rest<T>>(text, Rest::class.java)
    }
}