import api from './api';
import type { PageResult } from '@/types/common';
import type { CommunityActivity, ActivityRegistration } from '@/types/activity';

export const fetchActivities = async (params?: {
  q?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}): Promise<PageResult<CommunityActivity>> => {
  const res = await api.get<PageResult<CommunityActivity>>('/activity/event/search', {
    params
  });
  return res.data;
};

export const registerActivity = async (eventId: number) => {
  return api.post<Record<string, unknown>>(`/activity/event/${eventId}/register`);
};

export const cancelRegistration = async (eventId: number) => {
  return api.delete<Record<string, unknown>>(`/activity/event/${eventId}/register`);
};

export const adminSaveActivity = async (payload: Partial<CommunityActivity>) => {
  if (payload.eventId) {
    return api.put<CommunityActivity>(`/activity/admin/event/${payload.eventId}`, payload);
  }
  return api.post<CommunityActivity>('/activity/admin/event', payload);
};

export const adminPublishActivity = async (eventId: number, publish: boolean) => {
  if (publish) {
    return api.post<CommunityActivity>(`/activity/admin/event/${eventId}/publish`);
  }
  return api.post<CommunityActivity>(`/activity/admin/event/${eventId}/unpublish`);
};

export const fetchMyRegistrations = async (params?: {
  page?: number;
  size?: number;
}): Promise<PageResult<ActivityRegistration>> => {
  const res = await api.get<PageResult<ActivityRegistration>>('/activity/my-registrations', {
    params
  });
  return res.data;
};
