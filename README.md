# NEXUS Launcher

Experimental Android home launcher built with Kotlin and Jetpack Compose, designed for phones and foldables.

## Current MVP
- Android HOME launcher intent and app drawer
- Responsive adaptive app grid for narrow and unfolded screens
- Three visual palettes (Nexus, Cyberpunk, Minimal)
- App search and launch via package manager
- GitHub Actions debug APK build on each push

## Install
1. Open **Actions → Android APK** and choose a successful run.
2. Download the **nexus-launcher-debug** artifact and extract the APK.
3. Install on Android and select NEXUS as your default Home app in Android settings.

> This is an early MVP, not yet a production launcher. Features such as gyro parallax, desktop freeform windows, widgets, custom gestures, icon rearrangement and smooth fold transitions are future work. Android and OEM restrictions apply to external-app windows.

## Local build
Requires JDK 17, Android SDK 35 and Gradle 8.9.

```bash
gradle :app:assembleDebug
```

## License
No license selected yet.
