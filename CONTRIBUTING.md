# Contributing to JanSetu AI

Thanks for helping. This is a student team project, so these rules keep five people from stepping on each other.

## Workflow

1. **Pick or open an issue** using the templates (feature / bug). Every PR links an issue.
2. **Branch from `main`:** `feature/<module-no>-<short-name>` (e.g. `feature/03-growth-chart`), `fix/<issue-no>-<short-name>`, `docs/<topic>`.
3. **Commit messages:** [Conventional Commits](https://www.conventionalcommits.org/), with the module as scope:
   `feat(growth): add WHZ calculation`, `fix(sos): retry SMS fallback`, `docs(rbac): add NGO admin rows`.
4. **Open a PR** using the template. CI must be green. At least **1 teammate review** is required; changes to
   `security/`, `sos/`, `sync/`, DB migrations or `docs/privacy-and-security.md` need **2**.
5. **Squash-merge.** Delete the branch after merge.

TODO(team): enable branch protection on `main` (require PR + CI + reviews).

## Where things go

| Change | Update also |
|---|---|
| New/changed endpoint | the module doc's *API endpoints* table, `@PreAuthorize`, [rbac-matrix.md](docs/rbac-matrix.md) |
| New/changed table | a **new** Flyway migration `V<n>__<desc>.sql` (never edit an applied one), the entity, [data-model.md](docs/data-model.md), the module doc |
| Data used offline | `mobile/src/db/schema.ts` (append a migration), [16-offline-sync.md](docs/modules/16-offline-sync.md) |
| New UI text | **both** `mobile/src/i18n/en.json` and `hi.json` |
| ML model/feature | [ml-overview.md](docs/ml-overview.md) + a model card in `ai-service/notebooks/` |

## Code style

- **Java:** Spring Boot conventions, constructor injection (`@RequiredArgsConstructor`), DTOs (records) at the API boundary. Never return entities.
- **Python:** `ruff check` and `ruff format` (config in `ai-service/pyproject.toml`).
- **TypeScript:** `npm run lint` and `npm run typecheck` in `mobile/`. No `any` without a comment.
- Mark unfinished work as `TODO(team): <what>`, not just `TODO`.

## Run checks locally (same as CI)

```bash
cd backend && mvn -B verify
cd ai-service && pip install -r requirements-dev.txt && ruff check . && ruff format --check . && pytest
cd mobile && npm ci && npm run lint && npm run typecheck && npm test
```

## Data and privacy rules (non-negotiable)

- **No real beneficiary data in the repo, issues, PRs or screenshots.** Use synthetic data only.
- Never commit `.env`, keystores, `google-services.json` or service-account keys. If you do by accident, tell the team at once. The key must be rotated, and deleting the commit is not enough.
- Never log health values, OTPs, tokens or phone numbers.
- Read [docs/privacy-and-security.md](docs/privacy-and-security.md) before touching auth, QR, SOS or exports.
