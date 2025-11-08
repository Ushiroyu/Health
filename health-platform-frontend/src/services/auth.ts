import api from './api';
import type { JwtUser } from '@/types/user';

export interface LoginPayload {
  username: string;
  password: string;
}

interface LoginResponse {
  token: string;
  role: JwtUser['role'];
  userId: number;
}

export const loginApi = async (payload: LoginPayload): Promise<JwtUser> => {
  const res = await api.post<LoginResponse>('/auth/login', payload);
  const data = res.data;
  return {
    userId: data.userId,
    username: payload.username,
    role: data.role,
    token: data.token
  };
};

export const registerApi = async (payload: {
  username: string;
  password: string;
  role?: JwtUser['role'];
}): Promise<{ userId: number }> => {
  const res = await api.post<{ userId: number }>('/auth/register', {
    ...payload,
    role: payload.role ?? 'RESIDENT'
  });
  return res.data;
};
