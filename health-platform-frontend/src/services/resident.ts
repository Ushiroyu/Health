import api from './api';
import type { PageResult } from '@/types/common';
import type { HealthProfile, HealthRecord, Reminder, TrendPoint } from '@/types/resident';

export const fetchMyProfile = async (): Promise<HealthProfile> => {
  const res = await api.get<HealthProfile>('/user/profile/me');
  return res.data;
};

export const updateMyProfile = async (payload: Partial<HealthProfile>) => {
  const res = await api.put<Record<string, unknown>>('/user/profile/me', payload);
  return res.data;
};

export const fetchHealthRecords = async (params: {
  type: string;
  page?: number;
  size?: number;
}): Promise<PageResult<HealthRecord>> => {
  const res = await api.get<PageResult<HealthRecord>>('/user/record/list', {
    params
  });
  return res.data;
};

export const saveHealthRecord = async (payload: {
  type: string;
  value: string;
  note?: string;
  date?: string;
}) => {
  return api.post<Record<string, unknown>>('/user/record/save', payload);
};

export const fetchTrend = async (params: {
  type: string;
  from: string;
  to: string;
}): Promise<{ series: TrendPoint[] }> => {
  const res = await api.get<{ series: TrendPoint[] }>('/user/record/trend', {
    params
  });
  return res.data;
};

export const fetchReminders = async (): Promise<Reminder[]> => {
  const res = await api.get<Reminder[]>('/appointment/reminder/list');
  return res.data;
};

export const createReminder = async (payload: {
  type: string;
  dailyTime: string;
  enabled?: boolean;
}) => {
  return api.post<Record<string, unknown>>('/appointment/reminder', payload);
};

export const updateReminder = async (id: number, payload: Partial<Reminder>) => {
  return api.put<Record<string, unknown>>(`/appointment/reminder/${id}`, payload);
};

export const deleteReminder = async (id: number) => {
  return api.delete<Record<string, unknown>>(`/appointment/reminder/${id}`);
};
