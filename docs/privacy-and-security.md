# Privacy & Security

JanSetu AI stores **health data of children aged 0–6 and of elderly women**, plus their names, families, phone numbers,
addresses and (with consent) location. These are vulnerable groups. A leak could expose a family's child malnutrition status,
reveal that an old woman lives alone and where, or show an abuse-related legal-aid request. This document sets the rules
every module must follow.

> This is an engineering design by students and **not legal advice**. TODO(team): have the faculty supervisor and the partner
> organisations' legal/compliance contact review it before any real data is collected.

## Legal and ethical basis

### Digital Personal Data Protection Act, 2023 (DPDP Act) and DPDP Rules, 2025

| DPDP requirement | What it means for JanSetu | Where |
|---|---|---|
| **Notice + consent** (ss. 5–6): free, specific, informed, unambiguous, by clear affirmative action, in English or a language of the Eighth Schedule | Consent screen at registration, read aloud by the worker in Hindi/English, with a printed notice to keep. Consent is recorded with date, method (`SIGNED_FORM`, `OTP`, `THUMB_IMPRESSION`) and the notice version | [Module 02](modules/02-child-management.md), [Module 08](modules/08-elderly-registration.md) |
| **Children's data** (s. 9): *verifiable consent of the parent/lawful guardian*. No tracking or behavioural monitoring of children, and no targeted advertising | Guardian consent is **required before a child record can sync**. The ML models score health risk for care purposes only. No profiling for any other purpose, no ads, no third-party analytics SDKs | [Module 02](modules/02-child-management.md#privacy--security-considerations), [ml-overview.md](ml-overview.md) |
| **Legitimate uses** (s. 7): e.g. State providing benefits/services, **medical emergency involving a threat to life** | SOS location sharing and notifying emergency contacts in a live emergency may rely on this. We still ask for location consent at registration | [Module 10](modules/10-emergency-sos.md) |
| **Purpose limitation & erasure** (s. 8(7)) | Data is used only for child nutrition/health monitoring and elderly care/welfare. It is erased (or anonymised) when the purpose ends. See [Retention](#retention) | [Retention](#retention) |
| **Reasonable security safeguards** (s. 8(5)) | Encryption, access control, audit logs, backups (this document) | [Encryption](#encryption), [Access control](#access-control), [Audit](#audit-logging) |
| **Breach notification** (s. 8(6)) to the Data Protection Board **and each affected person** | Incident response plan, see [Breach response](#breach-response) | [Breach response](#breach-response) |
| **Rights** (ss. 11–14): access, correction, erasure, grievance redressal, nomination | Guardian can view the child's data in the app. Correction/erasure requests go through the AWW/NGO with a tracked request. Grievance contact is shown in the app | [Rights](#data-principal-rights-in-practice) |
| **Persons who cannot consent themselves** (s. 9 includes persons with disability via lawful guardian) | For elderly women with cognitive impairment, consent via a lawful guardian / family member, recorded as such | [Module 08](modules/08-elderly-registration.md) |

**Who is the Data Fiduciary?** The organisation that decides *why* the data is processed: the partner NGO and/or the ICDS
department that runs the Anganwadi centres. The student team builds and operates the system **on their behalf** (Data Processor).
TODO(team): put this in writing (an MoU or data-processing agreement) with each partner before the pilot.

### Research ethics

Using field data to **train ML models** is research on human participants, including minors. TODO(team): get approval from
VIT Bhopal University's Institutional Ethics Committee (or equivalent) before using any real records for training, following the
ICMR *National Ethical Guidelines for Biomedical and Health Research Involving Human Participants* (2017). Phase I uses **synthetic data only**.

## Data minimisation: what we do *not* collect

| Not collected | Why / instead |
|---|---|
| Aadhaar number (child, guardian or beneficiary) | Not needed to provide the service. High harm if leaked. Use our own `child_code` / `beneficiary_code`. If a welfare scheme application needs it, the worker enters it **on the government portal**, not in JanSetu |
| Caste, religion | Not needed for health monitoring |
| Bank account numbers | Financial assistance is recorded as *amount + date + scheme*, not account details |
| Continuous location tracking | Location is captured **only** at the moment of an SOS, and only with consent. No background tracking of workers or beneficiaries |
| Contacts list, call logs, other phone data | Never requested. Android permissions limited to camera, location (SOS), notifications, SMS (SOS fallback only) |
| Photos of children's faces | Optional child photo is **off by default** (TODO(team): decide). Meal photos must be of the food, and the app shows "Do not photograph children" on the camera screen |

Fields are tagged by sensitivity in each module's data model:

| Level | Examples | Rules |
|---|---|---|
| **S3 – Special** | Medical history, z-scores/nutrition status, vitals, medicines, SOS location, legal-aid/counselling records | Role + scope check, every read audited, never in notifications/logs/exports without aggregation |
| **S2 – Personal** | Name, DOB, phone, address, guardian, emergency contacts | Role + scope check, writes audited |
| **S1 – Operational** | Centre name, meal menu, attendance counts | Role check |

## Encryption

| Where | How |
|---|---|
| **In transit** | HTTPS/TLS 1.2+ everywhere (managed TLS on the load balancer). Android `network_security_config` blocks cleartext in release builds. Backend → AI service on the private network **plus** an API key. TODO(team): certificate pinning for the release build |
| **At rest – server DB** | Managed MySQL with storage encryption (cloud default, AES-256) and encrypted automated backups |
| **At rest – sensitive columns** | TODO(team): application-level encryption (AES-GCM, key in Secret Manager) for `medical_history`, `welfare_records.description` of `LEGAL_AID`/`COUNSELLING`, and SOS coordinates older than 30 days |
| **At rest – device** | SQLite encrypted with **SQLCipher** (`op-sqlite` built with `sqlcipher: true`). Random 256-bit key generated on first launch and stored in the **Android Keystore** via `react-native-keychain`. Tokens are also in the Keystore, never in AsyncStorage |
| **At rest – files** | Private object-storage bucket, server-side encryption, access only through the API or short-lived signed URLs |
| **Passwords / OTPs / refresh tokens** | BCrypt for passwords. OTPs and refresh tokens stored only as hashes |
| **Secrets** | `.env` locally (git-ignored), cloud Secret Manager in staging/prod. Never in the repo or the APK |

## Access control

- **Role-scoped access:** full matrix in [rbac-matrix.md](rbac-matrix.md). Enforced on the server with `@PreAuthorize` + `AccessScope` data scoping.
  The app hiding a screen is a convenience only.
- **Out-of-scope = 404**, so IDs cannot be probed.
- **Least privilege by default:** a new user has no access until SA/NGA/GS assigns a role and a centre/NGO.
- **Shared phones:** many workers share a phone with family. The app requires a 4-digit app PIN or the device lock after 5 min in the background,
  hides content in the app switcher (`FLAG_SECURE` on sensitive screens), and logout can wipe local data (TODO(team): decide whether logout always wipes).
- **Lost device:** NGA/GS/SA can revoke all refresh tokens of a user/device ([Module 01](modules/01-authentication.md)). The local DB stays encrypted.
- **Session limits:** access token 15 min, refresh 30 days, rotated on use. Offline use continues with the local PIN, and sync requires a valid refresh.

## QR health card: minimal disclosure

Details in [Module 06](modules/06-qr-health-card.md).

- The QR contains **only a signed token**: `JWS { cid: <card token id>, typ: "CHILD"|"ELDERLY", v: 1 }`. It holds **no name, no health data**, so a photo of the card leaks nothing by itself.
- Scanning returns data **according to the scanner's role**:

| Scanner | Sees |
|---|---|
| Not logged in / any camera app | Nothing useful (opaque token). TODO(team): optionally print a helpline number as text on the card |
| Any logged-in JanSetu user | **Emergency view:** first name, age, blood group, allergies, emergency contact phone |
| AWW (own centre), HP, NGW (assigned) | Plus vaccination/health summary (last measurement status, due vaccines, current medicines) |
| PG | Their own child's card only |

- Every scan is audited (who, when, which card). Lost cards are **revoked and reissued**, and the old token stops working.

## Retention

| Data | Retention | Then |
|---|---|---|
| Child records | Until the child exits the Anganwadi (age 6) + 3 years. TODO(team): confirm with ICDS partner | Anonymise (drop name, guardian, contacts; keep age band, centre, measurements for statistics) |
| Elderly beneficiary records | While active + 3 years after exit or death | Anonymise |
| SOS precise location | 90 days | Keep only the village/block |
| Meal photos | 1 year | Delete. Keep the ratings |
| Audit logs | 3 years | Delete |
| OTP challenges | 24 h | Delete |
| Device-local data | Until synced + 30 days for read caches | Purged by the app |

TODO(team): implement as a scheduled job. Erasure on request follows the same anonymisation path.

## Data principal rights in practice

- **Access:** guardians see their child's profile, growth chart, vaccinations and attendance in the app. Beneficiaries without a phone can ask the NGO worker for a printout.
- **Correction:** guardian/beneficiary asks the AWW/NGW, who edits the record (audited).
- **Erasure / withdrawal of consent:** request is logged, approved by GS/NGA, then the record is anonymised. Consent withdrawal stops future processing but does not undo earlier lawful processing.
- **Grievance:** in-app *Help → Privacy* shows the grievance contact (TODO(team): partner organisation's contact) and the response time (TODO(team)).

## Audit logging

`audit_logs` ([`AuditLog`](../backend/src/main/java/in/jansetu/common/audit/AuditLog.java)) is append-only. The application DB user has `INSERT`/`SELECT` but no `UPDATE`/`DELETE` on it (TODO(team): set grants in prod).

Logged events: login success/failure, role/scope changes, **every read of S3 data by a non-owner role**, every create/edit/delete,
QR scans, SOS raise/acknowledge/resolve, risk-alert review decisions, report exports, sync conflicts and merges, consent changes.

Each entry holds actor, action, entity type + id, device id, time, and a short `details` string. **Health values are never written to audit logs or application logs.**

## Application security checklist

- [ ] Bean Validation on every DTO, with server-side range checks (e.g. weight 0.5–40 kg for 0–6 years)
- [ ] Parameterised queries only (Spring Data JPA). No string-concatenated SQL
- [ ] Uploads: type sniffing (not just extension), size limit, EXIF stripped (removes GPS) before storage
- [ ] OTP: 6 digits, 5-min expiry, 5 attempts, rate-limited per number and IP
- [ ] JWT secret ≥ 256 bits from the secret store. Algorithm fixed to HS256
- [ ] CORS locked to known origins (the mobile app doesn't need CORS)
- [ ] Swagger UI disabled in prod
- [ ] Dependabot / `mvn versions` + `pip-audit` + `npm audit` in CI (TODO(team))
- [ ] Release APK: minified, no debug logs, cleartext traffic blocked
- [ ] Error responses never include stack traces or record contents

## Breach response

1. Contain: revoke tokens/keys, block affected accounts, preserve logs.
2. Assess within 24 h: what data, whose, how many people.
3. Notify the Data Fiduciary (partner) **immediately**. The fiduciary notifies the Data Protection Board and affected people as required by the DPDP Rules.
4. Fix the root cause and write a post-mortem.

TODO(team): name the people responsible for each step during the pilot.

## Synthetic data only in development

Dev and staging use generated names and values. Never copy production data to laptops or staging. Demo videos and screenshots use synthetic data.
