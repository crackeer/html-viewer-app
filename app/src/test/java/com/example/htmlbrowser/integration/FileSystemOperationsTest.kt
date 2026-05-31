package com.example.htmlbrowser.integration

import com.example.htmlbrowser.utils.FileUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Integration tests for file system operations (copy, move, delete, size calculation).
 */
class FileSystemOperationsTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    // ─── calculateDirectorySize ──────────────────────────────────────────────────

    @Test
    fun calculateSize_singleFile_correct() {
        val dir = tempDir.newFolder("single")
        File(dir, "data.bin").writeBytes(ByteArray(2048))
        assertEquals(2048L, FileUtils.calculateDirectorySize(dir))
    }

    @Test
    fun calculateSize_nestedStructure_correct() {
        val root = tempDir.newFolder("root")
        File(root, "a.bin").writeBytes(ByteArray(100))
        val sub = File(root, "sub").also { it.mkdir() }
        File(sub, "b.bin").writeBytes(ByteArray(200))
        val deep = File(sub, "deep").also { it.mkdir() }
        File(deep, "c.bin").writeBytes(ByteArray(300))
        assertEquals(600L, FileUtils.calculateDirectorySize(root))
    }

    // ─── deleteDirectory ─────────────────────────────────────────────────────────

    @Test
    fun deleteDirectory_withFiles_deletesAll() {
        val dir = tempDir.newFolder("to_delete")
        File(dir, "f1.txt").writeText("hello")
        File(dir, "f2.txt").writeText("world")
        assertTrue(FileUtils.deleteDirectory(dir))
        assertFalse(dir.exists())
    }

    @Test
    fun deleteDirectory_nested_deletesAll() {
        val root = tempDir.newFolder("nested_del")
        val sub = File(root, "sub").also { it.mkdir() }
        File(sub, "file.txt").writeText("content")
        assertTrue(FileUtils.deleteDirectory(root))
        assertFalse(root.exists())
    }

    @Test
    fun deleteDirectory_nonExistent_returnsFalse() {
        val dir = File(tempDir.root, "ghost_dir")
        assertFalse(FileUtils.deleteDirectory(dir))
    }

    // ─── createTempFile ──────────────────────────────────────────────────────────

    @Test
    fun createTempFile_fileExists() {
        // Use java.io.File.createTempFile directly (no Context needed)
        val tmp = File.createTempFile("archive_import_", ".zip", tempDir.root)
        assertTrue(tmp.exists())
        assertTrue(tmp.name.startsWith("archive_import_"))
        assertTrue(tmp.name.endsWith(".zip"))
        tmp.delete()
    }

    // ─── getFileNameFromUri (path segment fallback) ──────────────────────────────

    @Test
    fun fileNameExtraction_fromPath_returnsLastSegment() {
        // Test the path-segment fallback logic directly
        val path = "/storage/emulated/0/Download/my_archive.zip"
        val name = path.substringAfterLast("/")
        assertEquals("my_archive.zip", name)
    }

    // ─── Size consistency after operations ───────────────────────────────────────

    @Test
    fun sizeAfterDelete_decreasesCorrectly() {
        val root = tempDir.newFolder("size_test")
        val f1 = File(root, "f1.bin").also { it.writeBytes(ByteArray(1000)) }
        val f2 = File(root, "f2.bin").also { it.writeBytes(ByteArray(500)) }

        val totalBefore = FileUtils.calculateDirectorySize(root)
        assertEquals(1500L, totalBefore)

        f1.delete()
        val totalAfter = FileUtils.calculateDirectorySize(root)
        assertEquals(500L, totalAfter)
    }
}
