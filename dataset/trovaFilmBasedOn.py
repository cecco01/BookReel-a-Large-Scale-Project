import requests
import json
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from threading import Lock
import os

# File di input e output
input_file = "nomi_film.txt"
output_file = "film_basati_su.jsonl"

# Endpoint Wikidata SPARQL
endpoint = "https://query.wikidata.org/sparql"

# Numero massimo di richieste parallele
MAX_WORKERS = 10

# Timeout per richiesta (3 minuti)
TIMEOUT = 180

# Numero massimo di retry in caso di errore
MAX_RETRIES = 3

# Leggi i titoli da file
with open(input_file, "r", encoding="utf-8") as f:
    titoli = [line.strip() for line in f if line.strip()]

# Set per evitare duplicati globali
visti = set()
file_lock = Lock()

# 🔹 Carica i record già presenti nel file di output (se esiste)
if os.path.exists(output_file):
    with open(output_file, "r", encoding="utf-8") as f:
        for line in f:
            try:
                record = json.loads(line)
                chiave = (record.get("Film"), record.get("BasatoSu"))
                if all(chiave):
                    visti.add(chiave)
            except json.JSONDecodeError:
                continue

def fetch_film_data(titolo):
    """Esegue la query SPARQL per un titolo e restituisce le coppie film->basatoSu"""
    risultati = []
    titolo_regex = titolo.replace("\\", "\\\\").replace('"', '\\"')

    query = f"""
    SELECT DISTINCT ?film ?filmLabel ?basedOn ?basedOnLabel WHERE {{
      ?film rdfs:label ?filmLabel .
      FILTER(REGEX(?filmLabel, "^{titolo_regex}$", "i"))
      ?film wdt:P31 wd:Q11424 .
      ?film wdt:P144 ?basedOn .
      SERVICE wikibase:label {{ bd:serviceParam wikibase:language "it,en" }}
    }}
    """

    headers = {"User-Agent": "FilmBasedOnBot/1.0 (example@example.com)"}

    for attempt in range(1, MAX_RETRIES + 1):
        try:
            response = requests.get(
                endpoint,
                params={"query": query, "format": "json"},
                headers=headers,
                timeout=TIMEOUT
            )
            if response.status_code == 200:
                data = response.json()
                for item in data["results"]["bindings"]:
                    film = item["filmLabel"]["value"]
                    basatoSu = item["basedOnLabel"]["value"]
                    risultati.append((film, basatoSu))
                break
            else:
                print(f"Errore HTTP {response.status_code} per titolo: {titolo}")
        except requests.exceptions.ReadTimeout:
            print(f"Timeout ({attempt}/{MAX_RETRIES}) per titolo: {titolo}")
            time.sleep(1)
        except Exception as e:
            print(f"Errore per titolo '{titolo}': {e}")
            break

    # Piccola pausa per non sovraccaricare Wikidata
    time.sleep(0.2)
    return risultati

# 🔹 Apri il file in APPEND
with open(output_file, "a", encoding="utf-8", buffering=1) as out_f:
    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        future_to_title = {
            executor.submit(fetch_film_data, titolo): titolo
            for titolo in titoli
        }

        for future in as_completed(future_to_title):
            titolo = future_to_title[future]
            try:
                risultati = future.result()
                for film, basatoSu in risultati:
                    chiave = (film, basatoSu)
                    if chiave not in visti:
                        visti.add(chiave)
                        record = {"Film": film, "BasatoSu": basatoSu}
                        with file_lock:
                            out_f.write(json.dumps(record, ensure_ascii=False) + "\n")
                            out_f.flush()
                        print(record)
            except Exception as e:
                print(f"Errore elaborando '{titolo}': {e}")

print(f"\nAppend completato correttamente su: {output_file}")
