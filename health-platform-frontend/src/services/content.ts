import api from './api';
import type { PageResult } from '@/types/common';
import type { Article } from '@/types/content';

export const fetchArticles = async (params?: {
  q?: string;
  category?: string;
  page?: number;
  size?: number;
}): Promise<PageResult<Article>> => {
  const res = await api.get<PageResult<Article>>('/content/article/search', {
    params
  });
  return res.data;
};

export const fetchArticleDetail = async (id: number): Promise<Article> => {
  const res = await api.get<Article>(`/content/article/${id}`);
  return res.data;
};

export const saveArticle = async (payload: Partial<Article>) => {
  if (payload.articleId) {
    return api.put<Article>(`/content/article/admin/article/${payload.articleId}`, payload);
  }
  return api.post<Article>('/content/article/admin/article', payload);
};

export const submitArticle = async (id: number) => {
  return api.post<Article>(`/content/article/admin/article/${id}/submit`);
};

export const reviewArticle = async (id: number, action: 'approve' | 'reject', reason?: string) => {
  const url = `/content/article/admin/article/${id}/${action}`;
  if (action === 'reject') {
    return api.post<Article>(url, null, { params: { reason } });
  }
  return api.post<Article>(url);
};

export const publishArticle = async (id: number, publish: boolean) => {
  if (publish) {
    return api.post<Article>(`/content/article/admin/article/${id}/publish`);
  }
  return api.post<Article>(`/content/article/admin/article/${id}/unpublish`);
};

export const uploadFile = async (file: File) => {
  const form = new FormData();
  form.append('file', file);
  return api.post<Record<string, string>>('/file/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};
