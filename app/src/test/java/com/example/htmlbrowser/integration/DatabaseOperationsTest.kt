package com.example.htmlbrowser.integration

import com.example.htmlbrowser.data.ArchiveRepositoryTest.FakeArchiveDao
import com.example.htmlbrowser.data.ArchiveRepositoryTest.FakeStorageManager
import com.example.htmlbrowser.data.repository.ArchiveRepository
import com.example.htmlbrowser.domain.model.HtmlArchive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.UUID

/**
 * End-to-end database operation tests using the in-memory FakeArchiveDao.
 */
class DatabaseOperationsTest {

    private lateinit var dao: FakeArchiveDao
    private lateinit var repository: ArchiveRepository

    @Before
    fun setUp() {
        dao = FakeArchiveDao()
        repository = ArchiveRepository(dao, FakeStorageManager())
    }

    private fun makeArchive(title: String = "Test", importDate: Date = Date()) = HtmlArchive(
        id = UUID.randomUUID().toString(),
        title = title,
        originalFileName = "test.zip",
        extractPath = "/archives/test",
        sizeBytes = 512L,
        importDate = importDate,
        lastAccessed = importDate
    )

    @Test
    fun insertMultiple_allRetrievable() = runTest {
        val archives = (1..5).map { makeArchive("Archive $it") }
        archives.forEach { repository.insertArchive(it) }
        val list = repository.getAllArchives().first()
        assertEquals(5, list.size)
    }

    @Test
    fun insertThenDelete_removedFromList() = runTest {
        val archive = makeArchive("To Delete")
        repository.insertArchive(archive)
        repository.deleteArchive(archive)
        val list = repository.getAllArchives().first()
        assertTrue(list.none { it.id == archive.id })
    }

    @Test
    fun insertDuplicate_replacesExisting() = runTest {
        val archive = makeArchive("Original")
        repository.insertArchive(archive)
        val updated = archive.copy(title = "Updated")
        repository.insertArchive(updated)
        val retrieved = repository.getArchiveById(archive.id)
        assertEquals("Updated", retrieved?.title)
        assertEquals(1, repository.getAllArchives().first().size)
    }

    @Test
    fun getById_existingArchive_returnsCorrectData() = runTest {
        val archive = makeArchive("Specific Archive")
        repository.insertArchive(archive)
        val retrieved = repository.getArchiveById(archive.id)
        assertNotNull(retrieved)
        assertEquals(archive.title, retrieved!!.title)
        assertEquals(archive.originalFileName, retrieved.originalFileName)
    }

    @Test
    fun getById_missingArchive_returnsNull() = runTest {
        assertNull(repository.getArchiveById("does-not-exist"))
    }

    @Test
    fun emptyRepository_returnsEmptyList() = runTest {
        val list = repository.getAllArchives().first()
        assertTrue(list.isEmpty())
    }

    @Test
    fun listOrderedByImportDateDescending() = runTest {
        val old = makeArchive("Old", Date(1000L))
        val mid = makeArchive("Mid", Date(5000L))
        val new = makeArchive("New", Date(9000L))
        listOf(old, mid, new).shuffled().forEach { repository.insertArchive(it) }
        val list = repository.getAllArchives().first()
        assertEquals("New", list[0].title)
        assertEquals("Mid", list[1].title)
        assertEquals("Old", list[2].title)
    }
}
