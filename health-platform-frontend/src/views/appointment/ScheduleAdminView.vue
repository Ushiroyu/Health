<template>
  <el-card shadow="never">
    <template #header>
      <span>排班管理</span>
    </template>
    <el-form :model="form" label-width="100px" class="form">
      <el-form-item label="医生编号">
        <el-input-number v-model="form.doctorId" :min="1" />
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="容量">
        <el-input-number v-model="form.capacity" :min="1" />
      </el-form-item>
      <el-form-item label="时段列表">
        <el-select v-model="form.timeSlots" multiple placeholder="选择时段">
          <el-option
            v-for="slot in timePresets"
            :key="slot"
            :label="slot"
            :value="slot"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">批量创建</el-button>
      </el-form-item>
    </el-form>

    <el-divider />

    <el-form :inline="true" :model="query">
      <el-form-item label="医生编号">
        <el-input-number v-model="query.doctorId" :min="1" />
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="query.date" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查看排班</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="schedule">
      <el-table-column prop="time" label="时段" width="120" />
      <el-table-column prop="capacity" label="容量" width="120" />
      <el-table-column prop="left" label="剩余" width="120" />
      <el-table-column prop="status" label="状态" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { batchCreateSchedule, fetchDoctorSchedule } from '@/services/doctor';

const form = reactive({
  doctorId: 0,
  date: '',
  capacity: 10,
  timeSlots: [] as string[]
});

const query = reactive({
  doctorId: 0,
  date: ''
});

const schedule = ref<Array<{ time: string; capacity: number; left: number; status: string }>>([]);
const loading = ref(false);

const timePresets = [
  '08:00-08:30',
  '08:30-09:00',
  '09:00-09:30',
  '09:30-10:00',
  '10:00-10:30',
  '10:30-11:00',
  '13:30-14:00',
  '14:00-14:30',
  '14:30-15:00'
];

const submit = async () => {
  if (!form.doctorId || !form.date || form.timeSlots.length === 0) {
    ElMessage.warning('请填写完整信息');
    return;
  }
  loading.value = true;
  try {
    await batchCreateSchedule({
      doctorId: form.doctorId,
      date: form.date,
      timeSlots: form.timeSlots,
      capacity: form.capacity
    });
    ElMessage.success('排班已更新');
    if (query.doctorId === form.doctorId && query.date === form.date) {
      load();
    }
  } finally {
    loading.value = false;
  }
};

const load = async () => {
  if (!query.doctorId || !query.date) {
    ElMessage.warning('请输入医生编号与日期');
    return;
  }
  schedule.value = await fetchDoctorSchedule(query.doctorId, query.date);
};
</script>

<style scoped>
.form {
  max-width: 520px;
}
</style>
