# RBAC Matrix

JanSetu AI uses **role-based access control** plus **data scoping**. The role decides *which* actions are allowed,
and the scope decides *on whose records*. Both are enforced on the server. The mobile app only hides screens
([`mobile/src/constants/roles.ts`](../mobile/src/constants/roles.ts)), so it is never the real gate.

## Roles

| Code | Role | Spring role (`hasRole`) | Authority | Typical user | Default data scope |
|---|---|---|---|---|---|
| **SA** | Super Admin | `SUPER_ADMIN` | `ROLE_SUPER_ADMIN` | Project technical admin | Everything (audited) |
| **AWW** | Anganwadi Worker | `ANGANWADI_WORKER` | `ROLE_ANGANWADI_WORKER` | Worker running one Anganwadi centre (AWC) | Own centre (`users.centre_id`) |
| **NGW** | NGO Worker | `NGO_WORKER` | `ROLE_NGO_WORKER` | Field worker / caregiver of a partner NGO | Beneficiaries assigned to them |
| **HP** | Healthcare Professional | `HEALTH_PROFESSIONAL` | `ROLE_HEALTH_PROFESSIONAL` | ANM, PHC/CHC doctor, nutrition counsellor | Referred patients + just-in-time QR access |
| **PG** | Parent / Guardian | `PARENT_GUARDIAN` | `ROLE_PARENT_GUARDIAN` | Mother/father/guardian of an enrolled child | Own child(ren) only (`guardians.user_id`) |
| **NGA** | NGO Administrator | `NGO_ADMIN` | `ROLE_NGO_ADMIN` | Programme coordinator of a partner NGO | Own NGO (`users.ngo_id`) |
| **GS** | Government Supervisor | `GOVT_SUPERVISOR` | `ROLE_GOVT_SUPERVISOR` | ICDS Lady Supervisor / CDPO | Centres in their jurisdiction |

> **TODO(team):** `users` holds only one `centre_id`, but a supervisor covers many centres. Add a
> `supervisor_jurisdictions (user_id, centre_id)` table in a V3 migration (see [data-model.md](data-model.md#open-questions)).
>
> **TODO(team):** decide how an HP gets access. Recommended: (a) children/beneficiaries with an open referral to the HP's
> facility, and (b) a time-limited (24 h), audited grant created when the HP scans a QR card ([Module 06](modules/06-qr-health-card.md)).
>
> **TODO(team):** the role list has **no role for the elderly beneficiary herself**, yet she must be able to press SOS.
> Recommended: a **beneficiary SOS device** registered by the NGO worker, holding a device-scoped token with the single
> authority `SOS_TRIGGER` (not a user role, no access to any record). Feature-phone users trigger SOS by SMS. See [Module 10](modules/10-emergency-sos.md).

## Legend

`V` view · `C` create · `E` edit · `D` delete (always a *soft* delete, see [data-model.md](data-model.md)) · `A` approve / verify / close · `—` no access.
A qualifier in brackets limits the scope, e.g. `V(own child)`.

## Role × module matrix

| # | Module | SA | AWW | NGW | HP | PG | NGA | GS |
|---|---|---|---|---|---|---|---|---|
| 01 | [Authentication & User Mgmt](modules/01-authentication.md) | VCEDA | V E(self) | V E(self) | V E(self) | V E(self) | VCE A (own NGO staff) | V A (centre staff) |
| 02 | [Child Management](modules/02-child-management.md) | VCEDA | VCE | — | V | V(own child) | — | V A |
| 03 | [Growth & Nutrition](modules/03-growth-nutrition.md) | VCEDA | VCE | — | VCE | V(own child) | — | V A |
| 04 | [Vaccination & Healthcare](modules/04-vaccination-healthcare.md) | VCEDA | VCE | — | VCE A | V(own child) | — | V |
| 05 | [Attendance](modules/05-attendance.md) | VCEDA | VCE | — | — | V(own child) | — | V A |
| 06 | [QR Health Card](modules/06-qr-health-card.md) | VCEDA | V C | V C | V(scan) | V(own child) | V C D(revoke) | V(scan) |
| 07 | [Midday Meal Quality](modules/07-midday-meal-quality.md) | VCEDA | VCE | — | — | — | — | VCE A |
| 08 | [Elderly Registration](modules/08-elderly-registration.md) | VCEDA | — | VCE | V | — | VCED A | — |
| 09 | [Elderly Healthcare](modules/09-elderly-healthcare.md) | VCEDA | — | VCE | VCE | — | V | — |
| 10 | [Emergency SOS](modules/10-emergency-sos.md) | V A | — | V C E | V(if notified) | — | V E A | — |
| 11 | [Welfare & NGO Assistance](modules/11-welfare-ngo-assistance.md) | VCEDA | — | VCE | — | — | VCED A | V(aggregate) |
| 12 | [NGO Worker & Caregiver Mgmt](modules/12-ngo-worker-caregiver.md) | VCEDA | — | V E(own visits) C(follow-up) | — | — | VCED A | — |
| 13 | [AI Intelligence & Analytics](modules/13-ai-analytics.md) | VCEDA | V | V | V A | — | V A | V A |
| 14 | [Notifications](modules/14-notifications.md) | VCEDA | V E(own) | V E(own) | V E(own) | V E(own) | V E(own) C(broadcast NGO) | V E(own) C(broadcast centres) |
| 15 | [Admin Dashboard & Reports](modules/15-admin-dashboard-reports.md) | VCEDA | — | — | — | — | V(NGO) export | V(jurisdiction) export |
| 16 | [Offline Sync](modules/16-offline-sync.md) | V A | V C(own device) | V C(own device) | V C(own device) | — | V A(NGO conflicts) | V A(centre conflicts) |

**Notes**

- **SA and health data.** SA has technical access to everything so they can support the system, but every read of a child or
  elderly health record by SA is audited. TODO(team): consider break-glass only (SA must enter a reason, and the event alerts NGA/GS).
- **AWW `D` is not granted.** A child who leaves is marked `EXITED` / `TRANSFERRED` (an edit). Only SA can soft-delete, e.g. a duplicate after a GS-approved merge.
- **Attendance edits** older than the same day need GS approval ([Module 05](modules/05-attendance.md#business-rules--validation)).
- **AI alerts** can be *viewed* by field workers but only *confirmed/dismissed* (`A`) by HP, GS (child alerts) or NGA (elderly alerts).

## Mapping to Spring Security

### 1. Authentication → authorities

[`JwtAuthFilter`](../backend/src/main/java/in/jansetu/security/JwtAuthFilter.java) validates the access token and creates a
[`JwtPrincipal`](../backend/src/main/java/in/jansetu/security/JwtPrincipal.java) `(userId, role, centreId, ngoId)` with the single
authority `ROLE_<ROLE>`. [`SecurityConfig`](../backend/src/main/java/in/jansetu/security/SecurityConfig.java) only decides public vs. authenticated.
Everything else is method security (`@EnableMethodSecurity`).

### 2. Role check on every endpoint: `@PreAuthorize`

```java
@PostMapping("/children")
@PreAuthorize("hasAnyRole('ANGANWADI_WORKER','SUPER_ADMIN')")
public ApiResponse<ChildDto> register(@Valid @RequestBody RegisterChildRequest req,
                                      @AuthenticationPrincipal JwtPrincipal me) { ... }
```

### 3. Data scope check: `@accessScope` bean

```java
@GetMapping("/children/{childId}")
@PreAuthorize("hasAnyRole('ANGANWADI_WORKER','HEALTH_PROFESSIONAL','PARENT_GUARDIAN','GOVT_SUPERVISOR','SUPER_ADMIN')"
            + " and @accessScope.canReadChild(authentication, #childId)")
public ApiResponse<ChildDto> get(@PathVariable UUID childId) { ... }
```

`AccessScope` (TODO(team): implement in `in.jansetu.security`) answers per role:

| Role | `canReadChild` true when… | `canReadBeneficiary` true when… |
|---|---|---|
| SA | always (audited) | always (audited) |
| AWW | `child.centreId == me.centreId` | never |
| GS | child's centre ∈ my jurisdiction | never |
| HP | open referral to my facility, or an active QR access grant | assigned/referred, or an active QR access grant |
| PG | `child.guardian.userId == me.userId` | never |
| NGW | never | `beneficiary.assignedWorkerId == me.userId` |
| NGA | never | `beneficiary.ngoId == me.ngoId` |

**List endpoints** don't use `@accessScope`. The service adds the scope to the query instead (e.g. `WHERE centre_id = :myCentre`),
so a list can never leak another centre's rows. A scope failure returns **404, not 403**, so a caller cannot probe which IDs exist.

### 4. Non-user authorities

| Authority | Holder | Allows |
|---|---|---|
| `SOS_TRIGGER` | Beneficiary SOS device token | `POST /api/v1/sos` for its own beneficiary only |
| (API key) | Spring Boot → ai-service | ai-service `/v1/**` (shared secret, internal network only) |

## Data scoping

| Scope column | Set from | Used by |
|---|---|---|
| `users.centre_id` | Assigned by SA/GS at user creation | AWW lists, attendance, meals |
| `users.ngo_id` | Assigned by SA/NGA | NGW / NGA lists |
| `elderly_beneficiaries.assigned_worker_id` + `caregiver_assignments` | NGA ([Module 12](modules/12-ngo-worker-caregiver.md)) | NGW access |
| `guardians.user_id` | Linked when the guardian registers with the same mobile number and the AWW confirms | PG access |
| `supervisor_jurisdictions` | TODO(team) | GS access |

Every change to a role, scope or assignment is written to `audit_logs` ([privacy-and-security.md](privacy-and-security.md#audit-logging)).
