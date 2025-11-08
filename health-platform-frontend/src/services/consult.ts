import api from './api';
import type { PageResult } from '@/types/common';
import type { ConsultMessage, ConsultSession } from '@/types/consult';

export const fetchConsultSessions = async (params: {
  page?: number;
  size?: number;
  userId?: number;
  doctorId?: number;
}): Promise<PageResult<ConsultSession>> => {
  const res = await api.get<PageResult<ConsultSession>>('/appointment/consult/session/list', {
    params
  });
  return res.data;
};

export const createConsultSession = async (payload: {
  doctorId: number;
  chiefComplaint?: string;
}): Promise<number> => {
  const res = await api.post<{ sessionId: number }>('/appointment/consult/session', payload);
  return res.data.sessionId;
};

export const acceptConsultSession = async (sessionId: number) => {
  return api.post<unknown>(`/appointment/consult/session/${sessionId}/accept`);
};

export const closeConsultSession = async (sessionId: number) => {
  return api.post<unknown>(`/appointment/consult/session/${sessionId}/close`);
};

export const fetchConsultMessages = async (params: {
  sessionId: number;
  page?: number;
  size?: number;
}): Promise<PageResult<ConsultMessage>> => {
  const res = await api.get<PageResult<ConsultMessage>>('/appointment/consult/messages', {
    params
  });
  return res.data;
};

export const sendConsultMessage = async (payload: {
  sessionId: number;
  content: string;
  contentType?: string;
}) => {
  return api.post<{ msgId: number }>('/appointment/consult/message', {
    ...payload,
    contentType: payload.contentType ?? 'text'
  });
};
