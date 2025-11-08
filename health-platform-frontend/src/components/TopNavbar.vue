<template>
  <header class="navbar">
    <div class="navbar__left">
      <button class="navbar__toggle" type="button" @click="$emit('toggle-sidebar')">
        <el-icon><Menu /></el-icon>
      </button>
      <div class="navbar__title">
        <span class="navbar__heading">社区健康指挥中心</span>
        <span class="navbar__tagline">一站式掌握社区健康数据，提升服务协同效率</span>
      </div>
    </div>
    <div class="navbar__right">
      <div class="navbar__meta">
        <span class="navbar__greeting">你好，{{ displayName }}</span>
        <span class="navbar__role">{{ roleLabel }}</span>
      </div>
      <div class="navbar__avatar">{{ initials }}</div>
      <button
        class="navbar__theme"
        type="button"
        :aria-pressed="isDark"
        :title="themeTitle"
        @click="toggleTheme"
      >
        <el-icon>
          <component :is="isDark ? Moon : Sunny" />
        </el-icon>
        <span class="sr-only">{{ themeTitle }}</span>
      </button>
      <el-dropdown>
        <span class="navbar__dropdown">
          快速操作
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="toHome">返回首页</el-dropdown-item>
            <el-dropdown-item @click="toDashboard">返回工作台</el-dropdown-item>
            <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowDown, Menu, Moon, Sunny } from '@element-plus/icons-vue';
import { useDark, useToggle } from '@vueuse/core';
import { useAuthStore } from '@/stores/auth';

defineEmits<{ (event: 'toggle-sidebar'): void }>();

const auth = useAuthStore();
const router = useRouter();

const isDark = useDark({
  selector: document.documentElement,
  attribute: 'data-theme',
  valueLight: 'light',
  valueDark: 'dark',
  storageKey: 'hp-preferred-theme'
});
const toggleDark = useToggle(isDark);

const toggleTheme = () => {
  toggleDark();
};

const themeTitle = computed(() => (isDark.value ? '切换至亮色模式' : '切换至暗色模式'));

const displayName = computed(() => auth.user?.username ?? '访客');
const initials = computed(() => {
  const name = displayName.value.trim();
  if (!name) return 'G';
  return name.length === 1 ? name.toUpperCase() : name.slice(0, 2).toUpperCase();
});

const roleLabel = computed(() => {
  const role = auth.user?.role ?? 'RESIDENT';
  const map: Record<string, string> = {
    ADMIN: '平台管理员',
    DOCTOR: '社区医生',
    RESIDENT: '社区居民'
  };
  return map[role] ?? '社区居民';
});

const logout = () => {
  auth.logout();
  router.push({ name: 'login' });
};

const toHome = () => {
  router.push({ name: 'home' });
};

const toDashboard = () => {
  router.push({ name: 'dashboard' });
};
</script>

<style scoped>
.navbar {
  height: 76px;
  margin: 24px 36px 0;
  padding: 14px 26px;
  border-radius: calc(var(--card-radius) + 12px);
  background: var(--navbar-blur);
  backdrop-filter: blur(18px);
  box-shadow: var(--card-shadow);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid rgba(255, 255, 255, 0.65);
}

.navbar__left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.navbar__toggle {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  border: none;
  background: rgba(79, 70, 229, 0.14);
  color: var(--app-primary);
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: all 0.25s ease;
}

.navbar__toggle:hover {
  background: rgba(79, 70, 229, 0.25);
  transform: translateY(-1px);
  box-shadow: 0 10px 25px rgba(79, 70, 229, 0.2);
}

.navbar__title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.navbar__heading {
  font-weight: 700;
  font-size: 20px;
  letter-spacing: 0.6px;
}

.navbar__tagline {
  font-size: 13px;
  color: var(--app-muted);
}

.navbar__right {
  display: flex;
  align-items: center;
  gap: 18px;
}

.navbar__theme {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  border: 1px solid transparent;
  background: rgba(79, 70, 229, 0.14);
  color: var(--app-primary);
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: all var(--transition-base);
}

.navbar__theme:hover {
  background: rgba(79, 70, 229, 0.22);
  transform: translateY(-1px);
}

.navbar__theme:focus-visible {
  box-shadow: var(--focus-ring);
}

.navbar__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  line-height: 1.4;
}

.navbar__greeting {
  font-weight: 600;
}

.navbar__role {
  font-size: 12px;
  color: var(--app-muted);
}

.navbar__avatar {
  width: 44px;
  height: 44px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--app-primary) 0%, var(--app-accent) 100%);
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 600;
  box-shadow: 0 16px 24px rgba(79, 70, 229, 0.25);
}

.navbar__dropdown {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(79, 70, 229, 0.12);
  color: var(--app-primary);
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
}

.navbar__dropdown:hover {
  background: rgba(79, 70, 229, 0.22);
}

@media (max-width: 1024px) {
  .navbar {
    margin: 18px 20px 0;
    padding: 12px 18px;
  }

  .navbar__tagline {
    display: none;
  }

  .navbar__right {
    gap: 12px;
  }
}
</style>
