#!/usr/bin/env python3
import sys
import traceback

try:
    from newspaper import Article
    def extract(url):
        a = Article(url)
        a.download()
        a.parse()
        return a.text
except ImportError:
    # Fallback ligero con BeautifulSoup
    import requests
    from bs4 import BeautifulSoup

    def extract(url):
        resp = requests.get(url, timeout=10)
        resp.raise_for_status()
        soup = BeautifulSoup(resp.text, "html.parser")
        # por ejemplo: texto de todos los <p>
        return "\n".join(p.get_text() for p in soup.find_all("p"))

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Uso: extract_full_content.py <URL>", file=sys.stderr)
        sys.exit(1)
    url = sys.argv[1]
    try:
        full = extract(url)
        print(full)
    except Exception:
        traceback.print_exc()
        sys.exit(1)