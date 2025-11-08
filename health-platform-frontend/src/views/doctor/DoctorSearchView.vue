<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>医生查询</span>
        <el-space>
          <el-select v-model="filters.deptId" placeholder="科室" clearable style="width: 180px">
            <el-option
              v-for="dept in departments"
              :key="dept.deptId"
              :label="dept.deptName"
              :value="dept.deptId"
            />
          </el-select>
          <el-input v-model="filters.keyword" placeholder="关键词" clearable style="width: 200px" />
          <el-date-picker v-model="filters.date" type="date" value-format="YYYY-MM-DD" />
          <el-button type="primary" @click="search">查询</el-button>
        </el-space>
      </div>
    </template>

    <el-table :data="doctors" style="width: 100%">
      <el-table-column prop="doctorId" label="编号" width="100" />
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column prop="specialty" label="专长" />
      <el-table-column label="标签" width="200">
        <template #default="{ row }">
          <el-tag
            v-for="tag in row.tags ?? []"
            :key="tag"
            size="small"
            style="margin-right: 4px"
          >
            {{ tag }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="可预约" width="100">
        <template #default="{ row }">
          <el-tag :type="row.available ? 'success' : 'info'">
            {{ row.available ? '可预约' : '暂无号源' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewSchedule(row)">查看排班</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-drawer v-model="drawerVisible" :title="`排班 - ${currentDoctor?.doctorId ?? ''}`" size="30%">
    <el-table :data="schedule" style="width: 100%">
      <el-table-column prop="time" label="时段" width="140" />
      <el-table-column prop="capacity" label="容量" width="100" />
      <el-table-column prop="left" label="剩余" width="100" />
      <el-table-column prop="status" label="状态" />
    </el-table>
  </el-drawer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { fetchDepartments, searchDoctors, fetchDoctorSchedule } from '@/services/doctor';
import type { Department, Doctor, ScheduleSlot } from '@/types/doctor';

const departments = ref<Department[]>([]);
const doctors = ref<Doctor[]>([]);
const schedule = ref<ScheduleSlot[]>([]);
const drawerVisible = ref(false);
const currentDoctor = ref<Doctor | null>(null);

const filters = reactive({
  deptId: undefined as number | undefined,
  keyword: '',
  date: ''
});

onMounted(async () => {
  departments.value = await fetchDepartments();
  const today = new Date().toISOString().slice(0, 10);
  filters.date = today;
  search();
});

const search = async () => {
  const result = await searchDoctors({
    deptId: filters.deptId,
    keyword: filters.keyword,
    date: filters.date,
    page: 0,
    size: 50
  });
  doctors.value = result.content;
};

const viewSchedule = async (doctor: Doctor) => {
  currentDoctor.value = doctor;
  schedule.value = await fetchDoctorSchedule(doctor.doctorId, filters.date ?? '');
  drawerVisible.value = true;
};
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
