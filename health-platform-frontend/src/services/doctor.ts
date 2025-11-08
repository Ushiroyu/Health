import api from './api';
import type { PageResult } from '@/types/common';
import type { Department, Doctor, ScheduleSlot } from '@/types/doctor';

export const fetchDepartments = async (): Promise<Department[]> => {
  const res = await api.get<Department[]>('/department/list');
  return res.data;
};

export const searchDoctors = async (params: {
  deptId?: number;
  keyword?: string;
  symptoms?: string;
  status?: string;
  date?: string;
  time?: string;
  page?: number;
  size?: number;
}): Promise<{ content: Doctor[]; totalPages: number; totalElements: number }> => {
  const res = await api.get<{ content: Doctor[]; totalPages: number; totalElements: number }>(
    '/doctor/search',
    { params }
  );
  return res.data;
};

export const fetchDoctorSchedule = async (doctorId: number, date: string): Promise<ScheduleSlot[]> => {
  const res = await api.get<ScheduleSlot[]>('/appointment/schedule', {
    params: { doctorId, date }
  });
  return res.data;
};

export const batchCreateSchedule = async (payload: {
  doctorId: number;
  date: string;
  timeSlots: string[];
  capacity: number;
}) => {
  return api.post<unknown>('/appointment/schedule/batch', payload);
};

export const updateDoctorStatus = async (doctorId: number, action: 'enable' | 'disable') => {
  return api.patch<unknown>(`/admin/doctor/${doctorId}/${action}`);
};

export const upsertDoctor = async (payload: Partial<Doctor>) => {
  if (payload.doctorId) {
    return api.put<Doctor>(`/admin/doctor/${payload.doctorId}`, payload);
  }
  return api.post<Doctor>('/admin/doctor', payload);
};

export const fetchDoctorListAdmin = async (params: {
  deptId?: number;
  status?: string;
  q?: string;
  page?: number;
  size?: number;
}): Promise<PageResult<Doctor>> => {
  const res = await api.get<PageResult<Doctor>>('/admin/doctor', { params });
  return res.data;
};

export const deleteDoctor = async (doctorId: number) => {
  return api.delete<unknown>(`/admin/doctor/${doctorId}`);
};

export const fetchDoctorTags = async (doctorId: number): Promise<string[]> => {
  const res = await api.get<string[]>(`/admin/doctor/${doctorId}/tags`);
  return res.data;
};

export const updateDoctorTags = async (doctorId: number, tags: string[]) => {
  return api.put<unknown>(`/admin/doctor/${doctorId}/tags`, { tags });
};
