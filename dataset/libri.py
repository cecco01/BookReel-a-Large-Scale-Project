import time
import json
import requests
from typing import Any, Dict, Optional, List
 
USER_AGENT = "uni-dataset-project/1.0 (contact: you@example.com)"  # metti un contatto reale
HEADERS = {"User-Agent": USER_AGENT}
 
SUBJECTS = [
    "science_fiction", "romance", "history", "mystery",
    "biography", "children", "poetry", "philosophy", "psychology"
]
 
LIMIT = 100
MAX_PER_SUBJECT = 500
 
# Sleep tra chiamate
SLEEP_SUBJECT = 0.25   # per /subjects
SLEEP_WORK = 0.15      # per /works/{id}.json
SLEEP_EDITIONS = 0.15  # per /works/{id}/editions.json
SLEEP_BOOK = 0.15      # per /books/{id}.json
 
OUT = "openlibrary_dataset_enriched.jsonl"
 
# Quante volte riprovare in caso di errori di rete temporanei
RETRIES = 3
TIMEOUT = 30
 
 
def get_json(url: str, params: Optional[dict] = None) -> Optional[dict]:
    """GET con retry + timeout. Ritorna dict oppure None."""
    for attempt in range(1, RETRIES + 1):
        try:
            r = requests.get(url, params=params, headers=HEADERS, timeout=TIMEOUT)
            r.raise_for_status()
            return r.json()
        except requests.RequestException as e:
            if attempt == RETRIES:
                print(f"[ERRORE] GET fallita: {url} params={params} err={e}")
                return None
            time.sleep(0.8 * attempt)
    return None
 
 
def normalize_description(desc: Any) -> Optional[str]:
    """
    In Open Library 'description' può essere:
    - stringa
    - oggetto {"type": "...", "value": "..."}
    - assente
    """
    if desc is None:
        return None
    if isinstance(desc, str):
        return desc.strip() or None
    if isinstance(desc, dict):
        val = desc.get("value")
        if isinstance(val, str):
            return val.strip() or None
    return None
 
 
def extract_languages(book_json: dict) -> List[str]:
    """
    In /books/... le lingue spesso sono tipo:
    "languages": [{"key": "/languages/eng"}]
    """
    langs = []
    for item in (book_json.get("languages") or []):
        if isinstance(item, dict) and "key" in item:
            langs.append(item["key"].split("/")[-1])
    return langs
 
 
def fetch_work_details(work_key: str) -> Dict[str, Any]:
    """
    work_key tipo "/works/OL123W"
    Ritorna: description + subjects (se vuoi) ecc.
    """
    url = f"https://openlibrary.org{work_key}.json"
    data = get_json(url)
    time.sleep(SLEEP_WORK)
 
    if not data:
        return {"description": None, "work_subjects": None}
 
    return {
        "description": normalize_description(data.get("description")),
        # opzionale: prendi anche i subjects completi della work (possono essere tanti)
        "work_subjects": data.get("subjects"),
    }
 
 
def fetch_one_edition_key_for_work(work_key: str) -> Optional[str]:
    """
    Prende 1 edition per la work:
    GET /works/{id}/editions.json?limit=1
    """
    url = f"https://openlibrary.org{work_key}/editions.json"
    data = get_json(url, params={"limit": 1})
    time.sleep(SLEEP_EDITIONS)
 
    if not data:
        return None
 
    entries = data.get("entries") or []
    if not entries:
        return None
 
    ed_key = entries[0].get("key")  # es "/books/OL...M"
    if isinstance(ed_key, str) and ed_key.startswith("/books/"):
        return ed_key
    return None
 
 
def fetch_edition_details(edition_key: str) -> Dict[str, Any]:
    """
    edition_key tipo "/books/OL999M"
    """
    url = f"https://openlibrary.org{edition_key}.json"
    data = get_json(url)
    time.sleep(SLEEP_BOOK)
 
    if not data:
        return {"languages": None, "publishers": None, "number_of_pages": None}
 
    publishers = data.get("publishers")
    if isinstance(publishers, list):
        publishers = [p for p in publishers if isinstance(p, str)]
    else:
        publishers = None
 
    number_of_pages = data.get("number_of_pages")
    if not isinstance(number_of_pages, int):
        number_of_pages = None
 
    languages = extract_languages(data)
 
    return {
        "languages": languages or None,
        "publishers": publishers or None,
        "number_of_pages": number_of_pages,
    }
 
 
seen = set()
count = 0
 
for subject in SUBJECTS:
    downloaded_for_subject = 0
    offset = 0
 
    while downloaded_for_subject < MAX_PER_SUBJECT:
        url = f"https://openlibrary.org/subjects/{subject}.json"
        params = {"limit": LIMIT, "offset": offset}
        data = get_json(url, params=params)
        time.sleep(SLEEP_SUBJECT)
 
        if not data:
            # se fallisce una pagina, passa alla successiva
            offset += LIMIT
            continue
 
        works = data.get("works", [])
        if not works:
            break
 
        with open(OUT, "a", encoding="utf-8") as f:
            for w in works:
                work_key = w.get("key")  # "/works/OL123W"
                if not work_key or work_key in seen:
                    continue
                seen.add(work_key)
 
                # --- dati base (da Subjects API)
                base_row = {
                    "work_key": work_key,
                    "title": w.get("title"),
                    "authors": [a.get("name") for a in (w.get("authors") or []) if a.get("name")],
                    "first_publish_year": w.get("first_publish_year"),
                    "edition_count": w.get("edition_count"),
                    "subject_from_query": subject,
                    "cover_id": w.get("cover_id"),
                }
 
                # --- arricchimento 1: dettagli work (descrizione, subjects completi)
                work_extra = fetch_work_details(work_key)
 
                # --- arricchimento 2: prendi UNA edition e leggi lingua/publisher/pagine
                edition_key = fetch_one_edition_key_for_work(work_key)
                if edition_key:
                    edition_extra = fetch_edition_details(edition_key)
                else:
                    edition_extra = {"languages": None, "publishers": None, "number_of_pages": None}
 
                row = {
                    **base_row,
                    **work_extra,
                    "edition_key_sampled": edition_key,  # utile per debug/trace
                    **edition_extra,
                }
 
                f.write(json.dumps(row, ensure_ascii=False) + "\n")
                count += 1
                downloaded_for_subject += 1
 
                if downloaded_for_subject >= MAX_PER_SUBJECT:
                    break
 
        offset += LIMIT
 
print(f"Fatto. Righe scritte: {count}. File: {OUT}")
