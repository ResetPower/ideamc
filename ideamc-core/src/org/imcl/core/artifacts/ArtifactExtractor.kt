package org.imcl.core.artifacts

import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

object ArtifactExtractor {
    fun extract(file: File) : Vector<Pair<String, ByteArray>> {
        try {
            val zf = ZipFile(file)
            val zis = ZipInputStream(FileInputStream(file))
            var entry: ZipEntry? = null
            val files = Vector<Pair<String, ByteArray>>()
            while (true) {
                entry = zis.nextEntry
                if (entry==null) {
                    break
                }
                if (entry.name.indexOf("/")!=-1) continue
                files.add(Pair<String, ByteArray>(entry.name, zf.getInputStream(entry).readBytes()))
            }
            return files
        } catch (e: ZipException) {
            return Vector()
        } catch (e: Exception) {
            throw e
        }
    }
}
