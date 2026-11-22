/**
 * On-device SQLite schema (SQLCipher-encrypted, see package.json "op-sqlite").
 * Mirrors the server tables a field worker needs offline – see docs/modules/16-offline-sync.md#offline-behaviour.
 * Every synced table has: id (client UUID), updated_at (ISO, device clock), server_version, deleted, dirty.
 * Bump DB_VERSION and append to MIGRATIONS; never edit an existing migration.
 */
export const DB_VERSION = 1;

const synced = `
  updated_at TEXT NOT NULL,
  server_version INTEGER,
  deleted INTEGER NOT NULL DEFAULT 0,
  dirty INTEGER NOT NULL DEFAULT 1`;

export const MIGRATIONS: string[][] = [
  [
    `CREATE TABLE IF NOT EXISTS guardians (
      id TEXT PRIMARY KEY, full_name TEXT NOT NULL, relation TEXT NOT NULL, mobile TEXT,
      address TEXT, consent_given_at TEXT, consent_method TEXT,${synced})`,
    `CREATE TABLE IF NOT EXISTS children (
      id TEXT PRIMARY KEY, child_code TEXT, full_name TEXT NOT NULL, date_of_birth TEXT NOT NULL,
      sex TEXT NOT NULL, centre_id TEXT NOT NULL, guardian_id TEXT NOT NULL REFERENCES guardians(id),
      allergies TEXT, status TEXT NOT NULL DEFAULT 'ACTIVE',${synced})`,
    `CREATE TABLE IF NOT EXISTS growth_measurements (
      id TEXT PRIMARY KEY, child_id TEXT NOT NULL REFERENCES children(id), measured_on TEXT NOT NULL,
      age_days INTEGER NOT NULL, weight_kg REAL, height_cm REAL, measurement_position TEXT, muac_cm REAL,
      oedema INTEGER NOT NULL DEFAULT 0, waz REAL, haz REAL, whz REAL, nutrition_status TEXT,${synced})`,
    `CREATE TABLE IF NOT EXISTS vaccination_records (
      id TEXT PRIMARY KEY, child_id TEXT NOT NULL REFERENCES children(id), schedule_item_id TEXT NOT NULL,
      due_on TEXT NOT NULL, given_on TEXT, status TEXT NOT NULL,${synced})`,
    `CREATE TABLE IF NOT EXISTS attendance_records (
      id TEXT PRIMARY KEY, child_id TEXT NOT NULL REFERENCES children(id), centre_id TEXT NOT NULL,
      attendance_date TEXT NOT NULL, status TEXT NOT NULL, absence_reason TEXT,${synced},
      UNIQUE (child_id, attendance_date))`,
    `CREATE TABLE IF NOT EXISTS meal_records (
      id TEXT PRIMARY KEY, centre_id TEXT NOT NULL, meal_date TEXT NOT NULL, meal_type TEXT NOT NULL,
      menu_items TEXT NOT NULL, children_served INTEGER NOT NULL, quantity_kg REAL, serving_temp_celsius REAL,
      freshness_rating INTEGER NOT NULL, hygiene_rating INTEGER NOT NULL, child_feedback_score INTEGER,
      local_photo_path TEXT, photo_url TEXT,${synced})`,
    `CREATE TABLE IF NOT EXISTS elderly_beneficiaries (
      id TEXT PRIMARY KEY, beneficiary_code TEXT, full_name TEXT NOT NULL, mobile TEXT, village TEXT,
      living_arrangement TEXT NOT NULL, assigned_worker_id TEXT,${synced})`,
    `CREATE TABLE IF NOT EXISTS vital_readings (
      id TEXT PRIMARY KEY, beneficiary_id TEXT NOT NULL REFERENCES elderly_beneficiaries(id),
      recorded_at TEXT NOT NULL, systolic INTEGER, diastolic INTEGER, pulse INTEGER,
      blood_glucose_mg_dl INTEGER, glucose_context TEXT,${synced})`,
    `CREATE TABLE IF NOT EXISTS visits (
      id TEXT PRIMARY KEY, beneficiary_id TEXT NOT NULL, scheduled_for TEXT NOT NULL, visited_at TEXT,
      status TEXT NOT NULL, purpose TEXT NOT NULL, notes TEXT,${synced})`,
    // Outbox: one row per local change, pushed in order. See src/sync/syncQueue.ts
    `CREATE TABLE IF NOT EXISTS sync_queue (
      op_id TEXT PRIMARY KEY, entity_type TEXT NOT NULL, entity_id TEXT NOT NULL,
      operation TEXT NOT NULL, payload TEXT NOT NULL, client_updated_at TEXT NOT NULL,
      priority INTEGER NOT NULL DEFAULT 5, attempts INTEGER NOT NULL DEFAULT 0,
      status TEXT NOT NULL DEFAULT 'PENDING', last_error TEXT, created_at TEXT NOT NULL)`,
    `CREATE INDEX IF NOT EXISTS ix_sync_queue_pending ON sync_queue (status, priority, created_at)`,
    // Pull cursor per entity type
    `CREATE TABLE IF NOT EXISTS sync_state (entity_type TEXT PRIMARY KEY, last_pulled_at TEXT)`,
  ],
];
