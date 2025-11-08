import api from './api';
import type { PageResult } from '@/types/common';
import type { JwtUser } from '@/types/user';
import type { AuditLog, HealthRecordStat, AbnormalRateStat } from '@/types/admin';

export const fetchUsers = async (params?: {
  page?: number;
  size?: number;
}): Promise<PageResult<JwtUser>> => {
  const res = await api.get<PageResult<JwtUser>>('/admin/users', { params });
  return res.data;
};

export const createUser = async (payload: {
  username: string;
  password: string;
  role: JwtUser['role'];
}) => {
  return api.post<{ userId: number }>('/admin/users', payload);
};

export const resetPassword = async (userId: number, password: string) => {
  return api.post<unknown>(`/admin/users/${userId}/reset-password`, { password });
};

export const setUserStatus = async (
  userId: number,
  action: 'enable' | 'disable' | 'lock' | 'unlock'
) => {
  return api.patch<unknown>(`/admin/users/${userId}/${action}`);
};

const auditPathMap = {
  auth: '/admin/audit',
  appointment: '/appointment/admin/audit',
  activity: '/activity/admin/audit'
} as const;

export const fetchAuditLogs = async (
  module: keyof typeof auditPathMap,
  params?: { page?: number; size?: number }
): Promise<PageResult<AuditLog>> => {
  const res = await api.get<PageResult<AuditLog>>(auditPathMap[module], {
    params
  });
  return res.data;
};

export const fetchHealthRecordStats = async (params: {
  type: string;
  from: string;
  to: string;
  bucket?: 'day' | 'week' | 'month';
}): Promise<HealthRecordStat[]> => {
  const res = await api.get<HealthRecordStat[]>('/admin/stats/health-records', {
    params
  });
  return res.data;
};

export const fetchAbnormalRateStats = async (params: {
  type: string;
  from: string;
  to: string;
  bucket?: 'day' | 'week' | 'month';
}): Promise<AbnormalRateStat[]> => {
  const res = await api.get<AbnormalRateStat[]>('/admin/stats/abnormal-rate', {
    params
  });
  return res.data;
};
