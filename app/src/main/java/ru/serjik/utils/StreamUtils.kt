package ru.serjik.utils

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object StreamUtils {
    private val UTF8 = Charsets.UTF_8

    @Throws(IOException::class)
    fun copy(input: InputStream, output: OutputStream): OutputStream {
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
        return output
    }

    fun closeQuietly(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (_: IOException) {
        }
    }

    fun toByteArray(input: InputStream): ByteArray {
        try {
            return (copy(input, ByteArrayOutputStream()) as ByteArrayOutputStream).toByteArray()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun toString(input: InputStream): String = String(toByteArray(input), UTF8)
}
