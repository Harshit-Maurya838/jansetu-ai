# data/

**No real beneficiary data is ever committed to this repository** – not even anonymised samples.
`.gitignore` blocks `*.csv`, `*.xlsx`, `*.parquet` and images in this folder.

## Data we still need (TODO(team))

| Dataset | For | Possible source | Status |
|---|---|---|---|
| WHO Child Growth Standards LMS tables | z-scores (Module 03) | WHO website (public) | TODO(team) |
| Labelled child growth histories (age, sex, weight, height, MUAC, outcome) | malnutrition risk model | Partner Anganwadi records, with consent + ethics approval; public NFHS-5 microdata for pre-training (requires DHS registration) | TODO(team) |
| Labelled meal photos (acceptable / poor) | meal image model | Photos taken during the pilot, labelled by supervisors | TODO(team) |
| Elderly vitals + outcomes | elderly risk model | Partner NGO records, with consent | TODO(team) |

Rules: de-identify before analysis (drop names, phones, addresses; replace ids with random keys), keep
raw data on encrypted university storage, record the consent basis for each dataset. See
[docs/privacy-and-security.md](../../docs/privacy-and-security.md).
