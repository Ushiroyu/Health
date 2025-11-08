<template>
  <section class="dashboard" aria-labelledby="dashboard-title">
    <h1 id="dashboard-title" class="sr-only">工作台总览</h1>
    <el-row :gutter="16" class="dashboard__summary">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-card__title">{{ card.title }}</div>
          <div class="summary-card__value">{{ card.value }}</div>
          <div class="summary-card__tip">{{ card.tip }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="dashboard__panel" shadow="never">
      <template #header>
        <div class="panel__header">
          <span>快捷入口</span>
          <el-tag type="info" effect="dark">为你而定</el-tag>
        </div>
      </template>
      <el-space wrap>
        <el-button
          v-for="action in quickActions"
          :key="action.name"
          :type="action.type"
          @click="go(action.name)"
        >
          {{ action.label }}
        </el-button>
      </el-space>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const auth = useAuthStore();

const roleLabel: Record<string, string> = {
  ADMIN: '管理员',
  DOCTOR: '医生',
  RESIDENT: '居民',
  STAFF: '工作人员'
};

const cards = computed(() => [
  {
    title: '当前身份',
    value: roleLabel[auth.user?.role ?? 'RESIDENT'],
    tip: '系统将根据角色展示相应功能'
  },
  {
    title: '账号编号',
    value: auth.user?.userId ?? '--',
    tip: '用于客服与支撑人员快速定位'
  },
  {
    title: '系统状态',
    value: '运行正常',
    tip: '后端服务运行于 8080 端口'
  },
  {
    title: '数据安全',
    value: 'AES 加密存储',
    tip: '敏感字段已启用加密持久化'
  }
]);

const quickActions = computed(() => [
  { name: 'resident-profile', label: '维护健康档案', type: 'primary' },
  { name: 'appointments-book', label: '预约挂号', type: 'success' },
  { name: 'content-articles', label: '健康资讯', type: 'info' },
  { name: 'activities', label: '社区活动', type: '' }
]);

const go = (name: string) => router.push({ name });
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.dashboard__summary {
  margin: 0;
}

.summary-card {
  height: 100%;
  padding: 18px 20px;
  border-radius: calc(var(--card-radius) + 4px);
  transition: transform var(--transition-base), box-shadow var(--transition-base);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(255, 255, 255, 0.82) 100%);
  border: 1px solid rgba(255, 255, 255, 0.35);
}

:root[data-theme='dark'] .summary-card {
  background: linear-gradient(180deg, rgba(26, 35, 58, 0.92) 0%, rgba(15, 23, 42, 0.92) 100%);
  border-color: rgba(148, 163, 184, 0.12);
}

.summary-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.15);
}

.summary-card__title {
  font-size: 14px;
  color: var(--app-muted);
}

.summary-card__value {
  margin-top: 12px;
  font-size: 28px;
  font-weight: 600;
  color: var(--app-text);
}

.summary-card__tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--app-muted);
}

.dashboard__panel {
  margin-top: 8px;
  border-radius: calc(var(--card-radius) + 4px);
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: var(--app-surface);
  transition: background var(--transition-base), border-color var(--transition-base);
}

:root[data-theme='dark'] .dashboard__panel {
  border-color: rgba(148, 163, 184, 0.12);
}

.panel__header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
}

@media (max-width: 1024px) {
  .summary-card__value {
    font-size: 24px;
  }
}

@media (max-width: 768px) {
  .summary-card {
    margin-bottom: 12px;
  }
}
</style>
