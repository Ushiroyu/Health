<template>
  <el-card shadow="never" v-loading="loading">
    <template #header>
      <div class="card-header">
        <span>我的活动报名</span>
        <el-button size="small" @click="load">刷新</el-button>
      </div>
    </template>

    <el-table :data="registrations?.content ?? []" style="width: 100%">
      <el-table-column prop="regId" label="报名编号" width="120" />
      <el-table-column prop="eventId" label="活动编号" width="120" />
      <el-table-column prop="regTime" label="报名时间" width="200">
        <template #default="{ row }">
          {{ formatDate(row.regTime, 'YYYY-MM-DD HH:mm') }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag size="small">{{ row.status ?? '报名成功' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="goDetail(row.eventId)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        layout="prev, pager, next"
        :page-size="query.size"
        :current-page="query.page + 1"
        :total="registrations?.totalElements ?? 0"
        @current-change="onPageChange"
      />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchMyRegistrations } from '@/services/activity';
import type { ActivityRegistration } from '@/types/activity';
import type { PageResult } from '@/types/common';
import { formatDate } from '@/utils/format';

const router = useRouter();

const query = reactive({
  page: 0,
  size: 10
});

const registrations = ref<PageResult<ActivityRegistration>>();
const loading = ref(false);

const load = async () => {
  loading.value = true;
  try {
    registrations.value = await fetchMyRegistrations({
      page: query.page,
      size: query.size
    });
  } finally {
    loading.value = false;
  }
};

const onPageChange = (page: number) => {
  query.page = page - 1;
  load();
};

const goDetail = (eventId: number) => {
  router.push({ name: 'activities-detail', params: { id: eventId } });
};

onMounted(load);
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
