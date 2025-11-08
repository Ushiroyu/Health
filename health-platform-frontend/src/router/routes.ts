import type { RouteRecordRaw } from 'vue-router';
import AppLayout from '@/layouts/AppLayout.vue';

export const basicRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/landing/HomeShowcase.vue'),
    meta: { public: true, title: '首页' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { public: true, title: '注册' }
  },
  {
    path: '/app',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'dashboard',
        component: () => import('@/views/dashboard/OverviewView.vue'),
        meta: { title: '工作台总览' }
      },
      {
        path: 'resident/profile',
        name: 'resident-profile',
        component: () => import('@/views/resident/ProfileView.vue'),
        meta: { title: '我的档案' }
      },
      {
        path: 'resident/records',
        name: 'resident-records',
        component: () => import('@/views/resident/HealthRecordsView.vue'),
        meta: { title: '健康记录' }
      },
      {
        path: 'resident/trends',
        name: 'resident-trends',
        component: () => import('@/views/resident/HealthTrendsView.vue'),
        meta: { title: '趋势洞察' }
      },
      {
        path: 'resident/reminders',
        name: 'resident-reminders',
        component: () => import('@/views/resident/RemindersView.vue'),
        meta: { title: '健康提醒' }
      },
      {
        path: 'consult',
        name: 'consult',
        component: () => import('@/views/consult/ConsultView.vue'),
        meta: { title: '在线问诊', roles: ['RESIDENT', 'DOCTOR', 'ADMIN'] }
      },
      {
        path: 'doctor/list',
        name: 'doctor-list',
        component: () => import('@/views/doctor/DoctorSearchView.vue'),
        meta: { title: '医生查询' }
      },
      {
        path: 'appointments/book',
        name: 'appointments-book',
        component: () => import('@/views/appointment/BookingView.vue'),
        meta: { title: '预约挂号' }
      },
      {
        path: 'appointments/my',
        name: 'appointments-my',
        component: () => import('@/views/appointment/MyAppointmentsView.vue'),
        meta: { title: '我的预约' }
      },
      {
        path: 'appointments/schedule',
        name: 'appointments-schedule',
        component: () => import('@/views/appointment/ScheduleAdminView.vue'),
        meta: { title: '排班管理', roles: ['DOCTOR', 'ADMIN'] }
      },
      {
        path: 'appointments/prescriptions',
        name: 'prescriptions',
        component: () => import('@/views/appointment/PrescriptionListView.vue'),
        meta: { title: '处方记录', roles: ['RESIDENT', 'DOCTOR', 'ADMIN'] }
      },
      {
        path: 'content/articles',
        name: 'content-articles',
        component: () => import('@/views/content/ArticleListView.vue'),
        meta: { title: '健康资讯' }
      },
      {
        path: 'content/articles/:id',
        name: 'content-article-detail',
        component: () => import('@/views/content/ArticleDetailView.vue'),
        meta: { title: '资讯详情' }
      },
      {
        path: 'content/admin',
        name: 'content-admin',
        component: () => import('@/views/content/ArticleAdminView.vue'),
        meta: { title: '内容管理', roles: ['ADMIN', 'DOCTOR'] }
      },
      {
        path: 'activities',
        name: 'activities',
        component: () => import('@/views/activity/ActivityListView.vue'),
        meta: { title: '社区活动' }
      },
      {
        path: 'activities/my',
        name: 'activities-my',
        component: () => import('@/views/activity/MyRegistrationsView.vue'),
        meta: { title: '我的活动', roles: ['RESIDENT', 'DOCTOR', 'ADMIN'] }
      },
      {
        path: 'activities/:id',
        name: 'activities-detail',
        component: () => import('@/views/activity/ActivityDetailView.vue'),
        meta: { title: '活动详情' }
      },
      {
        path: 'activities/admin',
        name: 'activities-admin',
        component: () => import('@/views/activity/ActivityAdminView.vue'),
        meta: { title: '活动管理', roles: ['ADMIN'] }
      },
      {
        path: 'guidance',
        name: 'guidance-plans',
        component: () => import('@/views/guidance/GuidancePlanView.vue'),
        meta: { title: '健康指导', roles: ['RESIDENT', 'DOCTOR', 'ADMIN'] }
      },
      {
        path: 'admin/doctors',
        name: 'admin-doctors',
        component: () => import('@/views/doctor/DoctorAdminView.vue'),
        meta: { title: '医生管理', roles: ['ADMIN'] }
      },
      {
        path: 'admin/users',
        name: 'admin-users',
        component: () => import('@/views/admin/AdminUserView.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] }
      },
      {
        path: 'admin/audit',
        name: 'admin-audit',
        component: () => import('@/views/admin/AdminAuditView.vue'),
        meta: { title: '操作审计', roles: ['ADMIN'] }
      },
      {
        path: 'admin/stats',
        name: 'admin-stats',
        component: () => import('@/views/admin/AdminStatsView.vue'),
        meta: { title: '数据分析', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/auth/NotFoundView.vue'),
    meta: { public: true, title: '页面不存在' }
  }
];
