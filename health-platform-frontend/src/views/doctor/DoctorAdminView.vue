<template>
  <el-card shadow="never" class="page" v-loading="loading">
    <template #header>
      <div class="card-header">
        <el-space wrap>
          <el-select
            v-model="query.deptId"
            clearable
            placeholder="科室"
            style="width: 180px"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.deptId"
              :label="dept.deptName"
              :value="String(dept.deptId)"
            />
          </el-select>
          <el-select
            v-model="query.status"
            clearable
            placeholder="状态"
            style="width: 160px"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-input
            v-model="query.q"
            clearable
            placeholder="姓名 / 专长关键词"
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-space>
        <el-button type="success" @click="openDialog()">新增医生</el-button>
      </div>
    </template>

    <el-table :data="doctors?.content ?? []" style="width: 100%">
      <el-table-column prop="doctorId" label="编号" width="90" />
      <el-table-column prop="userId" label="用户编号" width="110" />
      <el-table-column label="科室" width="150">
        <template #default="{ row }">
          {{ departmentName(row.deptId) }}
        </template>
      </el-table-column>
      <el-table-column prop="title" label="职称" width="140" />
      <el-table-column prop="specialty" label="专长" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-space>
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button
              v-if="row.status !== 'ENABLED'"
              link
              type="success"
              :loading="statusLoading === row.doctorId"
              @click="setStatus(row, 'enable')"
            >
              启用
            </el-button>
            <el-button
              v-else
              link
              type="warning"
              :loading="statusLoading === row.doctorId"
              @click="setStatus(row, 'disable')"
            >
              禁用
            </el-button>
            <el-button link @click="openTagDialog(row)">标签</el-button>
            <el-button
              link
              type="danger"
              :loading="deleteLoading === row.doctorId"
              @click="remove(row)"
            >
              删除
            </el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        layout="prev, pager, next"
        :page-size="query.size"
        :current-page="query.page + 1"
        :total="doctors?.totalElements ?? 0"
        @current-change="onPageChange"
      />
    </div>
  </el-card>

  <el-dialog
    v-model="editDialogVisible"
    :title="form.doctorId ? '编辑医生' : '新增医生'"
    width="520px"
  >
    <el-form :model="form" label-width="110px">
      <el-form-item label="医生编号" v-if="form.doctorId">
        <el-input v-model="form.doctorId" disabled />
      </el-form-item>
      <el-form-item label="关联用户">
        <el-input v-model="form.userId" placeholder="请输入医生对应的用户 ID" />
      </el-form-item>
      <el-form-item label="所属科室">
        <el-select v-model="form.deptId" placeholder="选择科室">
          <el-option
            v-for="dept in departments"
            :key="dept.deptId"
            :label="dept.deptName"
            :value="String(dept.deptId)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="职称">
        <el-input v-model="form.title" />
      </el-form-item>
      <el-form-item label="专长">
        <el-input v-model="form.specialty" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="saveDoctor">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="tagDialogVisible" title="调整标签" width="420px">
    <el-alert type="info" show-icon style="margin-bottom: 12px">
      支持添加多个症状标签，便于居民按症状筛选医生。
    </el-alert>
    <el-select
      v-model="tagValues"
      multiple
      filterable
      default-first-option
      allow-create
      placeholder="输入并回车添加标签"
      style="width: 100%"
    />
    <template #footer>
      <el-button @click="tagDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="savingTags" @click="saveTags">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  deleteDoctor,
  fetchDoctorTags,
  fetchDoctorsAdmin,
  fetchDepartments,
  updateDoctorStatus,
  updateDoctorTags,
  upsertDoctor
} from '@/services/doctor';
import type { Department, Doctor } from '@/types/doctor';
import type { PageResult } from '@/types/common';

const departments = ref<Department[]>([]);
const doctors = ref<PageResult<Doctor>>();
const loading = ref(false);

const statusOptions = [
  { label: '启用', value: 'ENABLED' },
  { label: '停用', value: 'DISABLED' },
  { label: '待审核', value: 'PENDING' }
];

const query = reactive({
  page: 0,
  size: 10,
  deptId: '',
  status: '',
  q: ''
});

const editDialogVisible = ref(false);
const saving = ref(false);
const form = reactive({
  doctorId: '',
  userId: '',
  deptId: '',
  title: '',
  specialty: '',
  status: 'ENABLED'
});

const tagDialogVisible = ref(false);
const savingTags = ref(false);
const tagDoctor = ref<Doctor | null>(null);
const tagValues = ref<string[]>([]);
const statusLoading = ref<number | null>(null);
const deleteLoading = ref<number | null>(null);

const departmentName = (deptId?: number | null) => {
  if (!deptId) return '';
  return departments.value.find((dept) => dept.deptId === deptId)?.deptName ?? '';
};

const statusLabel = (status?: string | null) => {
  const map: Record<string, string> = {
    ENABLED: '启用',
    DISABLED: '停用',
    PENDING: '待审核'
  };
  return status ? map[status] ?? status : '启用';
};

const statusTag = (status?: string | null) => {
  const map: Record<string, string> = {
    ENABLED: 'success',
    DISABLED: 'warning',
    PENDING: 'info'
  };
  return status ? map[status] ?? 'info' : 'success';
};

const load = async () => {
  loading.value = true;
  try {
    const res = await fetchDoctorsAdmin({
      page: query.page,
      size: query.size,
      deptId: query.deptId ? Number(query.deptId) : undefined,
      status: query.status || undefined,
      q: query.q || undefined
    });
    doctors.value = res;
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

const openDialog = (doctor?: Doctor) => {
  form.doctorId = doctor?.doctorId ? String(doctor.doctorId) : '';
  form.userId = doctor?.userId ? String(doctor.userId) : '';
  form.deptId = doctor?.deptId ? String(doctor.deptId) : '';
  form.title = doctor?.title ?? '';
  form.specialty = doctor?.specialty ?? '';
  form.status = doctor?.status ?? 'ENABLED';
  editDialogVisible.value = true;
};

const saveDoctor = async () => {
  const userId = Number(form.userId);
  const deptId = Number(form.deptId);
  if (Number.isNaN(userId) || userId <= 0) {
    ElMessage.warning('请填写有效的用户编号');
    return;
  }
  if (Number.isNaN(deptId) || deptId <= 0) {
    ElMessage.warning('请选择科室');
    return;
  }
  const payload: Partial<Doctor> = {
    doctorId: form.doctorId ? Number(form.doctorId) : undefined,
    userId,
    deptId,
    title: form.title || undefined,
    specialty: form.specialty || undefined,
    status: form.status || undefined
  };
  saving.value = true;
  try {
    await upsertDoctor(payload);
    ElMessage.success('保存成功');
    editDialogVisible.value = false;
    load();
  } finally {
    saving.value = false;
  }
};

const setStatus = async (doctor: Doctor, action: 'enable' | 'disable') => {
  if (!doctor.doctorId) return;
  statusLoading.value = doctor.doctorId;
  try {
    await updateDoctorStatus(doctor.doctorId, action);
    ElMessage.success(action === 'enable' ? '已启用' : '已禁用');
    load();
  } finally {
    statusLoading.value = null;
  }
};

const remove = async (doctor: Doctor) => {
  if (!doctor.doctorId) return;
  try {
    await ElMessageBox.confirm(`确认删除医生 #${doctor.doctorId} 吗？`, '提示', { type: 'warning' });
  } catch {
    return;
  }
  deleteLoading.value = doctor.doctorId;
  try {
    await deleteDoctor(doctor.doctorId);
    ElMessage.success('已删除');
    load();
  } finally {
    deleteLoading.value = null;
  }
};

const openTagDialog = async (doctor: Doctor) => {
  if (!doctor.doctorId) return;
  tagDoctor.value = doctor;
  tagDialogVisible.value = true;
  try {
    const tags = await fetchDoctorTags(doctor.doctorId);
    tagValues.value = Array.isArray(tags) ? [...tags] : [];
  } catch {
    tagValues.value = [];
  }
};

const saveTags = async () => {
  if (!tagDoctor.value?.doctorId) return;
  savingTags.value = true;
  try {
    await updateDoctorTags(tagDoctor.value.doctorId, tagValues.value);
    ElMessage.success('标签已更新');
    tagDialogVisible.value = false;
  } finally {
    savingTags.value = false;
  }
};

onMounted(async () => {
  departments.value = await fetchDepartments();
  load();
});
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
