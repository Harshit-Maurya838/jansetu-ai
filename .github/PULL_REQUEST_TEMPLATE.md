## What & why

Closes #

**Module(s):**

## How it was tested

- [ ] Unit tests added/updated
- [ ] Tried on a real low-end Android phone / emulator (if mobile)
- [ ] Tried offline → online sync (if the feature works offline)

## Checklist

- [ ] CI green (backend, ai-service, mobile)
- [ ] Endpoint has `@PreAuthorize` and data scoping; [rbac-matrix.md](../docs/rbac-matrix.md) updated if roles changed
- [ ] New table/column → new Flyway migration (no edits to applied ones) + docs updated
- [ ] UI text in both `en.json` and `hi.json`
- [ ] No secrets, real beneficiary data, or health values in code, logs, tests or screenshots
- [ ] Module doc updated if behaviour changed
- [ ] Needs 2 reviewers (touches security / SOS / sync / migrations / privacy)?
