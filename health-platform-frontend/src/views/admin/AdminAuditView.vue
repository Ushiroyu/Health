<template>
  <el-card shadow="never">
    <template #header>
      <span>操作审计</span>
    </template>
    <el-tabs v-model="active" @tab-change="load">
      <el-tab-pane label="认证服务" name="auth" />
      <el-tab-pane label="预约服务" name="appointment" />
      <el-tab-pane label="活动服务" name="activity" />
    </el-tabs>
    <el-table :data="logs?.content ?? []">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="ts" label="时间" width="180" />
      <el-table-column prop="userId" label="用户" width="120" />
      <el-table-column prop="action" label="动作" width="200" />
      <el-table-column prop="targetType" label="对象类型" width="140" />
      <el-table-column prop="targetId" label="对象ID" width="140" />
      <el-table-column prop="details" label="详情" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import type { PageResult } from '@/types/common';
import type { AuditLog } from '@/types/admin';
import { fetchAuditLogs } from '@/services/admin';

const active = ref<'auth' | 'appointment' | 'activity'>('auth');
const logs = ref<PageResult<AuditLog>>();

const load = async () => {
  logs.value = await fetchAuditLogs(active.value, { page: 0, size: 50 });
};

onMounted(load);
</script>
