<template>
  <aside
    :class="[
      'sidebar',
      { 'sidebar--collapsed': collapsed, 'sidebar--mobile': isMobile, 'sidebar--mobile-open': isMobile && !collapsed }
    ]"
    role="navigation"
    aria-label="主导航"
    :aria-expanded="!collapsed"
    :aria-hidden="isMobile && collapsed"
    :tabindex="isMobile && !collapsed ? 0 : -1"
    @keydown.esc.prevent="handleEscape"
  >
    <div class="sidebar__brand">
      <div class="sidebar__logo">
        <span class="sidebar__logo-dot"></span>
        <el-icon><Grid /></el-icon>
      </div>
      <div v-if="!collapsed" class="sidebar__brand-text">
        <span class="sidebar__brand-title">健康枢纽</span>
        <span class="sidebar__brand-sub">社区健康平台</span>
      </div>
    </div>

    <el-scrollbar class="sidebar__scroll" aria-label="导航菜单">
      <el-menu
        :collapse="collapsed"
        :default-active="active"
        class="sidebar__menu"
        background-color="transparent"
        text-color="rgba(255,255,255,0.92)"
        active-text-color="#fff"
        @select="onSelect"
      >
        <template v-for="item in filteredMenus" :key="item.name">
          <el-menu-item :index="item.name">
            <el-icon class="sidebar__icon">
              <component :is="item.icon" />
            </el-icon>
            <template #title>
              <span>{{ item.label }}</span>
            </template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>

    <div class="sidebar__footer" v-if="auth.user">
      <div class="sidebar__avatar">{{ initials }}</div>
      <div v-if="!collapsed" class="sidebar__user">
        <span class="sidebar__user-name">{{ auth.user?.username }}</span>
        <span class="sidebar__user-role">{{ roleLabel }}</span>
      </div>
    </div>

    <button class="sidebar__toggle" type="button" @click="$emit('toggle')" aria-label="切换侧边导航">
      <el-icon>
        <component :is="collapsed ? ArrowRightBold : ArrowLeftBold" />
      </el-icon>
      <span v-if="!collapsed">收起菜单</span>
    </button>
  </aside>
</template>

<script setup lang="ts">
import type { Component } from 'vue';
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  ArrowLeftBold,
  ArrowRightBold,
  Calendar,
  Collection,
  CollectionTag,
  DataLine,
  Document,
  Flag,
  Grid,
  Guide,
  Histogram,
  Notebook,
  Operation,
  Search,
  Setting,
  Tickets,
  TrendCharts,
  User,
  UserFilled,
  Bell,
  ChatDotRound
} from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';

const props = defineProps<{ collapsed: boolean; isMobile: boolean }>();
const emit = defineEmits<{ (event: 'toggle'): void }>();

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

interface MenuItem {
  name: string;
  label: string;
  icon: Component;
  roles?: string[];
}

const menus: MenuItem[] = [
  { name: 'dashboard', label: '总体概览', icon: Grid },
  { name: 'resident-profile', label: '我的档案', icon: UserFilled },
  { name: 'resident-records', label: '健康记录', icon: Collection },
  { name: 'resident-trends', label: '趋势洞察', icon: TrendCharts },
  { name: 'resident-reminders', label: '健康提醒', icon: Bell },
  { name: 'consult', label: '在线问诊', icon: ChatDotRound, roles: ['RESIDENT', 'DOCTOR', 'ADMIN'] },
  { name: 'guidance-plans', label: '健康指导', icon: Guide, roles: ['RESIDENT', 'DOCTOR', 'ADMIN'] },
  { name: 'doctor-list', label: '医生查询', icon: Search },
  { name: 'appointments-book', label: '预约挂号', icon: Calendar },
  { name: 'appointments-my', label: '我的预约', icon: Tickets },
  { name: 'prescriptions', label: '处方记录', icon: Notebook, roles: ['RESIDENT', 'DOCTOR', 'ADMIN'] },
  { name: 'appointments-schedule', label: '排班管理', icon: Operation, roles: ['ADMIN', 'DOCTOR'] },
  { name: 'content-articles', label: '健康资讯', icon: Document },
  { name: 'content-admin', label: '内容审核', icon: Setting, roles: ['ADMIN', 'DOCTOR'] },
  { name: 'activities', label: '社区活动', icon: Flag },
  { name: 'activities-my', label: '我的活动', icon: CollectionTag, roles: ['RESIDENT', 'DOCTOR', 'ADMIN'] },
  { name: 'activities-admin', label: '活动管理', icon: Operation, roles: ['ADMIN'] },
  { name: 'admin-doctors', label: '医生管理', icon: Operation, roles: ['ADMIN'] },
  { name: 'admin-users', label: '用户管理', icon: User, roles: ['ADMIN'] },
  { name: 'admin-audit', label: '操作审计', icon: Histogram, roles: ['ADMIN'] },
  { name: 'admin-stats', label: '数据分析', icon: DataLine, roles: ['ADMIN'] }
];

const filteredMenus = computed(() => {
  const role = auth.user?.role ?? 'RESIDENT';
  return menus.filter((item) => !item.roles || item.roles.includes(role));
});

const active = computed(() => (route.name ? String(route.name) : 'dashboard'));

const onSelect = (name: string) => {
  router.push({ name });
  if (props.isMobile && !props.collapsed) {
    emit('toggle');
  }
};

const initials = computed(() => {
  const name = auth.user?.username ?? '';
  if (!name.trim()) return 'G';
  return name.length === 1 ? name.toUpperCase() : name.slice(0, 2).toUpperCase();
});

const roleLabel = computed(() => {
  const map: Record<string, string> = {
    ADMIN: 'Admin',
    DOCTOR: 'Doctor',
    RESIDENT: 'Resident'
  };
  return map[auth.user?.role ?? 'RESIDENT'] ?? 'Member';
});

const handleEscape = () => {
  if (props.isMobile && !props.collapsed) {
    emit('toggle');
  }
};
</script>

<style scoped>
.sidebar {
  width: 260px;
  background: var(--sidebar-gradient);
  color: #fff;
  display: flex;
  flex-direction: column;
  position: relative;
  padding: 24px 20px 18px;
  gap: 18px;
  transition: width 0.25s ease;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
  border-right: 1px solid var(--sidebar-border);
  z-index: 15;
}

.sidebar--collapsed {
  width: 92px;
  padding: 24px 12px 18px;
}

.sidebar__brand {
  display: flex;
  align-items: center;
  gap: 14px;
  position: relative;
}

.sidebar__logo {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.08);
  display: grid;
  place-items: center;
  position: relative;
  overflow: hidden;
}

.sidebar__logo::after {
  content: '';
  position: absolute;
  inset: 12px;
  border-radius: 12px;
  background: rgba(79, 70, 229, 0.4);
  filter: blur(18px);
}

.sidebar__logo-dot {
  position: absolute;
  width: 76px;
  height: 76px;
  background: radial-gradient(circle at 30% 30%, rgba(20, 184, 166, 0.45), transparent 60%);
  top: -30px;
  left: -26px;
  filter: blur(4px);
}

.sidebar__brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.35;
}

.sidebar__brand-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.6px;
}

.sidebar__brand-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
}

.sidebar__scroll {
  flex: 1;
  --el-menu-item-font-size: 14px;
}

.sidebar__menu {
  border-right: none;
  background: transparent;
}

.sidebar__menu :deep(.el-menu-item) {
  margin-bottom: 4px;
  border-radius: 12px;
}

.sidebar__menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(79, 70, 229, 0.95) 0%, rgba(20, 184, 166, 0.75) 100%);
  box-shadow: 0 12px 24px rgba(79, 70, 229, 0.35);
}

.sidebar__menu :deep(.el-menu-item:not(.is-active):hover) {
  background: rgba(255, 255, 255, 0.18);
}

.sidebar__icon {
  margin-right: 12px;
  font-size: 16px;
}

.sidebar__footer {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.sidebar__avatar {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.9), rgba(20, 184, 166, 0.9));
  font-weight: 600;
  letter-spacing: 1px;
}

.sidebar__user {
  display: flex;
  flex-direction: column;
  line-height: 1.35;
}

.sidebar__user-name {
  font-weight: 600;
}

.sidebar__user-role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
}

.sidebar__toggle {
  border: none;
  border-radius: 14px;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background 0.2s ease;
}

.sidebar__toggle:hover {
  background: rgba(255, 255, 255, 0.3);
}

.sidebar--collapsed .sidebar__toggle {
  width: 52px;
  margin-inline: auto;
}

.sidebar__toggle:focus-visible {
  outline: none;
  box-shadow: var(--focus-ring);
}

.sidebar--mobile {
  position: fixed;
  inset: 0 auto 0 0;
  height: 100vh;
  transform: translateX(-100%);
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.sidebar--mobile.sidebar--mobile-open {
  transform: translateX(0);
  box-shadow: 24px 0 50px rgba(2, 6, 23, 0.35);
}

.sidebar--mobile.sidebar--collapsed .sidebar__toggle span {
  display: none;
}
</style>
