export interface AuditLog {
  id: number;
  ts: string;
  userId?: number;
  action: string;
  targetType?: string;
  targetId?: string;
  details?: string;
  ip?: string;
}

export interface HealthRecordStat {
  bucket: string;
  count: number;
}

export interface AbnormalRateStat {
  bucket: string;
  total: number;
  abnormal: number;
  rate: number;
}
