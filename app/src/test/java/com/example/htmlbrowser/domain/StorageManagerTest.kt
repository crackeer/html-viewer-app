package com.example.htmlbrowser.domain

import com.example.htmlbrowser.utils.FileUtils
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.forAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class StorageManagerTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private fun createDirWithFiles(vararg fileSizes: Int): File {
        val dir = tempDir.newFolder("archive_${System.nanoTime()}")
        fileSizes.forEachIndexed { index, size ->
            val file = File(dir, "file_$index.bin")
            file.writeBytes(ByteArray(size))
        }
        return dir
    }

    // ─── calculateDirectorySize ─────────────────────────────────────────────────

    @Test
    fun emptyDirectory_returnsZero() {
        val dir = tempDir.newFolder("empty")
        assertEquals(0L, FileUtils.calculateDirectorySize(dir))
    }

    @Test
    fun singleFile_returnsFileSize() {
        val dir = createDirWithFiles(1024)
        assertEquals(1024L, FileUtils.calculateDirectorySize(dir))
    }

    @Test
    fun multipleFiles_returnsSumOfSizes() {
        val dir = createDirWithFiles(100, 200, 300)
        assertEquals(600L, FileUtils.calculateDirectorySize(dir))
    }

    @Test
    fun nestedDirectory_includesAllFiles() {
        val dir = tempDir.newFolder("nested")
        File(dir, "file1.bin").writeBytes(ByteArray(500))
        val subDir = File(dir, "sub").also { it.mkdir() }
        File(subDir, "file2.bin").writeBytes(ByteArray(300))
        assertEquals(800L, FileUtils.calculateDirectorySize(dir))
    }

    @Test
    fun nonExistentDirectory_returnsZero() {
        val dir = File(tempDir.root, "does_not_exist")
        assertEquals(0L, FileUtils.calculateDirectorySize(dir))
    }

    // ─── deleteDirectory ────────────────────────────────────────────────────────

    @Test
    fun deleteExistingDirectory_returnsTrue() {
        val dir = createDirWithFiles(100, 200)
        assertTrue(FileUtils.deleteDirectory(dir))
        assertFalse(dir.exists())
    }

    @Test
    fun deleteNonExistentDirectory_returnsFalse() {
        val dir = File(tempDir.root, "ghost")
        assertFalse(FileUtils.deleteDirectory(dir))
    }

    @Test
    fun deleteDirectory_isIdempotent() {
        val dir = createDirWithFiles(100)
        val first = FileUtils.deleteDirectory(dir)
        val second = FileUtils.deleteDirectory(dir)
        // First delete succeeds, second returns false (already gone)
        assertTrue(first)
        assertFalse(second)
    }

    // ─── Property-Based Tests ───────────────────────────────────────────────────

    @Test
    fun pbt_2_1_directorySizeEqualsSum() = runTest {
        forAll(Arb.list(Arb.int(0..1024), 0..10)) { sizes ->
            val dir = tempDir.newFolder("pbt_${System.nanoTime()}")
            sizes.forEachIndexed { i, size ->
                File(dir, "f$i.bin").writeBytes(ByteArray(size))
            }
            val calculated = FileUtils.calculateDirectorySize(dir)
            val expected = sizes.sumOf { it.toLong() }
            FileUtils.deleteDirectory(dir)
            calculated == expected
        }
    }

    @Test
    fun pbt_2_2_deleteIsIdempotent() = runTest {
        forAll(Arb.list(Arb.int(1..512), 1..5)) { sizes ->
            val dir = tempDir.newFolder("pbt_del_${System.nanoTime()}")
            sizes.forEachIndexed { i, size ->
                File(dir, "f$i.bin").writeBytes(ByteArray(size))
            }
            val first = FileUtils.deleteDirectory(dir)
            val second = FileUtils.deleteDirectory(dir)
            // First must succeed, second must return false
            first && !second
        }
    }

    @Test
    fun pbt_2_3_storageInvariantAfterDelete() = runTest {
        forAll(Arb.list(Arb.int(1..512), 2..5)) { sizes ->
            val dirs = sizes.map { size ->
                val dir = tempDir.newFolder("pbt_inv_${System.nanoTime()}")
                File(dir, "data.bin").writeBytes(ByteArray(size))
                dir
            }
            val totalBefore = dirs.sumOf { FileUtils.calculateDirectorySize(it) }
            val toDelete = dirs.first()
            val deletedSize = FileUtils.calculateDirectorySize(toDelete)
            FileUtils.deleteDirectory(toDelete)
            val totalAfter = dirs.drop(1).sumOf { FileUtils.calculateDirectorySize(it) }
            // Clean up remaining
            dirs.drop(1).forEach { FileUtils.deleteDirectory(it) }
            totalAfter == totalBefore - deletedSize
        }
    }
}
