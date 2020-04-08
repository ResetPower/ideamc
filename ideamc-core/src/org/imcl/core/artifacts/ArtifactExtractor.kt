package org.imcl.core.artifacts

import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

object ArtifactExtractor {
    fun extract(file: File, extractFileOnly: Boolean = false, extractDirectoryOnly: Boolean = false) : Vector<Pair<String, ByteArray>> {
        val zf = ZipFile(file)
        val zis = ZipInputStream(FileInputStream(file))
        var entry: ZipEntry? = null
        val files = Vector<Pair<String, ByteArray>>()
        if (extractDirectoryOnly) {
            while (true) {
                entry = zis.nextEntry
                if (entry==null||!entry.isDirectory) {
                    break
                }
                files.add(Pair<String, ByteArray>(entry.name, zf.getInputStream(entry).readBytes()))
            }
        } else if (extractFileOnly) {
            while (true) {
                entry = zis.nextEntry
                if (entry==null||entry.isDirectory||entry.name.indexOf("/")!=-1) {
                    break
                }
                files.add(Pair<String, ByteArray>(entry.name, zf.getInputStream(entry).readBytes()))
            }
        } else {
            while (true) {
                entry = zis.nextEntry
                if (entry==null) {
                    break
                }
                files.add(Pair<String, ByteArray>(entry.name, zf.getInputStream(entry).readBytes()))
            }
        }
        return files
    }
}
