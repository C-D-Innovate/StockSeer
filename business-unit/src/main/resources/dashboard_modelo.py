import streamlit as st
import pandas as pd
import numpy as np
import joblib
import plotly.graph_objects as go
from sklearn.linear_model import LinearRegression
import sys
import os

# === Parámetros de entrada ===
if len(sys.argv) < 3:
    st.error("Debes proporcionar la ruta al CSV y al modelo como argumentos al lanzar el dashboard.")
    st.stop()

csv_path = sys.argv[1]
model_path = sys.argv[2]

# === Validación de rutas ===
if not os.path.exists(csv_path):
    st.error(f"El archivo CSV no existe en la ruta proporcionada: {csv_path}")
    st.stop()

if not os.path.exists(model_path):
    st.error(f"El archivo del modelo no existe en la ruta proporcionada: {model_path}")
    st.stop()

# === Cargar datos y modelo ===
try:
    df = pd.read_csv(csv_path)
except Exception as e:
    st.error(f"Error al leer el archivo CSV: {e}")
    st.stop()

try:
    model = joblib.load(model_path)
except Exception as e:
    st.error(f"Error al cargar el modelo desde '{model_path}': {e}")
    st.stop()

# === Ingeniería de características ===
df['delta_open'] = df['open_price'].diff()
df['delta_close'] = df['close_price'].diff()
df['delta_sent'] = df['avg_sent'].diff()
df['range'] = df['close_price'] - df['open_price']
df['volatility'] = df['range'].rolling(window=3).std()
df['momentum'] = df['open_price'].pct_change(periods=2)
df['y'] = df['open_price'].shift(-1)
features = ['open_price', 'close_price', 'avg_sent', 'delta_open', 'delta_close',
            'delta_sent', 'range', 'volatility', 'momentum']
X_today = df.iloc[[-1]][features]
df = df.dropna()

# === Preparación de sets ===
X = df[features]
y = df['y']
split_idx = int(len(X) * 0.8)
X_train, X_test = X.iloc[:split_idx], X.iloc[split_idx:]
y_train, y_test = y.iloc[:split_idx], y.iloc[split_idx:]

# === Predicciones y métricas ===
y_pred = model.predict(X_test)
y_pred_tomorrow = model.predict(X_today)
rmse = np.sqrt(np.mean((y_test - y_pred) ** 2))
mae = np.mean(np.abs(y_test - y_pred))
r2 = 1 - (np.sum((y_test - y_pred) ** 2) / np.sum((y_test - np.mean(y_test)) ** 2))

# === Interpretaciones ===
def interpretar_rmse(rmse):
    if rmse < 1:
        return f"El RMSE es muy bajo ({rmse:.2f}), lo que indica una excelente precisión del modelo."
    elif rmse < 3:
        return f"Un RMSE de {rmse:.2f} sugiere que el modelo tiene un buen rendimiento, con errores moderados."
    else:
        return f"Un RMSE de {rmse:.2f} indica errores considerables en las predicciones."

def interpretar_mae(mae):
    if mae < 1:
        return f"El MAE es muy bajo ({mae:.2f}), lo que significa que los errores medios absolutos son pequeños."
    elif mae < 2.5:
        return f"Un MAE de {mae:.2f} es aceptable y refleja precisión razonable en las predicciones."
    else:
        return f"Un MAE de {mae:.2f} indica desviaciones medias notables en las predicciones."

def interpretar_r2(r2):
    if r2 > 0.9:
        return f"El modelo tiene un R² de {r2:.3f}, lo que significa que explica casi toda la varianza del target."
    elif r2 > 0.75:
        return f"Un R² de {r2:.3f} indica que el modelo captura buena parte de la variabilidad, aunque no toda."
    elif r2 > 0.5:
        return f"Con un R² de {r2:.3f}, el modelo explica algo más de la mitad de la varianza; puede mejorarse."
    else:
        return f"Un R² de {r2:.3f} es bajo; el modelo no representa bien la variabilidad de los datos."

def interpretar_prediccion(valor):
    return f"La predicción estimada para la próxima apertura es de {valor:.2f}. Este valor puede servir de guía para decisiones de entrada al mercado, pero hay que tener en cuenta los resultados de los indicadores."

interpretaciones = {
    'RMSE': interpretar_rmse(rmse),
    'MAE': interpretar_mae(mae),
    'R2': interpretar_r2(r2),
    'Predicción': interpretar_prediccion(y_pred_tomorrow[0])
}

# === Interfaz Streamlit ===
st.title("📊 Dashboard de Predicción AMZ")

st.markdown("""
    <style>
        h1 { text-align: center; }
        .metric-container {
            display: flex;
            justify-content: center;
            gap: 4rem;
            flex-wrap: wrap;
            margin-top: 2rem;
            margin-bottom: 2rem;
        }
        .metric-container > div { text-align: center !important; }
        .appview-container .main .block-container {
            padding-left: 1rem !important;
            padding-right: 1rem !important;
            max-width: 100% !important;
        }
    </style>
""", unsafe_allow_html=True)

st.markdown("### 🔍 Indicadores", unsafe_allow_html=True)
st.markdown('<div class="metric-container">', unsafe_allow_html=True)
col1, col2, col3, col4 = st.columns(4)
with col1:
    st.metric("RMSE", f"{rmse:.2f}")
with col2:
    st.metric("MAE", f"{mae:.2f}")
with col3:
    st.metric("R² Score", f"{r2:.2f}")
with col4:
    st.metric("Próxima apertura", f"{y_pred_tomorrow[0]:.2f}")
st.markdown('</div>', unsafe_allow_html=True)

st.markdown("### 📘 Interpretaciones")
for k, v in interpretaciones.items():
    st.markdown(f"**{k}:** {v}")

# === Gráfico pred vs real ===
fig_pred = go.Figure()
fig_pred.add_trace(go.Scatter(y=y_test.values, name="Real", mode="lines"))
fig_pred.add_trace(go.Scatter(y=y_pred, name="Predicción", mode="lines"))
fig_pred.update_layout(
    title="📈 Predicción vs Valores Reales",
    title_font=dict(size=24),
    xaxis_title="Índice",
    yaxis_title="Apertura"
)
st.plotly_chart(fig_pred)

# === Scatter avg_sent[t] vs open_price[t+1] ===
df = df.sort_values("day").reset_index(drop=True)
df['next_open_price'] = df['open_price'].shift(-1)
df = df.dropna(subset=['next_open_price'])

X_sent_today = df['avg_sent'].values.reshape(-1, 1)
y_open_tomorrow = df['next_open_price'].values

reg = LinearRegression().fit(X_sent_today, y_open_tomorrow)
y_trend = reg.predict(X_sent_today)
correlacion = np.corrcoef(df['avg_sent'], df['next_open_price'])[0, 1]

if correlacion > 0.5:
    interpretacion_corr = f"🔼 Existe una correlación positiva fuerte entre el sentimiento medio y el precio de apertura (r = {correlacion:.2f})."
elif correlacion > 0.2:
    interpretacion_corr = f"↗️ Existe una correlación positiva débil (r = {correlacion:.2f}). El sentimiento podría tener cierto efecto sobre el precio."
elif correlacion < -0.5:
    interpretacion_corr = f"🔽 Existe una correlación negativa fuerte entre el sentimiento medio y el precio de apertura (r = {correlacion:.2f})."
elif correlacion < -0.2:
    interpretacion_corr = f"↘️ Existe una correlación negativa débil (r = {correlacion:.2f}). Podría haber una relación inversa leve."
else:
    interpretacion_corr = f"No se observa una correlación significativa entre el sentimiento medio y la apertura (r = {correlacion:.2f})."

fig_sent = go.Figure()
fig_sent.add_trace(go.Scatter(
    x=df['avg_sent'],
    y=df['open_price'],
    mode='markers',
    marker=dict(size=8, color=df['open_price'], colorscale='Viridis', showscale=True,
                colorbar=dict(title="Apertura")),
    hovertemplate='Sentimiento: %{x:.2f}<br>Apertura: %{y:.2f}<extra></extra>'
))
fig_sent.add_trace(go.Scatter(
    x=df['avg_sent'],
    y=y_trend,
    mode='lines',
    name='Tendencia lineal',
    line=dict(color='orange', dash='dash')
))
fig_sent.update_layout(
    title="📊 Impacto del Sentimiento Medio en la Apertura",
    title_font=dict(size=24),
    xaxis_title="Sentimiento Medio",
    yaxis_title="Precio de Apertura",
    legend=dict(x=0.01, y=0.99),
    height=500
)
st.plotly_chart(fig_sent)

st.markdown("### 🧠 Interpretación de la correlación entre sentimiento y apertura")
st.markdown(interpretacion_corr)