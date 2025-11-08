import api from './api';
import type { PageResult } from '@/types/common';
import type { Prescription } from '@/types/prescription';

export const fetchPrescriptions = async (params: {
  userId?: number;
  doctorId?: number;
  page?: number;
  size?: number;
}): Promise<PageResult<Prescription>> => {
  const res = await api.get<PageResult<Prescription>>('/appointment/prescription/list', {
    params
  });
  return res.data;
};

export const getPrescription = async (id: number): Promise<Prescription> => {
  const res = await api.get<Prescription>(`/appointment/prescription/${id}`);
  return res.data;
};

export const createPrescription = async (payload: {
  appointmentId: number;
  userId: number;
  doctorId: number;
  medicines: string;
  advice?: string;
}) => {
  return api.post<{ prescId: number }>('/appointment/prescription', payload);
};
