/**
 * Roles and module access, mirroring docs/rbac-matrix.md.
 * The app only HIDES screens a role cannot use; the backend is the real gate (@PreAuthorize + data scoping).
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

const ALL: ModuleKey[] = Object.keys(MODULE_DOC) as ModuleKey[];

/** Which module tiles each role sees on the home screen (any access level, incl. read-only). */
export const MODULE_ACCESS: Record<Role, ModuleKey[]> = {
  SUPER_ADMIN: ALL,
  ANGANWADI_WORKER: ['child', 'growth', 'vaccination', 'attendance', 'healthcard', 'meal', 'insights', 'notifications', 'sync'],
  NGO_WORKER: ['elderly', 'elderlyhealth', 'sos', 'welfare', 'caregiver', 'healthcard', 'insights', 'notifications', 'sync'],
  HEALTH_PROFESSIONAL: ['child', 'growth', 'vaccination', 'healthcard', 'elderly', 'elderlyhealth', 'insights', 'notifications'],
  PARENT_GUARDIAN: ['child', 'growth', 'vaccination', 'attendance', 'healthcard', 'notifications'],
  NGO_ADMIN: ['elderly', 'elderlyhealth', 'sos', 'welfare', 'caregiver', 'healthcard', 'insights', 'notifications', 'admin'],
  GOVT_SUPERVISOR: ['child', 'growth', 'vaccination', 'attendance', 'meal', 'insights', 'notifications', 'admin'],
};

export const canSee = (role: Role, module: ModuleKey): boolean => MODULE_ACCESS[role].includes(module);
