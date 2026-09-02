import {
  ArrowDownToLine,
  Bookmark,
  CalendarDays,
  Database,
  Film,
  Heart,
  Search,
  ShieldCheck,
  Star,
} from "lucide-react";

import { Button } from "@/components/ui/button";

const features = [
  {
    icon: Search,
    title: "Live TMDB dashboard",
    copy: "Open directly into current TMDB charts, ranked films and a spotlight that updates from live movie data.",
  },
  {
    icon: Bookmark,
    title: "A library that feels personal",
    copy: "Keep a watchlist, mark films watched, save favorites and carry every choice into a simple, durable collection.",
  },
  {
    icon: Star,
    title: "Ratings and private reviews",
    copy: "Rate each film from one to five stars and keep personal notes that stay on your device.",
  },
  {
    icon: CalendarDays,
    title: "A viewing diary",
    copy: "Turn watched films into a dated timeline and see your viewing habits build over time.",
  },
];

const roadmap = [
  "New stats.fm-inspired dashboard with ranked movie charts",
  "Reliable Home loading with independent TMDB requests and retry",
  "Aligned cards, navigation, actions and film detail layouts",
  "TMDB-only discovery with no Wikipedia or Wikidata fallback",
];

export default function Home() {
  return (
    <main className="site-shell min-h-screen overflow-hidden bg-background text-foreground">
      <header className="site-header">
        <a className="brand" href="#top" aria-label="ReelTrack home">
          <span className="brand-mark" aria-hidden="true">R</span>
          <span>ReelTrack</span>
        </a>
        <nav aria-label="Main navigation">
          <a href="#features">Features</a>
          <a href="#data">Data & privacy</a>
          <a href="#roadmap">Roadmap</a>
        </nav>
        <Button asChild className="nav-download">
          <a href="./downloads/ReelTrack-v4.0.apk" download>
            Download APK <ArrowDownToLine aria-hidden="true" />
          </a>
        </Button>
      </header>

      <section id="top" className="hero-section" aria-labelledby="hero-title">
        <div className="hero-glow" aria-hidden="true" />
        <div className="hero-copy">
          <div className="status-pill">
            <span aria-hidden="true" /> Android 6+ · Private beta
          </div>
          <p className="eyebrow">Your films. Your story.</p>
          <h1 id="hero-title">Remember more than what you watched.</h1>
          <p className="hero-lead">
            ReelTrack is a focused Android film diary for discovering movies,
            building a watchlist, rating what you see and keeping private reviews.
          </p>
          <div className="hero-actions">
            <Button asChild size="lg" className="primary-cta">
              <a href="./downloads/ReelTrack-v4.0.apk" download>
                <ArrowDownToLine aria-hidden="true" /> Download v4.0
              </a>
            </Button>
            <Button asChild size="lg" variant="outline" className="secondary-cta">
              <a href="#features">Explore the project</a>
            </Button>
          </div>
          <p className="download-note">Signed APK · No ads · Local-first tracking</p>
        </div>
        <figure className="hero-visual">
          <img
            src="./assets/reeltrack-hero.png"
            alt="A cinematic phone mockup showing a dark film-tracking interface"
            width="1536"
            height="1024"
          />
          <figcaption>Original ReelTrack product concept</figcaption>
        </figure>
      </section>

      <section className="signal-strip" aria-label="Project highlights">
        <div><strong>5</strong><span>tracking views</span></div>
        <div><strong>∞</strong><span>personal reviews</span></div>
        <div><strong>0</strong><span>monthly fees</span></div>
        <div><strong>100%</strong><span>local tracking data</span></div>
      </section>

      <section id="features" className="content-section">
        <div className="section-heading">
          <p className="eyebrow">Built for the ritual</p>
          <h2>A calmer way to keep up with cinema.</h2>
          <p>
            ReelTrack keeps the essential film-tracking tools close without
            turning your diary into an attention feed.
          </p>
        </div>
        <div className="feature-grid">
          {features.map(({ icon: Icon, title, copy }, index) => (
            <article className="feature-card" key={title}>
              <div className="feature-topline">
                <span className="feature-icon"><Icon aria-hidden="true" /></span>
                <span>0{index + 1}</span>
              </div>
              <h3>{title}</h3>
              <p>{copy}</p>
            </article>
          ))}
        </div>
      </section>

      <section id="data" className="data-section">
        <div className="data-copy">
          <p className="eyebrow">Open data, clear boundaries</p>
          <h2>Movie information in. Private opinions stay put.</h2>
          <p>
            ReelTrack v4.0 searches and explores TMDB movie data, including
            poster artwork. Watchlists, ratings, diary entries and personal
            reviews are stored locally on the Android device.
          </p>
          <div className="data-points">
            <div><ShieldCheck aria-hidden="true" /><span><strong>Private by default</strong> No public profile or social feed.</span></div>
            <div><Database aria-hidden="true" /><span><strong>Transparent sources</strong> TMDB is credited clearly inside the app.</span></div>
            <div><Heart aria-hidden="true" /><span><strong>Non-commercial beta</strong> No ads, subscriptions or sale of user data.</span></div>
          </div>
        </div>
        <aside className="use-card" aria-label="TMDB usage">
          <div className="use-card-icon"><Film aria-hidden="true" /></div>
          <p className="use-label">Live movie data</p>
          <h3>TMDB integration</h3>
          <p>
            ReelTrack uses TMDB for film search, release information, genres,
            posters, descriptions, cast, trailers, recommendations and IMDb identifiers.
          </p>
          <dl>
            <div><dt>Platform</dt><dd>Android</dd></div>
            <div><dt>Use</dt><dd>Personal, non-commercial</dd></div>
            <div><dt>Status</dt><dd>Redesigned in v4.0</dd></div>
          </dl>
        </aside>
      </section>

      <section id="roadmap" className="roadmap-section">
        <div className="roadmap-title">
          <p className="eyebrow">The next ReelTrack</p>
          <h2>New in ReelTrack v4.0</h2>
        </div>
        <ol className="roadmap-list">
          {roadmap.map((item, index) => (
            <li key={item}>
              <span>{String(index + 1).padStart(2, "0")}</span>
              <p>{item}</p>
            </li>
          ))}
        </ol>
      </section>

      <section className="closing-section">
        <div>
          <p className="eyebrow">Start your diary</p>
          <h2>What will you watch next?</h2>
        </div>
        <Button asChild size="lg" className="primary-cta">
          <a href="./downloads/ReelTrack-v4.0.apk" download>
            Download ReelTrack <ArrowDownToLine aria-hidden="true" />
          </a>
        </Button>
      </section>

      <footer>
        <a className="brand" href="#top"><span className="brand-mark" aria-hidden="true">R</span><span>ReelTrack</span></a>
        <p>Independent Android film tracker · 2026</p>
        <p>This product uses the TMDB API but is not endorsed or certified by TMDB.</p>
      </footer>
    </main>
  );
}
