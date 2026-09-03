# ReelTrack Android

ReelTrack is a local-first Android movie tracker for Android 6.0 and newer. Version 4.1 adds a charcoal-and-green data-app theme, icon-led navigation and persistent Violet, Ocean and Amber theme options in Settings.

## Movie service

The app calls the restricted `/api/tmdb` proxy on the ReelTrack Sites deployment. The TMDB token stays in that server's secret environment and is never compiled into the public APK.

```bash
gradle --no-daemon assembleDebug
```

GitHub Actions builds the APK, uploads it as a workflow artifact, and packages it with the website without needing an API secret.

## Attribution

This product uses the TMDB API but is not endorsed or certified by TMDB.
