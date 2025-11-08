<template>
  <el-card shadow="never" v-loading="loading">
    <template #header>
      <div class="card-header">
        <span>处方记录</span>
        <el-space>
          <el-button size="small" @click="load">刷新</el-button>
          <el-button v-if="canCreate" type="primary" size="small" @click="openDialog()">
            新建处方
          </el-button>
        </el-space>
      </div>
    </template>

    <el-form :inline="true" class="filter-form">
      <el-form-item label="居民用户 ID">
        <el-input
          v-model="query.userId"
          placeholder="居民用户 ID"
          :disabled="!canEditUserFilter"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="医生编号">
        <el-input
          v-model="query.doctorId"
          placeholder="医生编号"
          :disabled="!canEditDoctorFilter"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="prescriptions?.content ?? []" style="width: 100%">
      <el-table-column prop="prescId" label="编号" width="100" />
      <el-table-column prop="userId" label="居民" width="120" />
      <el-table-column prop="doctorId" label="医生" width="120" />
      <el-table-column prop="appointmentId" label="预约" width="120" />
      <el-table-column prop="prescDate" label="开具时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.prescDate, 'YYYY-MM-DD HH:mm') }}
        </template>
      </el-table-column>
      <el-table-column label="用药">
        <template #default="{ row }">
          <span class="ellipsis">{{ row.medicines }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="advice" label="医嘱">
        <template #default="{ row }">
          <span class="ellipsis">{{ row.advice }}</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        layout="prev, pager, next"
        :page-size="query.size"
        :current-page="query.page + 1"
        :total="prescriptions?.totalElements ?? 0"
        @current-change="onPageChange"
      />
    </div>
  </el-card>

  <el-dialog v-model="dialogVisible" title="新建处方" width="520px">
    <el-form :model="form" label-width="110px">
      <el-form-item label="预约编号">
        <el-input v-model="form.appointmentId" />
      </el-form-item>
      <el-form-item label="居民用户 ID">
        <el-input v-model="form.userId" />
      </el-form-item>
      <el-form-item label="医生编号">
        <el-input v-model="form.doctorId" />
      </el-form-item>
      <el-form-item label="药品信息">
        <el-input v-model="form.medicines" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item label="医嘱">
        <el-input v-model="form.advice" type="textarea" :rows="4" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { createPrescription, fetchPrescriptions } from '@/services/prescription';
import type { Prescription } from '@/types/prescription';
import type { PageResult } from '@/types/common';
import { formatDate } from '@/utils/format';

const auth = useAuthStore();
const role = computed(() => auth.user?.role ?? 'RESIDENT');
const authUserId = computed(() => auth.user?.userId);
const canCreate = computed(() => role.value === 'DOCTOR' || role.value === 'ADMIN');
const canEditUserFilter = computed(() => role.value !== 'RESIDENT');
const canEditDoctorFilter = computed(() => role.value !== 'RESIDENT');

const query = reactive({
  page: 0,
  size: 10,
  userId: '',
  doctorId: ''
});

const prescriptions = ref<PageResult<Prescription>>();
const loading = ref(false);

const dialogVisible = ref(false);
const saving = ref(false);
const form = reactive({
  appointmentId: '',
  userId: '',
  doctorId: '',
  medicines: '',
  advice: ''
});

const buildQueryParams = () => {
  const params: Record<string, number> = {
    page: query.page,
    size: query.size
  };
  const userId = Number(query.userId || authUserId.value);
  const doctorId = Number(query.doctorId);
  if (!Number.isNaN(userId)) {
    params.userId = userId;
  }
  if (!Number.isNaN(doctorId) && query.doctorId) {
    params.doctorId = doctorId;
  }
  return params;
};

const load = async () => {
  loading.value = true;
  try {
    prescriptions.value = await fetchPrescriptions(buildQueryParams());
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  query.page = 0;
  load();
};

const onPageChange = (page: number) => {
  query.page = page - 1;
  load();
};

const openDialog = () => {
  form.appointmentId = '';
  form.userId = query.userId || String(authUserId.value ?? '');
  form.doctorId = query.doctorId;
  form.medicines = '';
  form.advice = '';
  dialogVisible.value = true;
};

const submit = async () => {
  const appointmentId = Number(form.appointmentId);
  const userId = Number(form.userId);
  const doctorId = Number(form.doctorId);
  if ([appointmentId, userId, doctorId].some((v) => Number.isNaN(v) || v <= 0)) {
    ElMessage.warning('请填写有效的预约、居民与医生编号');
    return;
  }
  saving.value = true;
  try {
    await createPrescription({
      appointmentId,
      userId,
      doctorId,
      medicines: form.medicines,
      advice: form.advice || undefined
    });
    ElMessage.success('处方已创建');
    dialogVisible.value = false;
    load();
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  if (!query.userId && authUserId.value) {
    query.userId = String(authUserId.value);
  }
  load();
});
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 12px;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.ellipsis {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
