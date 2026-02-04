import json

input_file = "film.jsonl"  # il tuo file di input
output_file = "nomi_film.txt"  # file di output

with open(input_file, "r", encoding="utf-8") as f_in, open(output_file, "w", encoding="utf-8") as f_out:
    for line in f_in:
        line = line.strip()
        if not line:
            continue  # salta righe vuote
        try:
            film = json.loads(line)
        except json.JSONDecodeError:
            print(f"Riga non valida: {line}")
            continue
        
        title = film.get("title", "")

        if title:
            f_out.write(f"{title}\n")
