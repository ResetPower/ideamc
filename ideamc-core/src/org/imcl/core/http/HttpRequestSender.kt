package org.imcl.core.http

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset


object HttpRequestSender {
    @JvmStatic
    fun post(spec: String, param: String, head: Pair<String, String> = Pair("Content-Type", "application/json"), whenError: () -> Unit): String? {
        var responseBuilder: StringBuilder? = null
        var reader: BufferedReader? = null
        var wr: OutputStreamWriter? = null
        val url: URL
        try {
            url = URL(spec)
            val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            conn.setRequestProperty(head.first, head.second)
            conn.doOutput = true
            conn.connectTimeout = 1000 * 5
            wr = OutputStreamWriter(conn.outputStream)
            wr.write(param)
            wr.flush()
            reader = BufferedReader(InputStreamReader(conn.inputStream))
            responseBuilder = StringBuilder()
            var line: String? = null
            while (reader.readLine().also { line = it } != null) {
                responseBuilder.append(line)
            }
            wr.close()
            reader.close()
            return responseBuilder.toString()
        } catch (e: IOException) {
            whenError()
            return null
        }
    }
    @JvmStatic
    fun get(spec: String, whenError: () -> Unit): String {
        var result: String = ""
        var `in`: BufferedReader? = null
        try {
            val urlNameString: String = "$spec"
            val realUrl = URL(urlNameString)
            val connection: URLConnection = realUrl.openConnection()
            connection.setRequestProperty("accept", "*/*")
            connection.connect()
            `in` = BufferedReader(InputStreamReader(connection.getInputStream()))
            var line: String?
            while (`in`.readLine().also { line = it } != null) {
                result += line
            }
        } catch (e: Exception) {
            whenError()
        } finally {
            try {
                `in`?.close()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
        return result
    }
    @JvmStatic
    fun readFileByUrl(urlStr: String, whenError: () -> Unit): String {
        var res = ""
        try {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            //设置超时间为3秒
            conn.connectTimeout = 3 * 1000
            //得到输入流
            val inputStream: InputStream = conn.inputStream
            res = readInputStream(inputStream)
        } catch (e: Exception) {
            whenError()
        }
        return res
    }
    @JvmStatic
    fun readInputStream(inputStream: InputStream): String {
        val buffer = ByteArray(1024)
        var len = 0
        val bos = ByteArrayOutputStream()
        while (inputStream.read(buffer).also { len = it } != -1) {
            bos.write(buffer, 0, len)
        }
        bos.close()
        return String(bos.toByteArray(), Charset.forName("utf-8"))
    }
}
