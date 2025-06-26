import sqlite3
import pandas as pd
import numpy as np
import sys
import os
from sklearn.model_selection import train_test_split, TimeSeriesSplit, RandomizedSearchCV
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import mean_squared_error
from sklearn.ensemble import RandomForestRegressor, StackingRegressor
from sklearn.linear_model import ElasticNet
from sklearn.svm import SVR
from sklearn.tree import DecisionTreeRegressor
from sklearn.neighbors import KNeighborsRegressor
from scipy.stats import randint, uniform
import joblib

# === 0. Argumentos de entrada ===
if len(sys.argv) != 4:
    print("Error: Se esperaban 3 argumentos: db_path, csv_path, model_path")
    print(f"Argumentos recibidos: {sys.argv[1:]}")
    sys.exit(1)

db_path     = sys.argv[1]
csv_path    = sys.argv[2]
model_path  = sys.argv[3]

print(f"[ARGS] db_path     = {db_path}")
print(f"[ARGS] csv_path    = {csv_path}")
print(f"[ARGS] model_path  = {model_path}")

# === 1. Conectar y exportar clean_datamart a CSV ===
try:
    conn = sqlite3.connect(db_path)
    df = pd.read_sql_query("SELECT * FROM clean_datamart", conn)
    conn.close()
    df.to_csv(csv_path, index=False, encoding='utf-8-sig')
    print(f"[OK] Tabla clean_datamart exportada a '{csv_path}'.")
except Exception as e:
    print(f"Error al conectar o leer la tabla: {e}")
    sys.exit(1)

# === 2. Carga del CSV para pipeline de entrenamiento ===
df = pd.read_csv(csv_path)

# === 3. Ingeniería de características ===
df['delta_open']    = df['open_price'].diff()
df['delta_close']   = df['close_price'].diff()
df['delta_sent']    = df['avg_sent'].diff()
df['range']         = df['close_price'] - df['open_price']
df['volatility']    = df['range'].rolling(window=3).std()
df['momentum']      = df['open_price'].pct_change(periods=2)
df['y']             = df['open_price'].shift(-1)

features = [
    'open_price', 'close_price', 'avg_sent',
    'delta_open', 'delta_close', 'delta_sent',
    'range', 'volatility', 'momentum'
]

df = df.dropna(subset=features + ['y'])
X = df[features]
y = df['y']
X_today = X.iloc[[-1]]

# === 4. Split y validación cruzada ===
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, shuffle=False
)
tscv = TimeSeriesSplit(n_splits=5)
min_train = len(X_train) // (tscv.get_n_splits() + 1)

# === 5. Modelos base ===
estimators = {
    'rf': (RandomForestRegressor(random_state=42), {
        'n_estimators': randint(100, 300),
        'max_depth': randint(5, 30),
        'min_samples_split': randint(2, 10)
    }),
    'svr': (SVR(), {
        'C': uniform(0.1, 10),
        'gamma': ['scale', 'auto'],
        'kernel': ['rbf','poly']
    }),
    'en': (ElasticNet(random_state=42), {
        'alpha': uniform(0.001,1),
        'l1_ratio': uniform(0,1),
        'max_iter': [1000,5000],
        'tol': [1e-4,1e-3]
    }),
    'dt': (DecisionTreeRegressor(random_state=42), {
        'criterion': ['squared_error','friedman_mse'],
        'max_depth': randint(3,20),
        'min_samples_split': randint(2,10),
        'min_samples_leaf': randint(1,5)
    }),
    'knn': (KNeighborsRegressor(), {
        'n_neighbors': list(range(1, min(min_train,15)+1)),
        'weights': ['uniform','distance'],
        'algorithm': ['auto','ball_tree','kd_tree'],
        'leaf_size': randint(10,50),
        'p': [1,2]
    })
}

best_estimators = {}
for name, (mdl, params) in estimators.items():
    if name in ('svr','en','knn'):
        mdl = Pipeline([('scaler', StandardScaler()), ('est', mdl)])
        params = {f'est__{k}': v for k, v in params.items()}
    search = RandomizedSearchCV(
        mdl, param_distributions=params,
        n_iter=30, cv=tscv,
        scoring='neg_root_mean_squared_error',
        n_jobs=-1, random_state=42, error_score=np.nan
    )
    search.fit(X_train, y_train)
    best_estimators[name] = search.best_estimator_
    print(f"[BEST] {name.upper()} -> {search.best_params_}")

# === 6. Modelo final ===
stack = StackingRegressor(
    estimators=[(k, v) for k, v in best_estimators.items()],
    final_estimator=RandomForestRegressor(n_estimators=100, random_state=42),
    passthrough=True, n_jobs=-1
)
stack.fit(X_train, y_train)
y_pred = stack.predict(X_test)
rmse = np.sqrt(mean_squared_error(y_test, y_pred))
print(f"[RESULT] RMSE en test: {rmse:.4f}")

# === 7. Predicción para mañana ===
pred_mañana = stack.predict(X_today)[0]
print(f"[PREDICTION] Apertura estimada mañana: {pred_mañana:.4f}")

# === 8. Guardar modelo entrenado ===
joblib.dump(stack, model_path)
print(f"[SAVED] Modelo guardado en '{model_path}'")