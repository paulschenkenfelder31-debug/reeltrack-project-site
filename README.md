# ReelTrack project website

Official project page for ReelTrack, a private Android film tracker for discovery, watchlists, ratings, reviews and a viewing diary.

## Publish with GitHub Pages

The repository includes an automatic GitHub Pages workflow. Push to `main`, then open **Settings → Pages** in GitHub and select **GitHub Actions** as the source if it is not already selected. Every later push to `main` publishes the newest version.

The expected public address is:

`https://YOUR-GITHUB-USERNAME.github.io/reeltrack-project-site/`

Use that public address as the ReelTrack application URL when registering a movie-data API.

## Local setup

Requires Node.js 22 or newer.

```bash
npm ci
npm run build
```

The static site output is generated in `dist/client`.

## Android app and included download

The Android source lives in `android/`. GitHub Actions builds ReelTrack v3.2, publishes the installable APK as a workflow artifact, and includes it in the deployed website at `public/downloads/ReelTrack-v3.2.apk`.

The Android app calls the restricted TMDB proxy in `worker/index.ts`. Set `TMDB_READ_ACCESS_TOKEN` as a secret environment variable on the ReelTrack Sites project. The token is never stored in Git or compiled into the public APK.

## Data and privacy

ReelTrack uses TMDB for movie search, metadata, posters, credits and IMDb identifiers. Watchlists, ratings, diary entries and private reviews are kept on the Android device.
