import api from './api';
import type { PageResult } from '@/types/common';
import type { GuidancePlan } from '@/types/guidance';

export const fetchGuidancePlans = async (params: {
  userId: number;
  page?: number;
  size?: number;
}): Promise<PageResult<GuidancePlan>> => {
  const res = await api.get<PageResult<GuidancePlan>>('/appointment/guidance/plan/list', {
    params
  });
  return res.data;
};

export const createGuidancePlan = async (payload: {
  userId: number;
  type: string;
  rules?: string;
  frequency?: string;
  dailyTime?: string;
  startAt?: string;
  endAt?: string;
}) => {
  return api.post<{ planId: number }>('/appointment/guidance/plan', payload);
};

export const updateGuidancePlan = async (
  planId: number,
  payload: {
    userId?: number;
    type?: string;
    rules?: string;
    frequency?: string;
    dailyTime?: string;
    startAt?: string;
    endAt?: string;
  }
) => {
  return api.put<unknown>(`/appointment/guidance/plan/${planId}`, payload);
};

export const deleteGuidancePlan = async (planId: number) => {
  return api.delete<unknown>(`/appointment/guidance/plan/${planId}`);
};
