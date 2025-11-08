import { createRouter, createWebHistory } from 'vue-router';
import { basicRoutes } from './routes';
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: basicRoutes,
  scrollBehavior: () => ({ top: 0 })
});

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore();
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 社区健康管理系统`;
  }
  if (to.meta?.public) {
    return next();
  }
  if (!auth.isAuthenticated) {
    return next({ name: 'login', query: { redirect: to.fullPath } });
  }
  const requiredRoles = (to.meta?.roles as string[] | undefined) ?? [];
  if (requiredRoles.length && !requiredRoles.includes(auth.user?.role ?? '')) {
    return next({ name: 'dashboard' });
  }
  return next();
});

export default router;
