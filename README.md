# ANotes - Android & PC App

<img title="Android version screenshots" src="https://github.com/lestec-al/a-notes/raw/main/readme_logo.png" width="540" height="524"/>

<img title="PC version screenshots" src="https://github.com/lestec-al/a-notes/raw/main/readme_logo_pc.png" width="540" height="412"/>

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
- Google Cloud with Google Drive API (you need to create and configure your own Google Cloud Project)

## Tech Stack PC
- IntelliJ IDEA
- Kotlin
- Compose for Desktop
- SQLite (through SQLDelight)
- Google Cloud with Google Drive API (you need to create and configure your own Google Cloud Project & copy your credentials (rename to auth.json) to "src/main/resources" folder)

## Other
- Both apps have some common code (e.g., most of the UI and cloud services), but also have some differences (e.g., database implementations)
- I am trying to follow recommendations from [Android App Architecture](https://developer.android.com/topic/architecture#recommended-app-arch) while building this apps