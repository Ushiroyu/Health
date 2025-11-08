import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import type { JwtUser } from '@/types/user';
import { loginApi } from '@/services/auth';

const TOKEN_KEY = 'hp_token';
const USER_KEY = 'hp_user';

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY));
  let initialUser: JwtUser | null = null;
  const cached = localStorage.getItem(USER_KEY);
  if (cached) {
    try {
      initialUser = JSON.parse(cached) as JwtUser;
    } catch {
      localStorage.removeItem(USER_KEY);
    }
  }
  const user = ref<JwtUser | null>(initialUser);
  const loading = ref(false);

  const isAuthenticated = computed(() => Boolean(token.value));

  const setToken = (value: string | null) => {
    token.value = value;
    if (value) {
      localStorage.setItem(TOKEN_KEY, value);
    } else {
      localStorage.removeItem(TOKEN_KEY);
    }
  };

  const setUser = (value: JwtUser | null) => {
    user.value = value;
    if (value) {
      localStorage.setItem(USER_KEY, JSON.stringify(value));
    } else {
      localStorage.removeItem(USER_KEY);
    }
  };

  const login = async (username: string, password: string) => {
    loading.value = true;
    try {
      const data = await loginApi({ username, password });
      setToken(data.token);
      setUser(data);
    } finally {
      loading.value = false;
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
  };

  return {
    token,
    user,
    loading,
    isAuthenticated,
    login,
    logout,
    setToken,
    setUser
  };
});
