# ReelTrack Android

ReelTrack is a local-first Android movie tracker for Android 6.0 and newer. Version 4.3 adds an optional Netflix playback tracker: after the user enables Android Notification access, ReelTrack reads only Netflix media-session metadata Android exposes, such as title, subtitle, playback state and position when available. No Netflix login, cookie or account data is used.

## Movie service

The app calls the restricted `/api/tmdb` proxy on the ReelTrack Sites deployment. The TMDB token stays in that server's secret environment and is never compiled into the public APK.

```bash
gradle --no-daemon assembleDebug
```

GitHub Actions builds the APK, uploads it as a workflow artifact, and packages it with the website without needing an API secret.

## Attribution

This product uses the TMDB API but is not endorsed or certified by TMDB.
