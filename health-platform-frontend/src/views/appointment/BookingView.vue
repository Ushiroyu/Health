<template>
  <el-card shadow="never">
    <template #header>
      <span>预约挂号</span>
    </template>
    <el-form :model="form" label-width="100px" class="form">
      <el-form-item label="科室">
        <el-select v-model="form.deptId" placeholder="选择科室" @change="loadDoctors">
          <el-option
            v-for="dept in departments"
            :key="dept.deptId"
            :label="dept.deptName"
            :value="dept.deptId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="医生">
        <el-select v-model="form.doctorId" placeholder="选择医生" @change="loadSlots">
          <el-option
            v-for="doctor in doctorOptions"
            :key="doctor.doctorId"
            :label="`${doctor.doctorId} - ${doctor.specialty ?? ''}`"
            :value="doctor.doctorId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="就诊日期">
        <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" @change="loadSlots" />
      </el-form-item>
      <el-form-item label="时段">
        <el-select v-model="form.time" placeholder="选择时段">
          <el-option
            v-for="slot in slots"
            :key="slot.key"
            :label="`${slot.key} (剩余 ${slot.left})`"
            :value="slot.key"
            :disabled="slot.left <= 0"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="症状描述">
        <el-input v-model="form.symptom" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="submit">提交预约</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchDepartments, searchDoctors } from '@/services/doctor';
import { fetchSlots, bookAppointment } from '@/services/appointment';
import type { Department, Doctor } from '@/types/doctor';

const departments = ref<Department[]>([]);
const doctorOptions = ref<Doctor[]>([]);
const slots = ref<Array<{ key: string; left: number }>>([]);
const submitting = ref(false);

const form = reactive({
  deptId: undefined as number | undefined,
  doctorId: undefined as number | undefined,
  date: '',
  time: '',
  symptom: ''
});

onMounted(async () => {
  departments.value = await fetchDepartments();
});

const loadDoctors = async () => {
  if (!form.deptId) return;
  const result = await searchDoctors({ deptId: form.deptId, page: 0, size: 50 });
  doctorOptions.value = result.content;
};

const loadSlots = async () => {
  if (!form.doctorId || !form.date) return;
  const res = await fetchSlots(form.doctorId, form.date);
  slots.value = Object.entries(res).map(([key, left]) => ({ key, left }));
};

const submit = async () => {
  if (!form.doctorId || !form.date || !form.time) {
    ElMessage.warning('请选择完整信息');
    return;
  }
  submitting.value = true;
  try {
    await bookAppointment({
      doctorId: form.doctorId,
      date: form.date,
      time: form.time,
      symptom: form.symptom
    });
    ElMessage.success('预约成功');
    form.time = '';
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.form {
  max-width: 540px;
}
</style>
