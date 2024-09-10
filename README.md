# ANotes - Android & PC Apps

<img title="Android version screenshots"
src="https://github.com/lestec-al/a-notes/raw/two-apps/screenshots_android.png"/>

<img title="PC version screenshots"
src="https://github.com/lestec-al/a-notes/raw/two-apps/screenshots_desktop.png"/>

Screenshots from versions of the "[two-apps](https://github.com/lestec-al/a-notes/tree/two-apps?tab=readme-ov-file)" branch. Apps from this project may look slightly different.

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
- Kotlin Multiplatform (with Compose Multiplatform UI Framework)
- SQLite (through SQLDelight)
- Google Cloud with Google Drive API

## Other
- This is the Kotlin Multiplatform Project - one project for multiple OSes (Android & PC). Most of the UI and business logic are shared
- If you want to create your own executables, to enable sync, you need to create your own Google Cloud Project and create an OAuth credentials. For the Desktop version, download the credential, rename it to "auth.json" and move to "composeApp/src/desktopMain/resources/"
- I am trying to follow recommendations from [Android App Architecture](https://developer.android.com/topic/architecture#recommended-app-arch) while building this apps