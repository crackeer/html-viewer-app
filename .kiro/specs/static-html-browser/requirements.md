# Requirements Document

## Introduction

Static HTML Browser is an Android application that allows users to browse static HTML content stored in ZIP archives. The app provides a simple interface for importing, managing, and viewing HTML content with all associated resources (CSS, JavaScript, images).

## Glossary

- **Application**: The Static HTML Browser Android application
- **HTML_Archive**: A ZIP file containing an index.html file and associated resources (CSS, JS, images)
- **Archive_List**: The main screen displaying imported HTML archives
- **HTML_Viewer**: The screen that renders HTML content from an archive
- **File_System**: The Android device's file system and storage
- **Storage_Manager**: The component responsible for managing archive storage
- **Archive_Parser**: The component responsible for parsing ZIP archives and extracting metadata
- **UI_Manager**: The component responsible for managing user interface interactions

## Requirements

### Requirement 1: Archive List Display

**User Story:** As a user, I want to see a list of imported HTML archives, so that I can easily browse and select content to view.

#### Acceptance Criteria

1. WHEN the Application starts, THE Archive_List SHALL display all previously imported HTML_Archives
2. FOR EACH HTML_Archive in the list, THE Archive_List SHALL display the archive's title extracted from the first `<title>` element in index.html
3. FOR EACH HTML_Archive in the list, THE Archive_List SHALL display the storage size of the extracted archive folder in megabytes with 2 decimal places
4. WHEN no HTML_Archives are imported, THE Archive_List SHALL display the message "No archives imported" in the center of the screen
5. WHEN new archives are imported, THE Archive_List SHALL refresh automatically within 100ms of import completion. IF refresh takes longer than 100ms, THEN THE Application SHALL treat this as a system failure
6. THE Application SHALL define HTML_Archive as a ZIP file containing an index.html file and associated resources (CSS, JS, images)
7. THE Application SHALL define "imported" as archives that have been successfully extracted to the app's private storage

### Requirement 2: Archive Import Functionality

**User Story:** As a user, I want to import HTML archives from ZIP files, so that I can add new content to browse.

#### Acceptance Criteria

1. WHEN the user taps the add button in Archive_List, THE UI_Manager SHALL open the device's file browser
2. WHEN the user selects a ZIP file, THE Archive_Parser SHALL validate that the file contains an index.html file
3. IF the selected ZIP file does not contain index.html, THEN THE Application SHALL detect this error condition. The system only needs to detect the error condition; it is acceptable if the error message display fails
4. WHEN a valid ZIP file is selected, THE Archive_Parser SHALL extract the title from the first `<title>` element in index.html
5. WHEN a valid ZIP file is selected, THE Storage_Manager SHALL extract the archive to a private application folder
6. WHEN archive extraction completes, THE Archive_List SHALL refresh to include the new archive. THE Archive_List SHALL refresh regardless of whether extraction succeeded or failed
7. THE Application SHALL maintain unique identifiers for each imported archive using UUID v4 format
8. IF the selected file is not a valid ZIP file, THEN THE Application SHALL display the error message "Selected file is not a valid ZIP archive"
9. IF the ZIP file extraction fails due to corruption, THEN THE Application SHALL display the error message "ZIP file is corrupted and cannot be extracted"

### Requirement 3: HTML Content Viewing

**User Story:** As a user, I want to view HTML content with all associated resources, so that I can browse static websites offline.

#### Acceptance Criteria

1. WHEN the user selects an archive from Archive_List, THE Application SHALL open the HTML_Viewer screen
2. THE HTML_Viewer SHALL load and display the index.html file from the selected archive
3. THE HTML_Viewer SHALL load all CSS, JavaScript, and image resources referenced in the HTML that exist within the archive folder
4. THE HTML_Viewer SHALL resolve relative paths within the archive folder structure by prepending the archive's base directory path
5. THE HTML_Viewer SHALL provide navigation controls (back, forward, refresh) for HTML content
6. THE HTML_Viewer SHALL display the archive title in the navigation bar
7. IF the index.html file contains malformed HTML that cannot be parsed, THEN THE HTML_Viewer SHALL display the error message "HTML content is malformed and cannot be displayed"
8. IF a referenced resource (CSS, JS, image) is missing from the archive folder, THEN THE HTML_Viewer SHALL continue loading and display the HTML without that resource
9. WHEN the user taps the back button in HTML_Viewer, THE Application SHALL navigate to the previous page in the WebView history if available, otherwise close HTML_Viewer

### Requirement 4: Storage Management

**User Story:** As a user, I want to manage storage usage, so that I can free up space when needed.

#### Acceptance Criteria

1. FOR EACH HTML_Archive, THE Storage_Manager SHALL calculate the size of the extracted folder whenever the archive is displayed in Archive_List
2. THE Archive_List SHALL display the calculated storage size for each archive in megabytes with 2 decimal places
3. WHEN the user initiates delete action on an archive, THE Application SHALL request confirmation with the message "Delete this archive? This action cannot be undone."
4. WHEN delete is confirmed, THE Storage_Manager SHALL remove the archive's extracted folder and all its contents
5. WHEN delete completes, THE Archive_List SHALL remove the archive from display within 100ms
6. THE Application SHALL update total storage usage display after delete operations by recalculating the sum of all remaining archive sizes
7. IF the user cancels the delete confirmation, THEN THE Application SHALL not delete the archive
8. THE Application SHALL display storage sizes using the format "X.XX MB" where X.XX is the size in megabytes with 2 decimal places

### Requirement 5: Error Handling and Validation

**User Story:** As a user, I want clear error messages when things go wrong, so that I can understand and resolve issues.

#### Acceptance Criteria

1. IF the file browser fails to open, THEN THE Application SHALL display the error message "Cannot open file browser. Please check storage permissions."
2. IF ZIP file extraction fails, THEN THE Application SHALL display an error message containing the specific failure reason (e.g., "Extraction failed: file is corrupted")
3. IF index.html cannot be parsed for title, THEN THE Application SHALL use the default title "Untitled Archive"
4. IF storage space is insufficient for extraction, THEN THE Application SHALL display the error message "Insufficient storage space. Please free up at least X MB."
5. IF HTML content fails to load in HTML_Viewer, THEN THE Application SHALL display the error message "Failed to load HTML content: [specific error]"
6. THE Application SHALL handle archives of any size without file size limitations. Storage availability is the only constraint.

### Requirement 6: User Interface Requirements

**User Story:** As a user, I want an intuitive and responsive interface, so that I can easily use the application.

#### Acceptance Criteria

1. THE Archive_List SHALL display archives in a scrollable list with at least 8dp spacing between items
2. THE add button SHALL be positioned in the bottom-right corner of Archive_List with 16dp margins from edges
3. EACH archive entry in Archive_List SHALL display: title, storage size in MB, and a delete icon button
4. THE Application SHALL maintain consistent visual design across all screens using Material Design 3 components
5. THE Application SHALL display a circular progress indicator during file operations that take longer than 500ms
6. THE Application SHALL be responsive to different screen sizes and orientations by using ConstraintLayout
7. IF the user attempts to delete an archive while import is in progress, THEN THE Application SHALL display the error message "Cannot delete archive while import is in progress"
8. THE Archive_List SHALL support both portrait and landscape orientations without losing functionality
9. THE Application SHALL provide haptic feedback when the user taps the add button or delete icon

### Requirement 7: Performance Requirements

**User Story:** As a user, I want the application to perform well, so that I have a smooth experience.

#### Acceptance Criteria

1. WHEN loading Archive_List, THE Application SHALL display the list within 500ms on a device with at least 2GB RAM
2. WHEN importing an archive under 10MB, THE Application SHALL complete extraction within 5 seconds on a device with at least 2GB RAM
3. WHEN opening HTML_Viewer, THE Application SHALL load and display HTML content within 2 seconds on a device with at least 2GB RAM
4. THE Application SHALL handle archives of any size without crashing, limited only by available device storage. This requirement is focused only on size handling; edge cases like empty archives or corrupted files are not covered
5. THE Application SHALL limit WebView memory usage to 50MB during HTML content rendering
6. IF archive extraction takes longer than 10 seconds, THEN THE Application SHALL display a progress dialog with estimated time remaining. The progress dialog may appear even for extractions that complete instantly
7. THE Application SHALL cache archive metadata in memory to achieve the 500ms Archive_List load time

### Requirement 8: Data Persistence

**User Story:** As a user, I want my imported archives to persist between app sessions, so that I don't lose my content.

#### Acceptance Criteria

1. THE Application SHALL persist archive metadata (title, path, size) between application sessions using Room database
2. THE Application SHALL persist extracted archive folders between application sessions in app-private storage
3. WHEN the Application is reinstalled, THE Application SHALL attempt to migrate existing archive data if the user grants permission
4. THE Application SHALL provide export functionality that creates a backup file containing archive metadata
5. IF data migration fails during reinstallation, THEN THE Application SHALL display the error message "Could not migrate existing data. Archives will need to be re-imported." This applies even when the user granted permission for migration
6. THE Application SHALL store archive metadata in a Room database with automatic backup to device storage