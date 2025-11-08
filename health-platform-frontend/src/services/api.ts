import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse
} from 'axios';
import { ElMessage } from 'element-plus';
import type { ApiResponse } from '@/types/common';
import { useAuthStore } from '@/stores/auth';

type ApiClient = {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>;
  post<T = unknown>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<ApiResponse<T>>;
  put<T = unknown>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<ApiResponse<T>>;
  patch<T = unknown>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<ApiResponse<T>>;
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>;
};

const normalizeBaseUrl = (value: string | undefined) => {
  if (!value) {
    return '';
  }
  const trimmed = value.trim();
  if (!trimmed) {
    return '';
  }
  return trimmed.endsWith('/') ? trimmed.slice(0, -1) : trimmed;
};

const API_BASE_URL = normalizeBaseUrl(import.meta.env.VITE_API_BASE);

const instance: AxiosInstance = axios.create({
  baseURL: API_BASE_URL || undefined,
  timeout: 12000
});

instance.interceptors.request.use((config) => {
  const auth = useAuthStore();
  const skipAuthHeader =
    typeof config.url === 'string' &&
    (/^\/?auth\//.test(config.url.replace(/^\//, '')) || config.url.startsWith('auth/'));
  if (!skipAuthHeader && auth.token && config.headers) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

instance.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    if (status === 401) {
      const auth = useAuthStore();
      auth.logout();
    }
    ElMessage.error(error?.response?.data?.message ?? error.message ?? '网络错误');
    return Promise.reject(error);
  }
);

const handleResponse = <T>(response: AxiosResponse<ApiResponse<T>>): ApiResponse<T> => {
  const payload = response.data as ApiResponse<T> | unknown;
  if (
    typeof payload === 'object' &&
    payload !== null &&
    'code' in payload &&
    typeof (payload as ApiResponse<T>).code === 'number'
  ) {
    const result = payload as ApiResponse<T>;
    if (result.code !== 0) {
      ElMessage.error(result.message || '请求失败');
      throw result;
    }
    return result;
  }
  return {
    code: 0,
    message: 'ok',
    data: payload as T
  };
};

const request = async <T>(
  config: AxiosRequestConfig
): Promise<ApiResponse<T>> => {
  const response = await instance.request<ApiResponse<T>>(config);
  return handleResponse(response);
};

const api: ApiClient = {
  get<T = unknown>(url: string, config?: AxiosRequestConfig) {
    return request<T>({ ...(config ?? {}), url, method: 'get' });
  },
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return request<T>({ ...(config ?? {}), url, method: 'post', data });
  },
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return request<T>({ ...(config ?? {}), url, method: 'put', data });
  },
  patch<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return request<T>({ ...(config ?? {}), url, method: 'patch', data });
  },
  delete<T = unknown>(url: string, config?: AxiosRequestConfig) {
    return request<T>({ ...(config ?? {}), url, method: 'delete' });
  }
};

export default api;
