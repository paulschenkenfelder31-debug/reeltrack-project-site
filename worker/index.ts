/** Cloudflare Worker entry point for the vinext-starter template. */
import { handleImageOptimization, DEFAULT_DEVICE_SIZES, DEFAULT_IMAGE_SIZES } from "vinext/server/image-optimization";
import handler from "vinext/server/app-router-entry";

interface Env {
  ASSETS: Fetcher;
  DB: D1Database;
  TMDB_READ_ACCESS_TOKEN?: string;
  IMAGES: {
    input(stream: ReadableStream): {
      transform(options: Record<string, unknown>): {
        output(options: { format: string; quality: number }): Promise<{ response(): Response }>;
      };
    };
  };
}

interface ExecutionContext {
  waitUntil(promise: Promise<unknown>): void;
  passThroughOnException(): void;
}

// Image security config. SVG sources with .svg extension auto-skip the
// optimization endpoint on the client side (served directly, no proxy).
// To route SVGs through the optimizer (with security headers), set
// dangerouslyAllowSVG: true in next.config.js and uncomment below:
// const imageConfig: ImageConfig = { dangerouslyAllowSVG: true };

const worker = {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);

    if (url.pathname.startsWith("/api/tmdb/")) {
      return handleTmdbRequest(request, env, url);
    }

    if (url.pathname === "/_vinext/image") {
      const allowedWidths = [...DEFAULT_DEVICE_SIZES, ...DEFAULT_IMAGE_SIZES];
      return handleImageOptimization(request, {
        fetchAsset: (path) => env.ASSETS.fetch(new Request(new URL(path, request.url))),
        transformImage: async (body, { width, format, quality }) => {
          const result = await env.IMAGES.input(body).transform(width > 0 ? { width } : {}).output({ format, quality });
          return result.response();
        },
      }, allowedWidths);
    }

    return handler.fetch(request, env, ctx);
  },
};

async function handleTmdbRequest(
  request: Request,
  env: Env,
  url: URL,
): Promise<Response> {
  if (request.method !== "GET") {
    return jsonResponse({ error: "Method not allowed" }, 405);
  }
  if (!env.TMDB_READ_ACCESS_TOKEN) {
    return jsonResponse({ error: "Movie service is not configured" }, 503);
  }

  let tmdbPath: string;
  let params: URLSearchParams;
  if (url.pathname === "/api/tmdb/search") {
    const query = (url.searchParams.get("query") ?? "").trim();
    if (query.length < 2 || query.length > 80) {
      return jsonResponse({ error: "Query must be 2 to 80 characters" }, 400);
    }
    tmdbPath = "/search/movie";
    params = new URLSearchParams({
      query,
      include_adult: "false",
      language: "en-US",
      page: "1",
    });
  } else {
    const match = url.pathname.match(/^\/api\/tmdb\/movie\/(\d{1,10})$/);
    if (!match) return jsonResponse({ error: "Not found" }, 404);
    tmdbPath = `/movie/${match[1]}`;
    params = new URLSearchParams({
      append_to_response: "credits,external_ids",
      language: "en-US",
    });
  }

  const response = await fetch(`https://api.themoviedb.org/3${tmdbPath}?${params}`, {
    headers: {
      Accept: "application/json",
      Authorization: `Bearer ${env.TMDB_READ_ACCESS_TOKEN}`,
    },
  });
  const body = await response.text();
  return new Response(body, {
    status: response.status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Cache-Control": response.ok ? "public, max-age=300" : "no-store",
    },
  });
}

function jsonResponse(body: Record<string, string>, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json; charset=utf-8" },
  });
}

export default worker;
