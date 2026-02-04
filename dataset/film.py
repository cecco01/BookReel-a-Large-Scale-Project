import json
import time
import requests

# ===== CONFIG =====
INPUT_FILE = "movie_ids_10_25_2025.json"
OUT = "tmdb_movies_enriched.jsonl"

MAX_MOVIES = 10000
SLEEP = 0.25
TIMEOUT = 20
RETRIES = 3

TMDB_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJlZDAwNzU3ZGYxYjBlYjU3ZDhlYmRkODVlYzlhNmYxNyIsIm5iZiI6MTc2OTg1NDU2NC4wODcwMDAxLCJzdWIiOiI2OTdkZDY2NDJlNDdmZWY1OTA3MTliNTIiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.Js7wEgf8GnsSCJHPcVc0nalH_xgHECuaJiSRtn2rOOk"

HEADERS = {
    "Authorization": f"Bearer {TMDB_TOKEN}",
    "Accept": "application/json"
}

BASE_URL = "https://api.themoviedb.org/3/movie"


def get_json(url: str, params: dict | None = None) -> dict | None:
    """GET con retry + timeout"""
    for attempt in range(1, RETRIES + 1):
        try:
            r = requests.get(url, headers=HEADERS, params=params, timeout=TIMEOUT)
            r.raise_for_status()
            return r.json()
        except requests.RequestException as e:
            if attempt == RETRIES:
                print(f"[ERRORE] {url} → {e}")
                return None
            time.sleep(0.8 * attempt)
    return None


count = 0

with open(INPUT_FILE, "r", encoding="utf-8") as fin, \
     open(OUT, "a", encoding="utf-8") as fout:

    for line in fin:
        if count >= MAX_MOVIES:
            break

        base = json.loads(line)
        movie_id = base.get("id")
        if not movie_id:
            continue

        url = f"{BASE_URL}/{movie_id}"
        params = {
            "language": "en-US",
            "append_to_response": "genres,production_companies"
        }

        data = get_json(url, params=params)
        time.sleep(SLEEP)

        if not data:
            continue

        # puoi tenere sia dati base che dettagli
        row = {
            "tmdb_id": movie_id,
            "original_title": base.get("original_title"),
            "adult": base.get("adult"),
            "popularity": base.get("popularity"),
            "video": base.get("video"),

            # dettagli
            "title": data.get("title"),
            "overview": data.get("overview"),
            "release_date": data.get("release_date"),
            "runtime": data.get("runtime"),
            "genres": [g["name"] for g in (data.get("genres") or [])],
            "vote_average": data.get("vote_average"),
            "vote_count": data.get("vote_count"),
            "original_language": data.get("original_language"),
            "production_companies": [
                c["name"] for c in (data.get("production_companies") or [])
            ],
        }

        fout.write(json.dumps(row, ensure_ascii=False) + "\n")
        count += 1

