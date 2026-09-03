# ReelTrack Android

ReelTrack is a local-first Android movie tracker for Android 6.0 and newer. Version 4.2 adds a local Netflix companion: optional Android Usage Access shows Netflix time from the past 24 hours, while titles can be explicitly marked as watched on Netflix from a film detail page.

## Movie service

The app calls the restricted `/api/tmdb` proxy on the ReelTrack Sites deployment. The TMDB token stays in that server's secret environment and is never compiled into the public APK.

```bash
gradle --no-daemon assembleDebug
```

GitHub Actions builds the APK, uploads it as a workflow artifact, and packages it with the website without needing an API secret.

## Attribution

This product uses the TMDB API but is not endorsed or certified by TMDB.
