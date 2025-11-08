<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>我的预约</span>
        <el-select
          v-model="filters.status"
          placeholder="状态筛选"
          clearable
          style="width: 180px"
          @change="load"
        >
          <el-option label="已预约" value="booked" />
          <el-option label="已取消" value="canceled" />
        </el-select>
      </div>
    </template>
    <el-table :data="records?.content ?? []">
      <el-table-column prop="apptDate" label="日期" width="120" />
      <el-table-column prop="apptTime" label="时段" width="120" />
      <el-table-column prop="doctorId" label="医生编号" width="120" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="symptom" label="症状描述" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'booked'"
            link
            type="danger"
            :loading="canceling === row.apptId"
            @click="cancel(row.apptId)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="records"
      class="pagination"
      layout="prev, pager, next"
      :total="records.totalElements"
      :page-size="records.size"
      :current-page="records.number + 1"
      @current-change="onPageChange"
    />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import type { PageResult } from '@/types/common';
import type { Appointment } from '@/types/appointment';
import { fetchMyAppointments, cancelAppointment } from '@/services/appointment';
import { ElMessage } from 'element-plus';

const records = ref<PageResult<Appointment>>();
const filters = reactive({
  page: 0,
  status: ''
});
const canceling = ref<number | null>(null);

const load = async () => {
  records.value = await fetchMyAppointments({
    page: filters.page,
    size: 10,
    status: filters.status || undefined
  });
};

onMounted(load);

const onPageChange = (page: number) => {
  filters.page = page - 1;
  load();
};

const cancel = async (id: number) => {
  canceling.value = id;
  try {
    await cancelAppointment(id);
    ElMessage.success('已取消预约');
    load();
  } finally {
    canceling.value = null;
  }
};
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
