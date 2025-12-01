# ANotes - Android & PC Apps

<img title="Android version screenshots"
src="https://github.com/lestec-al/a-notes/raw/main/screenshots_android.png"/>

<img title="PC version screenshots"
src="https://github.com/lestec-al/a-notes/raw/main/screenshots_desktop.png"/>

## Features
- Notes
- Ordered (with statuses) notes
- Drawings
- Swipe-to-Organize Notes (a two-column view where you can swipe notes left or right to sort them into categories, like "In Process" and "Done." Each side has a distinct color that you can customize for easy visual recognition)
- Cloud sync of data
- One note widget (for Android)
- Light & Dark theme of interface
- Dynamic, automatically adjusting interface colors to match the device theme (for Android 12+)

## Tech Stack
- Android SDK
- Kotlin
- Kotlin Multiplatform (with Compose Multiplatform UI Framework)
- SQLite (through SQLDelight)
- DataStore
- Google Cloud with Google Drive API

## Other
- This is the Kotlin Multiplatform Project - one project for multiple OSes (Android & PC). Most of the UI and business logic are shared
- If you want to create your own executables, to enable sync, you need to create your own Google Cloud Project and create an OAuth credentials. For the Desktop version, download the credential, rename it to "auth.json" and move to "composeApp/src/desktopMain/resources/"
- I am trying to follow recommendations from [Android App Architecture](https://developer.android.com/topic/architecture#recommended-app-arch) while building this apps