import type { DB } from '@op-engineering/op-sqlite';
import { uuidv4 } from '../utils/uuid';

export type SyncOp = 'CREATE' | 'UPDATE' | 'DELETE';

/** Lower number = pushed first. SOS is never queued behind routine data. */
export const PRIORITY = { SOS: 0, HEALTH: 3, DEFAULT: 5, MEDIA: 8 } as const;

export interface QueuedChange {
  opId: string;
  entityType: string; // server table name, e.g. "growth_measurements"
  entityId: string;
  operation: SyncOp;
  payload: Record<string, unknown>;
  clientUpdatedAt: string;
  priority: number;
}

/**
 * Write the local row and its outbox entry in ONE transaction, so a change is never saved without being queued.
 * Push/pull protocol: docs/modules/16-offline-sync.md and docs/architecture.md#offline-sync-design
 */
export async function enqueue(
  tx: Pick<DB, 'execute'>,
  change: Omit<QueuedChange, 'opId' | 'clientUpdatedAt'>,
): Promise<QueuedChange> {
  const queued: QueuedChange = { ...change, opId: uuidv4(), clientUpdatedAt: new Date().toISOString() };
  await tx.execute(
    `INSERT INTO sync_queue (op_id, entity_type, entity_id, operation, payload, client_updated_at, priority, created_at)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
    [
      queued.opId,
      queued.entityType,
      queued.entityId,
      queued.operation,
      JSON.stringify(queued.payload),
      queued.clientUpdatedAt,
      queued.priority,
      queued.clientUpdatedAt,
    ],
  );
  return queued;
}

// TODO(team): pushPending() – batch of 50 by (priority, created_at) -> POST /sync/push -> mark APPLIED/DUPLICATE
//             as done, CONFLICT -> show in Sync Status screen, network error -> retry with exponential backoff.
// TODO(team): pull() – GET /sync/pull?since=<sync_state.last_pulled_at> -> upsert rows where dirty = 0.
// TODO(team): trigger on app start, on NetInfo "connected", and every 15 min while online.
