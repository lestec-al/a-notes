# ANotes - Android & PC Apps

This branch consists of 2 separate projects for Android & PC of the Notes app. See the "main" branch for up-to-date information.

<img title="Android version screenshots"
src="https://github.com/lestec-al/a-notes/raw/two-apps/screenshots_android.png"/>

<img title="PC version screenshots"
src="https://github.com/lestec-al/a-notes/raw/two-apps/screenshots_desktop.png"/>

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
- Both projects have some similar code (e.g., most of the UI), but also have some differences (e.g., database implementations)
- If you want to create your own executables, to enable sync, you need to create your own Google Cloud Project and create an OAuth credentials. For the Desktop version, download the credential, rename it to "auth.json" and move to "ANotesDesktop/src/main/resources/"
- I am trying to follow recommendations from [Android App Architecture](https://developer.android.com/topic/architecture#recommended-app-arch) while building this apps