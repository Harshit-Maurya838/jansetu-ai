/**
 * Roles and module keys, mirroring docs/rbac-matrix.md. Which modules a role sees comes from the server
 * (`permissions` in the login response). The app only HIDES screens; the backend is the real gate.
 */
export const ROLES = [
  'SUPER_ADMIN',
  'ANGANWADI_WORKER',
  'NGO_WORKER',
  'HEALTH_PROFESSIONAL',
  'PARENT_GUARDIAN',
  'NGO_ADMIN',
  'GOVT_SUPERVISOR',
] as const;
export type Role = (typeof ROLES)[number];

export type ModuleKey =
  | 'child'
  | 'growth'
  | 'vaccination'
  | 'attendance'
  | 'healthcard'
  | 'meal'
  | 'elderly'
  | 'elderlyhealth'
  | 'sos'
  | 'welfare'
  | 'caregiver'
  | 'insights'
  | 'notifications'
  | 'admin'
  | 'sync';

/** Doc number for each module, used to link from TODO screens to docs/modules/NN-*.md */
export const MODULE_DOC: Record<ModuleKey, string> = {
  child: '02',
  growth: '03',
  vaccination: '04',
  attendance: '05',
  healthcard: '06',
  meal: '07',
  elderly: '08',
  elderlyhealth: '09',
  sos: '10',
  welfare: '11',
  caregiver: '12',
  insights: '13',
  notifications: '14',
  admin: '15',
  sync: '16',
};

/**
 * Home-screen modules from the server's `permissions` (login / GET /users/me), which mirror docs/rbac-matrix.md.
 * Unknown keys (from a newer server) are dropped; SOS always comes first.
 */
export function visibleModules(permissions: readonly string[]): ModuleKey[] {
  const known = permissions.filter((p): p is ModuleKey => p in MODULE_DOC);
  return [...new Set(known)].sort((a, b) => (a === 'sos' ? -1 : b === 'sos' ? 1 : 0));
}
