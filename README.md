# ANotes - Android & PC Apps

- This branch is for the Kotlin Multiplatform version of the app. See the "main" branch for stable apps.
The main difference is that there is now one project for multiple OSes (Android and PC). Most of the UI and business logic are shared.
- If you want to create your own executables, to enable sync, you need to create your own Google Cloud Project and create an OAuth credentials. For the Desktop version, download the credential, rename it to "auth.json" and move to "composeApp/src/desktopMain/resources/"

## Tech Stack
- Android Studio
- Kotlin
- Kotlin Multiplatform (with Compose Multiplatform UI Framework)
- SQLite (through SQLDelight)
- Google Cloud with Google Drive API