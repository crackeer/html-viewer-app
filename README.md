# Static HTML Browser - Android Application

## Project Overview

This is an Android application that allows users to browse static HTML content stored in ZIP archives. The app provides a simple interface for importing, managing, and viewing HTML content with all associated resources (CSS, JavaScript, images).

## Project Structure

```
StaticHtmlBrowser/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/htmlbrowser/
│   │   │   ├── ui/                    # UI components (Activities, Adapters)
│   │   │   ├── domain/                # Business logic and models
│   │   │   ├── data/                  # Data layer (Repository, Database)
│   │   │   └── utils/                 # Utility classes
│   │   ├── res/                       # Resources (layouts, strings, drawables)
│   │   └── AndroidManifest.xml
│   ├── src/test/                      # Unit tests
│   └── build.gradle.kts
├── gradle/
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Key Features

1. **Archive List Display**: View imported HTML archives with titles and storage sizes
2. **Archive Import**: Import ZIP files containing HTML content
3. **HTML Content Viewing**: View HTML content with all associated resources
4. **Storage Management**: Manage storage usage and delete archives
5. **Error Handling**: Comprehensive error handling with user-friendly messages

## Technical Specifications

- **Language**: Kotlin
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Architecture**: Clean Architecture with MVVM pattern
- **Database**: Room Database for persistence
- **Dependencies**: 
  - AndroidX libraries (Core, AppCompat, RecyclerView, etc.)
  - Material Design 3
  - Room Database
  - Coroutines for async operations
  - zip4j for ZIP file operations

## Setup Instructions

1. Open the project in Android Studio
2. Sync Gradle dependencies
3. Build and run the application

## Implementation Status

This project has been set up with the complete Android Studio project structure including:

- ✅ Project configuration files (build.gradle.kts, settings.gradle.kts)
- ✅ AndroidManifest.xml with required permissions
- ✅ Resource files (layouts, strings, themes, drawables)
- ✅ Kotlin source code structure with packages
- ✅ Basic activity implementations
- ✅ Domain model classes
- ✅ Configuration objects

## Next Steps

The project is ready for implementation of the remaining tasks as outlined in the spec document.