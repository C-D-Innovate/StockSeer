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

## 🧱 Módulos del proyecto

A continuación se detallan los distintos módulos que componen el sistema, junto con su diagrama de clases correspondiente.
Haz clic en el nombre de cada imagen para visualizarla en una nueva pestaña.

---

### 📦 `time-series-intraday-feeder`

<details>
  <summary>📄 Ver diagrama de clases</summary>

🔗 [Abrir imagen en el navegador](./diagrams/time-series-intraday-feeder-class-diagram.png)

</details>

Este módulo se encarga de:

* Obtener datos bursátiles intradía de la API de **AlphaVantage**, centrados en el símbolo `AMZN`.
* Filtrar los eventos correspondientes al **inicio y cierre exacto del mercado estadounidense** (09:30 y 16:00 en Nueva York).
* Publicar los eventos en un **broker ActiveMQ** o almacenarlos en **SQLite**, dependiendo de la configuración proporcionada en `args.txt`.

### 📁 Flujo simplificado

1. `IntradayFetcher` espera al cierre del mercado.
2. Llama a `AlphaVantageIntradayFetcher.fetch()`.
3. Filtra apertura/cierre exactos mediante `MarketHoursFilter`.
4. Guarda en `SQLite` o publica en `ActiveMQ`.

---

### 🗞️ `news-api-feeder`

<details>
  <summary>📄 Ver diagrama de clases</summary>

🔗 [Abrir imagen en el navegador](./diagrams/news-api-feeder-class-diagram.png)

</details>

Este módulo se encarga de:

* Recuperar **noticias económicas** mediante la API de [NewsAPI.org](https://newsapi.org/), filtradas por tema y fecha.
* Crear eventos del tipo `ArticleEvent` que contienen título, contenido, fecha de publicación, etc.
* Enriquecer el contenido mediante técnicas de scraping si el texto original está truncado, usando librerías como `newspaper` o `BeautifulSoup`.
* **Publicar** los artículos procesados en un broker (`ActiveMQ`) o almacenarlos en una base de datos local SQLite.

### 📁 Flujo simplificado

1. `ArticleController` calcula el rango de fechas del día anterior.
2. Solicita los artículos usando `NewsApiFetcher`.
3. Procesa y enriquece cada artículo (`ArticleProcessor`, `ArticleEnricher`).
4. Almacena en SQLite o publica en cola/tópico con ActiveMQ.

---
Perfecto, aquí tienes la descripción del módulo `event-store-builder` para integrarla en tu README dentro del apartado de módulos:

---

### 🗃️ `event-store-builder`

<details>
  <summary>📄 Ver diagrama de clases</summary>

🔗 [Abrir imagen en el navegador](./diagrams/event-store-builder-class-diagram.png)

</details>

Este módulo actúa como **consumidor durable** de eventos publicados en el broker **ActiveMQ**. Su objetivo es escuchar eventos de distintos tópicos, deserializarlos, extraer metainformación clave (`ts`, `ss`, `topic`) y almacenarlos en el sistema de ficheros con una estructura organizada.

### 🧠 Principales características

* Se suscribe de forma **durable** a un tópico mediante `ActiveMQSubscriber`, asegurando la entrega incluso tras reinicios.
* Cada mensaje JSON recibido se procesa mediante el controlador `EventHandler`, que lo transforma en un objeto `Event`.
* Los eventos se guardan en ficheros `.events` con la siguiente estructura:

```
eventstore/{topic}/{ss}/{YYYYMMDD}.events
```

Cada línea del fichero contiene un evento en formato JSON.

* Utiliza el patrón **Hexagonal** con:

  * **Puerto**: `EventStorage`
  * **Adaptador**: `FileSystemStorage`, que gestiona la persistencia física.

### 📁 Flujo simplificado

1. `ActiveMQSubscriber` recibe un mensaje del tópico.
2. `EventHandler` deserializa el mensaje y crea un `Event`.
3. El evento se guarda con `FileSystemStorage`, creando la ruta si no existe.

📦 Este módulo facilita la persistencia segura y estructurada de eventos históricos para futuros análisis o auditoría.

---
