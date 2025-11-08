<template>
  <el-card v-if="activity" shadow="never">
    <template #header>
      <div class="card-header">
        <span>{{ activity.title }}</span>
        <el-space>
          <el-button
            v-if="showRegisterButton"
            type="primary"
            :loading="loading.register"
            @click="handleRegister"
          >
            报名
          </el-button>
          <el-button
            v-if="showCancelButton"
            type="danger"
            :loading="loading.cancel"
            @click="handleCancel"
          >
            取消报名
          </el-button>
        </el-space>
      </div>
    </template>

    <el-descriptions :column="1" border>
      <el-descriptions-item label="时间">{{ format(activity.eventDate) }}</el-descriptions-item>
      <el-descriptions-item label="地点">{{ activity.location }}</el-descriptions-item>
      <el-descriptions-item label="容量">{{ activity.capacity ?? '--' }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ statusLabel }}</el-descriptions-item>
      <el-descriptions-item label="简介">
        <div class="description-text">{{ activity.description }}</div>
      </el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { CommunityActivity } from '@/types/activity';
import {
  fetchActivities,
  fetchMyRegistrations,
  registerActivity,
  cancelRegistration
} from '@/services/activity';
import { formatDate } from '@/utils/format';

const route = useRoute();
const activity = ref<CommunityActivity>();
const registered = ref(false);
const loading = ref({
  register: false,
  cancel: false
});

const load = async () => {
  const id = Number(route.params.id);
  if (!Number.isFinite(id)) return;
  const res = await fetchActivities({ size: 100 });
  activity.value = res.content.find((item) => item.eventId === id);
  await updateRegistration(id);
};

const updateRegistration = async (eventId: number) => {
  try {
    const res = await fetchMyRegistrations({ size: 100 });
    registered.value = res.content.some((item) => item.eventId === eventId);
  } catch {
    registered.value = false;
  }
};

onMounted(load);

const format = (value?: string) => formatDate(value);

const showRegisterButton = computed(() => {
  if (!activity.value) return false;
  const rawStatus = (activity.value.status ?? '').toUpperCase();
  return !registered.value && rawStatus !== 'CLOSED';
});

const showCancelButton = computed(() => registered.value);

const statusLabel = computed(() => {
  if (!activity.value) return '未知';
  if (registered.value) return '已报名';
  const raw = (activity.value.status ?? '').toUpperCase();
  switch (raw) {
    case 'PUBLISHED':
      return '可报名';
    case 'DRAFT':
      return '未发布';
    case 'CLOSED':
      return '已结束';
    default:
      return raw || '未知';
  }
});

const handleRegister = async () => {
  if (!activity.value || registered.value) return;
  loading.value.register = true;
  try {
    await registerActivity(activity.value.eventId);
    registered.value = true;
    ElMessage.success('报名成功');
  } finally {
    loading.value.register = false;
  }
};

const handleCancel = async () => {
  if (!activity.value || !registered.value) return;
  loading.value.cancel = true;
  try {
    await cancelRegistration(activity.value.eventId);
    registered.value = false;
    ElMessage.success('已取消报名');
  } finally {
    loading.value.cancel = false;
  }
};
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.description-text {
  white-space: pre-wrap;
}
</style>
