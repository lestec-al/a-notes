# ANotes - Android & PC App

<img title="Android version screenshots"
src="https://github.com/lestec-al/a-notes/raw/main/readme_logo.png"/>

<img title="PC version screenshots"
src="https://github.com/lestec-al/a-notes/raw/main/readme_logo_pc.png"/>

## Features
- Manage notes
- Manage ordered (with statuses) notes (test stage, may have bugs)
- Cloud sync of data
- One note widget (for Android)
- Light & Dark theme of interface
- Dynamic, automatically adjusting interface colors to match the device theme (for Android 12+)

## Tech Stack
- Android Studio
- Kotlin
- Jetpack Compose (with Navigation) on Android, Compose for Desktop on PC
- SQLite (through Room on Android & through SQLDelight on PC)
- Google Cloud with Google Drive API

## Other
- Both apps have some common code (e.g., most of the UI and cloud services), but also have some differences (e.g., database implementations)
- I am trying to follow recommendations from [Android App Architecture](https://developer.android.com/topic/architecture#recommended-app-arch) while building this apps