#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import argparse
import nltk
from nltk.sentiment.vader import SentimentIntensityAnalyzer

def main():
    parser = argparse.ArgumentParser(
        description="Analiza sentimiento de un archivo de texto usando VADER y permite boostear lo negativo o positivo."
    )
    parser.add_argument('-f', '--file', required=True, help='Ruta al archivo .txt a analizar')
    parser.add_argument('--boost-neg', type=float, default=1.0, help='Factor multiplicador para negativos')
    parser.add_argument('--boost-pos', type=float, default=1.0, help='Factor multiplicador para positivos')
    args = parser.parse_args()

    print("📥 Script iniciado", file=sys.stderr)
    nltk.download('vader_lexicon', quiet=True)
    sia = SentimentIntensityAnalyzer()

    for palabra, puntaje in list(sia.lexicon.items()):
        if puntaje < 0:
            sia.lexicon[palabra] = puntaje * args.boost_neg
        elif puntaje > 0:
            sia.lexicon[palabra] = puntaje * args.boost_pos

    try:
        with open(args.file, encoding='utf-8') as f:
            texto = f.read()
        print(f"📄 Texto leído ({len(texto)} caracteres)", file=sys.stderr)
    except Exception as e:
        print(f"❌ Error al leer {args.file}: {e}", file=sys.stderr)
        sys.exit(1)

    try:
        scores = sia.polarity_scores(texto)
        comp = scores['compound']
        print(f"✅ Scores: {scores}", file=sys.stderr)

        if comp >= 0.05:
            print("POSITIVE")
        elif comp <= -0.05:
            print("NEGATIVE")
        else:
            print("NEUTRAL")
    except Exception as e:
        print(f"❌ Error en análisis de sentimiento: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == '__main__':
    main()
