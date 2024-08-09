# ANotes - Android & PC App

<img title="Android version screenshots"
src="https://github.com/lestec-al/a-notes/raw/main/readme_logo.png"/>

<img title="PC version screenshots"
src="https://github.com/lestec-al/a-notes/raw/main/readme_logo_pc.png"/>

## Features
- Manage notes
- Manage tasks (test stage, may have bugs)
- Cloud sync of notes
- One note widget (for Android)
- Light and Dark theme
- Dynamic colors (for Android 12+)

## Tech Stack Android
- Android Studio
- Kotlin
- Jetpack Compose (with Navigation)
- SQLite (through Room)
- Google Cloud with Google Drive API

## Tech Stack PC
- IntelliJ IDEA
- Kotlin
- Compose for Desktop
- SQLite (through SQLDelight)
- Google Cloud with Google Drive API

## Other
- Both apps have some common code (e.g., most of the UI and cloud services), but also have some differences (e.g., database implementations)
- I am trying to follow recommendations from [Android App Architecture](https://developer.android.com/topic/architecture#recommended-app-arch) while building this apps