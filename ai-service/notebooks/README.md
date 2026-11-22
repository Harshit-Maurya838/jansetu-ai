# notebooks/

Exploration and training notebooks. Naming: `NN-topic.ipynb` (e.g. `01-who-lms-validation.ipynb`).

- Clear outputs before committing (outputs can contain data).
- Load data from a path outside the repo (env var `JANSETU_DATA_DIR`).
- Each training notebook ends by writing a model card: data snapshot, features, metrics, known limits.

TODO(team): `01-who-lms-validation` – check `zscore.py` against WHO Anthro outputs for 20+ sample children.
