package com.example.htmlbrowser.data

import com.example.htmlbrowser.data.database.ArchiveDao
import com.example.htmlbrowser.data.database.ArchiveEntity
import com.example.htmlbrowser.data.database.toDomain
import com.example.htmlbrowser.data.database.toEntity
import com.example.htmlbrowser.data.repository.ArchiveRepository
import com.example.htmlbrowser.domain.manager.StorageManager
import com.example.htmlbrowser.domain.model.HtmlArchive
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
 * In-memory fake DAO for testing ArchiveRepository without a real Room database.
 */
class FakeArchiveDao : ArchiveDao {
    private val store = mutableListOf<ArchiveEntity>()
    private val flow = MutableStateFlow<List<ArchiveEntity>>(emptyList())

    override fun getAllArchives(): Flow<List<ArchiveEntity>> =
        flow.map { it.sortedByDescending { e -> e.importDate } }

    override suspend fun insertArchive(archive: ArchiveEntity) {
        store.removeAll { it.id == archive.id }
        store.add(archive)
        flow.value = store.toList()
    }

    override suspend fun deleteArchive(archive: ArchiveEntity) {
        store.removeAll { it.id == archive.id }
        flow.value = store.toList()
    }

    override suspend fun deleteArchiveById(archiveId: String) {
        store.removeAll { it.id == archiveId }
        flow.value = store.toList()
    }

    override suspend fun getArchiveById(archiveId: String): ArchiveEntity? =
        store.firstOrNull { it.id == archiveId }
}

/**
 * Fake StorageManager that doesn't touch the filesystem.
 */
class FakeStorageManager : StorageManager(null!!) {
    override fun calculateArchiveSize(archiveId: String): Long = 1024L
    override suspend fun deleteArchive(archiveId: String): Boolean = true
}

class ArchiveRepositoryTest {

    private lateinit var dao: FakeArchiveDao
    private lateinit var repository: ArchiveRepository

    @Before
    fun setUp() {
        dao = FakeArchiveDao()
        repository = ArchiveRepository(dao, FakeStorageManager())
    }

    private fun makeArchive(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Archive",
        importDate: Date = Date()
    ) = HtmlArchive(
        id = id,
        title = title,
        originalFileName = "test.zip",
        extractPath = "/data/archives/$id",
        sizeBytes = 1024L,
        importDate = importDate,
        lastAccessed = importDate
    )

    // ─── Basic CRUD ──────────────────────────────────────────────────────────────

    @Test
    fun insertAndRetrieve_roundTrip() = runTest {
        val archive = makeArchive(title = "Round Trip Test")
        repository.insertArchive(archive)
        val retrieved = repository.getArchiveById(archive.id)
        assertNotNull(retrieved)
        assertEquals(archive.id, retrieved!!.id)
        assertEquals(archive.title, retrieved.title)
    }

    @Test
    fun getArchiveById_notFound_returnsNull() = runTest {
        assertNull(repository.getArchiveById("nonexistent-id"))
    }

    @Test
    fun deleteArchive_removesFromList() = runTest {
        val archive = makeArchive()
        repository.insertArchive(archive)
        repository.deleteArchive(archive)
        assertNull(repository.getArchiveById(archive.id))
    }

    @Test
    fun getAllArchives_returnsInsertedArchives() = runTest {
        val a1 = makeArchive(title = "First")
        val a2 = makeArchive(title = "Second")
        repository.insertArchive(a1)
        repository.insertArchive(a2)
        val list = repository.getAllArchives().first()
        assertEquals(2, list.size)
    }

    // ─── Ordering invariant ──────────────────────────────────────────────────────

    @Test
    fun getAllArchives_orderedByImportDateDescending() = runTest {
        val older = makeArchive(title = "Older", importDate = Date(1000L))
        val newer = makeArchive(title = "Newer", importDate = Date(9000L))
        repository.insertArchive(older)
        repository.insertArchive(newer)
        val list = repository.getAllArchives().first()
        assertTrue(list[0].importDate.time >= list[1].importDate.time)
    }

    // ─── Property-Based Tests ────────────────────────────────────────────────────

    @Test
    fun pbt_4_1_insertThenGet_roundTrip() = runTest {
        forAll(Arb.string(1..50)) { title ->
            val archive = makeArchive(title = title)
            repository.insertArchive(archive)
            val retrieved = repository.getArchiveById(archive.id)
            repository.deleteArchive(archive) // cleanup
            retrieved?.title == title
        }
    }

    @Test
    fun pbt_4_2_archiveListOrderedByImportDate() = runTest {
        forAll(Arb.list(Arb.long(1L..Long.MAX_VALUE), 2..5)) { timestamps ->
            // Reset DAO
            dao = FakeArchiveDao()
            repository = ArchiveRepository(dao, FakeStorageManager())

            timestamps.forEach { ts ->
                repository.insertArchive(makeArchive(importDate = Date(ts)))
            }
            val list = repository.getAllArchives().first()
            list.zipWithNext().all { (a, b) -> a.importDate.time >= b.importDate.time }
        }
    }

    @Test
    fun pbt_4_3_uniqueIds_invariant() = runTest {
        forAll(Arb.list(Arb.string(1..20), 2..10)) { titles ->
            dao = FakeArchiveDao()
            repository = ArchiveRepository(dao, FakeStorageManager())

            titles.forEach { title ->
                repository.insertArchive(makeArchive(title = title))
            }
            val list = repository.getAllArchives().first()
            list.map { it.id }.distinct().size == list.size
        }
    }
}
