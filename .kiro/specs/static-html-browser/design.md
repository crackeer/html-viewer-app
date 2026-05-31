# Design Document

## Introduction

This document outlines the technical design for the Static HTML Browser Android application. The design follows Android best practices and implements the requirements specified in the requirements document.

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Static HTML Browser App                   │
├─────────────────────────────────────────────────────────────┤
│  Presentation Layer (UI Components)                          │
│  ├── ArchiveListActivity                                     │
│  ├── HtmlViewerActivity                                      │
│  └── Common UI Components (Dialogs, Adapters, etc.)         │
│                                                              │
│  Domain Layer (Business Logic)                               │
│  ├── ArchiveManager                                          │
│  ├── StorageManager                                          │
│  ├── HtmlParser                                             │
│  └── FileSystemHelper                                        │
│                                                              │
│  Data Layer (Persistence)                                    │
│  ├── ArchiveRepository                                       │
│  ├── PreferencesManager                                      │
│  └── File Storage (Internal/External)                       │
└─────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

1. **ArchiveListActivity**: Main activity displaying list of imported archives
2. **HtmlViewerActivity**: Activity for viewing HTML content
3. **ArchiveManager**: Manages archive import, extraction, and metadata extraction
4. **StorageManager**: Handles file operations and storage calculations
5. **HtmlParser**: Parses HTML files to extract titles and metadata
6. **FileSystemHelper**: Abstracts file system operations
7. **ArchiveRepository**: Manages persistence of archive metadata
8. **PreferencesManager**: Manages application preferences and settings

## Technical Design

### 1. Archive Management

#### Archive Import Flow
```
User taps add button → File picker opens → User selects ZIP file → 
Validation (index.html check) → Title extraction → 
Extraction to app storage → Metadata persistence → List refresh
```

#### Data Structures
```kotlin
data class HtmlArchive(
    val id: String,           // UUID for unique identification
    val title: String,        // Extracted from index.html
    val originalFileName: String, // Original ZIP file name
    val extractPath: String,  // Path to extracted folder
    val sizeBytes: Long,      // Size in bytes
    val importDate: Date,     // When archive was imported
    val lastAccessed: Date    // When archive was last viewed
)

data class ArchiveImportResult(
    val success: Boolean,
    val archive: HtmlArchive?,
    val errorMessage: String?
)
```

### 2. Storage Management

#### Storage Locations
- **Internal Storage**: `appContext.filesDir + "/archives/{archive_id}/"`
- **Metadata Database**: Room database for archive metadata
- **Preferences**: SharedPreferences for app settings

#### Storage Calculation
```kotlin
class StorageManager(private val context: Context) {
    fun calculateArchiveSize(archiveId: String): Long {
        val archiveDir = File(context.filesDir, "archives/$archiveId")
        return calculateDirectorySize(archiveDir)
    }
    
    private fun calculateDirectorySize(directory: File): Long {
        return directory.walk()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
}
```

### 3. HTML Viewing

#### WebView Configuration
```kotlin
class HtmlViewerActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
            
            webViewClient = LocalWebViewClient()
        }
        
        // Load HTML from archive directory
        val archiveId = intent.getStringExtra("archive_id")
        val archivePath = getArchivePath(archiveId)
        val htmlFile = File(archivePath, "index.html")
        webView.loadUrl("file://${htmlFile.absolutePath}")
    }
    
    private inner class LocalWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            // Handle local file URLs within archive directory
            return false
        }
    }
}
```

### 4. User Interface Design

#### ArchiveListActivity Layout
```xml
<!-- activity_archive_list.xml -->
<androidx.coordinatorlayout.widget.CoordinatorLayout>
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/archive_list"
        app:layoutManager="LinearLayoutManager" />
    
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_add"
        android:layout_gravity="bottom|end"
        android:src="@drawable/ic_add" />
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

#### Archive Item Layout
```xml
<!-- item_archive.xml -->
<androidx.cardview.widget.CardView>
    <LinearLayout>
        <TextView android:id="@+id/title" />
        <TextView android:id="@+id/size" />
        <TextView android:id="@+id/date" />
        <ImageButton android:id="@+id/btn_delete" />
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

### 5. Database Design

#### Room Database Schema
```kotlin
@Entity(tableName = "archives")
data class ArchiveEntity(
    @PrimaryKey val id: String,
    val title: String,
    val originalFileName: String,
    val extractPath: String,
    val sizeBytes: Long,
    val importDate: Long,
    val lastAccessed: Long
)

@Dao
interface ArchiveDao {
    @Query("SELECT * FROM archives ORDER BY importDate DESC")
    fun getAllArchives(): Flow<List<ArchiveEntity>>
    
    @Insert
    suspend fun insertArchive(archive: ArchiveEntity)
    
    @Delete
    suspend fun deleteArchive(archive: ArchiveEntity)
    
    @Query("DELETE FROM archives WHERE id = :archiveId")
    suspend fun deleteArchiveById(archiveId: String)
}
```

### 6. File Operations

#### ZIP Extraction
```kotlin
class ArchiveManager(private val context: Context) {
    suspend fun importArchive(zipUri: Uri): ArchiveImportResult {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Copy ZIP to temp location
                val tempZip = createTempZipFile(zipUri)
                
                // 2. Validate ZIP contains index.html
                if (!validateZipContainsIndexHtml(tempZip)) {
                    return@withContext ArchiveImportResult(
                        success = false,
                        archive = null,
                        errorMessage = "ZIP file must contain index.html"
                    )
                }
                
                // 3. Extract title from index.html
                val title = extractTitleFromZip(tempZip)
                
                // 4. Generate unique ID and extraction path
                val archiveId = UUID.randomUUID().toString()
                val extractDir = File(context.filesDir, "archives/$archiveId")
                
                // 5. Extract ZIP contents
                extractZipFile(tempZip, extractDir)
                
                // 6. Calculate size
                val size = calculateDirectorySize(extractDir)
                
                // 7. Create archive object
                val archive = HtmlArchive(
                    id = archiveId,
                    title = title,
                    originalFileName = getFileNameFromUri(zipUri),
                    extractPath = extractDir.absolutePath,
                    sizeBytes = size,
                    importDate = Date(),
                    lastAccessed = Date()
                )
                
                ArchiveImportResult(success = true, archive = archive, errorMessage = null)
            } catch (e: Exception) {
                ArchiveImportResult(success = false, archive = null, errorMessage = e.message)
            }
        }
    }
}
```

### 7. Error Handling

#### Error Types
```kotlin
sealed class AppError {
    data class FileNotFound(val message: String) : AppError()
    data class InvalidArchive(val message: String) : AppError()
    data class StorageFull(val message: String) : AppError()
    data class NetworkError(val message: String) : AppError()
    data class UnknownError(val message: String) : AppError()
}

class ErrorHandler {
    fun handleError(error: AppError): String {
        return when (error) {
            is AppError.FileNotFound -> "File not found: ${error.message}"
            is AppError.InvalidArchive -> "Invalid archive: ${error.message}"
            is AppError.StorageFull -> "Storage is full. Please free up space."
            is AppError.NetworkError -> "Network error: ${error.message}"
            is AppError.UnknownError -> "An error occurred: ${error.message}"
        }
    }
}
```

## Implementation Details

### Dependencies
```gradle
dependencies {
    // AndroidX
    implementation "androidx.core:core-ktx:1.12.0"
    implementation "androidx.appcompat:appcompat:1.6.1"
    implementation "androidx.constraintlayout:constraintlayout:2.1.4"
    implementation "androidx.recyclerview:recyclerview:1.3.2"
    implementation "androidx.cardview:cardview:1.0.0"
    
    // Material Design
    implementation "com.google.android.material:material:1.11.0"
    
    // Room Database
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    kapt "androidx.room:room-compiler:2.6.1"
    
    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
    
    // Lifecycle
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.7.0"
    
    // File operations
    implementation "net.lingala.zip4j:zip4j:2.11.5"
}
```

### Permissions
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" 
    android:minSdkVersion="30" />
```

### Configuration
```kotlin
object AppConfig {
    // Note: File size limit has been removed per user request
    const val SUPPORTED_FILE_TYPES = arrayOf("application/zip", "application/x-zip-compressed")
    const val ARCHIVE_DIR_NAME = "archives"
    const val DATABASE_NAME = "html_archive.db"
}
```

## Testing Strategy

### Unit Tests
1. **ArchiveManagerTest**: Test archive import, extraction, and validation
2. **StorageManagerTest**: Test storage calculations and file operations
3. **HtmlParserTest**: Test HTML title extraction
4. **ArchiveRepositoryTest**: Test database operations

### Integration Tests
1. **ArchiveImportFlowTest**: Test complete import flow with UI
2. **HtmlViewingTest**: Test HTML rendering with resources
3. **StorageManagementTest**: Test delete and storage operations

### UI Tests
1. **ArchiveListUITest**: Test list display and interactions
2. **HtmlViewerUITest**: Test HTML viewing functionality
3. **ErrorHandlingUITest**: Test error scenarios and user feedback

## Security Considerations

1. **File Validation**: Validate ZIP files before extraction to prevent zip bombs
2. **Path Traversal**: Sanitize file paths to prevent directory traversal attacks
3. **WebView Security**: Configure WebView securely to prevent XSS attacks
4. **Storage Isolation**: Use app-private storage for extracted files
5. **Input Validation**: Validate all user inputs and file contents

## Performance Considerations

1. **Background Processing**: Perform file operations in background threads
2. **Memory Management**: Use efficient data structures and clear references
3. **Database Optimization**: Use Room database with proper indexing
4. **File Caching**: Cache frequently accessed files and metadata
5. **Lazy Loading**: Load archive metadata lazily in lists

## Accessibility

1. **Content Description**: Provide content descriptions for all UI elements
2. **Keyboard Navigation**: Support keyboard navigation for all interactive elements
3. **Text Scaling**: Support dynamic text scaling
4. **Color Contrast**: Maintain sufficient color contrast ratios
5. **Screen Reader Support**: Ensure compatibility with screen readers

## Correctness Properties

Based on the prework analysis, the following property-based tests should be implemented:

### 1. Archive Import Properties

**Property 1.1: Valid ZIP with index.html should import successfully**
- **Type**: Round-trip property
- **Description**: For all valid ZIP files containing index.html, import should succeed and produce a valid HtmlArchive object
- **Test**: `importArchive(validZip) → success = true ∧ archive ≠ null`
- **Generator**: Generate ZIP files with various HTML structures, file sizes, and directory depths

**Property 1.2: Invalid ZIP without index.html should fail with error**
- **Type**: Error condition property
- **Description**: For all ZIP files missing index.html, import should fail with appropriate error message
- **Test**: `importArchive(zipWithoutIndex) → success = false ∧ errorMessage contains "index.html"`
- **Generator**: Generate ZIP files with various contents but no index.html

**Property 1.3: Title extraction should preserve HTML title**
- **Type**: Model-based property
- **Description**: Extracted title should match the `<title>` tag content in index.html
- **Test**: `extractTitleFromZip(zip) = parseTitleFromHtml(indexHtmlContent)`
- **Generator**: Generate HTML files with various title formats (empty, long, special characters)

### 2. Storage Management Properties

**Property 2.1: Directory size calculation should be accurate**
- **Type**: Model-based property
- **Description**: Calculated directory size should equal sum of all file sizes
- **Test**: `calculateDirectorySize(dir) = sum(file.length() for file in dir.walk().filter { it.isFile })`
- **Generator**: Generate directory structures with various file sizes and nesting levels

**Property 2.2: Delete operation should be idempotent**
- **Type**: Idempotence property
- **Description**: Deleting an archive twice should have the same effect as deleting it once
- **Test**: `deleteArchive(id); deleteArchive(id) ≡ deleteArchive(id)`
- **Generator**: Generate archive IDs and test delete operations

**Property 2.3: Storage calculation invariant**
- **Type**: Invariant property
- **Description**: After deleting an archive, total storage used should decrease by archive size
- **Test**: `totalBefore = totalStorage(); deleteArchive(id); totalAfter = totalStorage(); totalAfter = totalBefore - archiveSize(id)`
- **Generator**: Generate multiple archives with varying sizes

### 3. Error Handling Properties

**Property 3.1: Error messages should be non-empty for failures**
- **Type**: Invariant property
- **Description**: All error conditions should produce non-empty error messages
- **Test**: `∀ error ∈ AppError: handleError(error).isNotEmpty()`
- **Generator**: Generate all possible error types with various messages

**Property 3.2: Error handling should be deterministic**
- **Type**: Invariant property
- **Description**: Same error input should always produce same error message output
- **Test**: `handleError(error) = handleError(error)` (same input produces same output)
- **Generator**: Generate error objects and test multiple invocations

### 4. Data Persistence Properties

**Property 4.1: Database round-trip property**
- **Type**: Round-trip property
- **Description**: Saving then loading an archive should produce equivalent data
- **Test**: `saveArchive(archive); loaded = loadArchive(archive.id); archive ≡ loaded`
- **Generator**: Generate HtmlArchive objects with various field values

**Property 4.2: Archive list ordering invariant**
- **Type**: Invariant property
- **Description**: Archives should always be ordered by import date (newest first)
- **Test**: `∀ i in 0..n-2: getAllArchives()[i].importDate >= getAllArchives()[i+1].importDate`
- **Generator**: Generate archives with various import dates

**Property 4.3: Unique ID invariant**
- **Type**: Invariant property
- **Description**: All archives should have unique IDs
- **Test**: `∀ archives: archives.map { it.id }.distinct().size = archives.size`
- **Generator**: Generate multiple archive objects

### 5. File Operations Properties

**Property 5.1: ZIP extraction completeness**
- **Type**: Invariant property
- **Description**: All files from ZIP should be extracted to destination directory
- **Test**: `extractZip(zip, dest); zipEntries = listZipEntries(zip); extractedFiles = listFiles(dest); zipEntries ≡ extractedFiles`
- **Generator**: Generate ZIP files with various file structures

**Property 5.2: File preservation during extraction**
- **Type**: Invariant property
- **Description**: File contents should be preserved during extraction
- **Test**: `extractZip(zip, dest); ∀ file in dest.walk(): file.contentEquals(originalContentInZip)`
- **Generator**: Generate ZIP files with various file contents

### Property Test Implementation Guidelines

```kotlin
// Example property test using Kotest Property Testing
class ArchiveManagerPropertyTest : StringSpec({
    "Valid ZIP with index.html should import successfully" {
        forAll(validZipArb) { zipBytes ->
            val result = archiveManager.importArchive(zipBytes)
            result.success && result.archive != null
        }
    }
    
    "Directory size calculation should be accurate" {
        forAll(directoryStructureArb) { directory ->
            val calculated = storageManager.calculateDirectorySize(directory)
            val actual = directory.walk()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
            calculated == actual
        }
    }
    
    "Delete operation should be idempotent" {
        forAll(archiveIdArb) { archiveId ->
            // First delete
            val result1 = storageManager.deleteArchive(archiveId)
            // Second delete
            val result2 = storageManager.deleteArchive(archiveId)
            // Results should be equivalent
            result1 == result2
        }
    }
})
```

### Property Test Generators

```kotlin
// Example generators for property tests
val validZipArb: Arb<ByteArray> = arb {
    // Generate ZIP bytes with index.html and various resources
    generateZipWithHtml(
        title = it.nextString(1..50),
        files = it.nextList(0..10) { 
            FileEntry(
                name = it.nextString(1..20) + it.nextFrom(listOf(".css", ".js", ".png", ".jpg")),
                content = it.nextBytes(0..1024)
            )
        }
    )
}

val directoryStructureArb: Arb<File> = arb {
    // Generate directory structure with random files
    val tempDir = createTempDir()
    val numFiles = it.nextInt(0..20)
    repeat(numFiles) {
        val file = File(tempDir, "file_${it.nextInt()}")
        file.writeBytes(it.nextBytes(0..10240))
    }
    tempDir
}

val archiveIdArb: Arb<String> = arb {
    UUID.randomUUID().toString()
}
```