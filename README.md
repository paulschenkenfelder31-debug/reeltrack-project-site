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

## Included download

The site currently distributes ReelTrack v2.0 for Android 6.0 and newer. Replace `public/downloads/ReelTrack-v2.0.apk` when shipping a new signed version, then update the visible version number and download links in `app/page.tsx`.

## Data and privacy

The current Android beta searches Wikidata and may display Wikimedia Commons imagery. Watchlists, ratings, diary entries and private reviews are kept on the Android device. TMDB integration is planned for richer metadata, posters, credits, trailers, recommendations and regional provider availability.
