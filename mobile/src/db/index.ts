import { open, type DB } from '@op-engineering/op-sqlite';
import { DB_VERSION, MIGRATIONS } from './schema';

let db: DB | null = null;

/**
 * Opens the encrypted local DB and applies pending migrations.
 * TODO(team): generate a random key on first launch and keep it in Keychain; never hard-code it.
 */
export async function getDb(encryptionKey: string): Promise<DB> {
  if (db) return db;
  db = open({ name: 'jansetu.db', encryptionKey });
  const res = await db.execute('PRAGMA user_version');
  const current = Number(res.rows[0]?.user_version ?? 0);
  for (let v = current; v < DB_VERSION; v++) {
    await db.transaction(async tx => {
      for (const sql of MIGRATIONS[v]) await tx.execute(sql);
    });
    await db.execute(`PRAGMA user_version = ${v + 1}`);
  }
  return db;
}
