<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <el-input v-model="filters.q" placeholder="关键词" clearable style="width: 220px" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>
    </template>
    <el-timeline>
      <el-timeline-item
        v-for="activity in list?.content ?? []"
        :key="activity.eventId"
        :timestamp="format(activity.eventDate)"
      >
        <div class="activity">
          <h3>{{ activity.title }}</h3>
          <p>{{ activity.description }}</p>
          <el-space>
            <el-button type="primary" @click="view(activity.eventId)">详情</el-button>
            <el-tag>{{ activity.status }}</el-tag>
          </el-space>
        </div>
      </el-timeline-item>
    </el-timeline>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { PageResult } from '@/types/common';
import type { CommunityActivity } from '@/types/activity';
import { fetchActivities } from '@/services/activity';
import { formatDate } from '@/utils/format';

const router = useRouter();
const list = ref<PageResult<CommunityActivity>>();

const filters = reactive({
  q: ''
});

const load = async () => {
  list.value = await fetchActivities({ q: filters.q || undefined });
};

onMounted(load);

const format = (value?: string) => formatDate(value, '');
const view = (id: number) => router.push({ name: 'activities-detail', params: { id } });
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.activity {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
