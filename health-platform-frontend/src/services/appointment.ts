import api from './api';
import type { PageResult } from '@/types/common';
import type { Appointment, AppointmentSearchParams, Slot } from '@/types/appointment';

export const bookAppointment = async (payload: {
  doctorId: number;
  date: string;
  time: string;
  symptom?: string;
}) => {
  return api.post<Record<string, unknown>>('/appointment/book', payload);
};

export const cancelAppointment = async (id: number) => {
  return api.post<unknown>(`/appointment/cancel/${id}`);
};

export const fetchMyAppointments = async (params?: {
  page?: number;
  size?: number;
  status?: string;
}): Promise<PageResult<Appointment>> => {
  const res = await api.get<PageResult<Appointment>>('/appointment/my', {
    params
  });
  return res.data;
};

export const fetchSlots = async (doctorId: number, date: string): Promise<Record<string, number>> => {
  const res = await api.get<Record<string, number>>('/appointment/slots', {
    params: { doctorId, date }
  });
  return res.data;
};

export const searchAppointments = async (
  params: AppointmentSearchParams
): Promise<PageResult<Appointment>> => {
  const res = await api.get<PageResult<Appointment>>('/appointment/search', {
    params
  });
  return res.data;
};

export const fetchScheduleWithLeft = async (doctorId: number, date: string): Promise<Slot[]> => {
  const res = await api.get<Slot[]>('/appointment/schedule', {
    params: { doctorId, date }
  });
  return res.data;
};

export const fetchAppointmentStats = async (params: {
  from: string;
  to: string;
  bucket?: 'day' | 'week' | 'month';
}): Promise<Array<{ bucket: string; total: number; canceled: number }>> => {
  const res = await api.get<Array<{ bucket: string; total: number; canceled: number }>>(
    '/appointment/admin/stats/appointments',
    { params }
  );
  return res.data;
};
