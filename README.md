---

# 📊 StockSeer – Predicción bursátil basada en noticias

## 📌 Descripción del proyecto y propuesta de valor

**StockSeer** es una herramienta diseñada para ayudar a los usuarios a tomar decisiones de inversión informadas mediante el análisis conjunto de datos financieros y noticias del mercado. El sistema recopila:

* 📰 Noticias económicas mediante la API de [NewsAPI.org](https://newsapi.org/).
* 💹 Datos bursátiles (intradiarios e históricos) a través de [Alpha Vantage](https://www.alphavantage.co).

El foco principal es la acción de Amazon (AMZN). A través de técnicas de análisis de sentimiento sobre el contenido informativo, y una estructura de datos organizada en un datamart, se genera una predicción visual en un dashboard interactivo.

🎯 **Propuesta de valor:**
Proporcionar un soporte a la toma de decisiones, integrando un contexto informativo y el comportamiento de mercado en una única herramienta.

---

## 🔌 Elección de APIs

Se ha optado por las siguientes APIs:

* **[NewsAPI.org](https://newsapi.org/):** Proporciona noticias económicas filtradas por palabras clave. En nuestro caso, se han utilizado términos relacionados con *Amazon* para obtener titulares relevantes que se analizan mediante técnicas de NLP.
* **[Alpha Vantage](https://www.alphavantage.co):** Ofrece datos bursátiles fiables, tanto intradía como históricos.

✅ Ambas APIs:

* Disponen de planes gratuitos.
* Cuentan con documentación clara.
* Se integran fácilmente en pipelines automáticos.

🧠 La elección está basada en la **complementariedad entre datos estructurados** (precios de apertura, cierre, volumen) y **datos no estructurados** (titulares, noticias), lo que enriquece los modelos predictivos y mejora la calidad de la decisión.

---

## 🔧 Requisitos

### 📦 Dependencias generales

* Tener instalado y en ejecución el broker de mensajería **ActiveMQ**.
* Tener **JDK 21** correctamente configurado.
* Tener **Python 3.11.9 o superior** en el PATH del sistema.

### 🐍 Librerías necesarias en el entorno Python

Para ejecutar los scripts de análisis y el dashboard, asegúrate de tener instaladas las siguientes dependencias:

```python
import sys
import os
import argparse
import nltk
from nltk.sentiment.vader import SentimentIntensityAnalyzer
import sqlite3
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split, TimeSeriesSplit, RandomizedSearchCV
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import mean_squared_error
from sklearn.ensemble import RandomForestRegressor, StackingRegressor
from sklearn.linear_model import ElasticNet, LinearRegression
from sklearn.svm import SVR
from sklearn.tree import DecisionTreeRegressor
from sklearn.neighbors import KNeighborsRegressor
from scipy.stats import randint, uniform
import joblib
import streamlit as st
import plotly.graph_objects as go
```

---

## 🧩 Estructura del sistema

Este proyecto sigue una combinación de **Clean Architecture** y **Arquitectura Hexagonal (Ports & Adapters)**. Aunque los módulos varían ligeramente según su propósito, mantienen una estructura coherente, desacoplada y extensible.

Esta organización es especialmente evidente en los **módulos tipo *feeder***, donde el diseño modular y los principios SOLID se aplican de forma rigurosa para separar la lógica de negocio de las implementaciones concretas (como fuentes de datos o mecanismos de almacenamiento).

### 🔄 Capas principales:

* **Modelo (dominio):** Contiene las entidades puras del sistema (por ejemplo, `AlphaVantageEvent`), independientes de frameworks o librerías externas.
* **Puertos (interfaces):** Definen las necesidades del sistema (entrada y salida), como `OpeningClosingEventSaver` o `IntradayStockEventFetcher`.
* **Adaptadores (infrastructure.adapters):** Implementan los puertos con tecnologías concretas (APIs, bases de datos, brokers de mensajería). Ejemplos: `SqliteEventSaver`, `ActivemqPublisher`, `AlphaVantageIntradayFetcher`.
* **Controlador:** Orquesta el flujo entre proveedores y almacenamiento, como `IntradayFetcher`, encapsulando el caso de uso principal del feeder.
* **Utils:** Contiene clases reutilizables para tareas transversales (como `DateParser`, `MarketCloseScheduler` o `TimestampParser`).

📌 Esta estructura garantiza que **las dependencias fluyen hacia el dominio**, no al revés. El dominio y los puertos no conocen detalles técnicos concretos, lo que facilita testeo, mantenimiento y evolución tecnológica sin afectar la lógica de negocio.

---

### 🧠 Principios de diseño aplicados

A lo largo del desarrollo, especialmente en los *feeders*, se han seguido los siguientes principios:

* **SRP** (Single Responsibility Principle): cada clase tiene una única responsabilidad clara (por ejemplo, `SqliteManager` solo gestiona la persistencia en SQLite).
* **OCP** (Open/Closed Principle): puedes añadir nuevos almacenamientos o proveedores sin modificar el controlador (`IntradayFetcher`).
* **DRY** (Don’t Repeat Yourself): se ha evitado duplicar lógica, centralizando el parseo, almacenamiento y mapeo en clases dedicadas.
* **YAGNI** (You Aren’t Gonna Need It): no se ha incluido código innecesario ni dependencias superfluas.

---

### 🧪 Ejemplo concreto

```java
OpeningClosingEventSaver storage = new ActivemqPublisher(
    argsValues.get("BROKER_URL"),
    argsValues.get("TOPIC_NAME"),
    DateParser.parse(argsValues.get("TODAY"))
);

OpeningClosingEventSaver storage = new SqliteManager(
    argsValues.get("DB_URL"),
    DateParser.parse(argsValues.get("TODAY"))
);

IntradayStockEventFetcher provider = new AlphaVantageIntradayFetcher(
    argsValues.get("API_KEY")
);

IntradayFetcher fetcher = new IntradayFetcher(
    argsValues.get("SYMBOL"),
    provider,
    storage,
    argsValues.get("DB_URL"),
    TimestampParser.parseMarketClose(argsValues.get("MARKET_CLOSE"))
);
```

👉 Esto demuestra la flexibilidad y extensibilidad del sistema, sin alterar la lógica de negocio al cambiar implementaciones concretas.

---
