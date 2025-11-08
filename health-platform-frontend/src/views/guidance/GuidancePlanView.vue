<template>
  <el-card shadow="never" v-loading="loading">
    <template #header>
      <div class="card-header">
        <span>健康指导计划</span>
        <el-space v-if="canManage">
          <el-button type="primary" size="small" @click="openDialog()">新建计划</el-button>
          <el-button size="small" @click="load">刷新</el-button>
        </el-space>
      </div>
    </template>

    <el-form :inline="true" class="filter-form">
      <el-form-item label="居民用户 ID">
        <el-input
          v-model="query.userId"
          placeholder="请输入居民用户 ID"
          :disabled="!canManage"
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="plans?.content ?? []" style="width: 100%">
      <el-table-column prop="planId" label="编号" width="90" />
      <el-table-column prop="userId" label="居民" width="100" />
      <el-table-column prop="doctorId" label="医生" width="100" />
      <el-table-column prop="type" label="类型" width="100" />
      <el-table-column prop="frequency" label="频率" width="120" />
      <el-table-column prop="dailyTime" label="提醒时间" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ row.status ?? 'ACTIVE' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rules" label="指导内容">
        <template #default="{ row }">
          <span class="ellipsis">{{ row.rules }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="canManage" label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="确认删除该计划吗？" @confirm="remove(row.planId)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        layout="prev, pager, next"
        :page-size="query.size"
        :current-page="query.page + 1"
        :total="plans?.totalElements ?? 0"
        @current-change="onPageChange"
      />
    </div>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="current ? '编辑计划' : '新建计划'" width="520px">
    <el-form :model="form" label-width="110px">
      <el-form-item label="居民用户 ID">
        <el-input v-model="form.userId" :disabled="!canManage" />
      </el-form-item>
      <el-form-item label="指导类型">
        <el-select v-model="form.type" placeholder="选择类型">
          <el-option label="血压" value="BP" />
          <el-option label="血糖" value="BG" />
          <el-option label="体重" value="WEIGHT" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>
      <el-form-item label="执行频率">
        <el-select v-model="form.frequency" placeholder="选择频率">
          <el-option label="每天" value="DAILY" />
          <el-option label="每周" value="WEEKLY" />
        </el-select>
      </el-form-item>
      <el-form-item label="每日时间">
        <el-time-picker v-model="form.dailyTime" value-format="HH:mm" placeholder="请选择时间" />
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker
          v-model="form.startAt"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker
          v-model="form.endAt"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="指导内容">
        <el-input v-model="form.rules" type="textarea" :rows="4" />
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
import {
  createGuidancePlan,
  deleteGuidancePlan,
  fetchGuidancePlans,
  updateGuidancePlan
} from '@/services/guidance';
import type { GuidancePlan } from '@/types/guidance';
import type { PageResult } from '@/types/common';

const auth = useAuthStore();
const role = computed(() => auth.user?.role ?? 'RESIDENT');
const authUserId = computed(() => auth.user?.userId);
const canManage = computed(() => role.value === 'DOCTOR' || role.value === 'ADMIN');

const query = reactive({
  page: 0,
  size: 10,
  userId: ''
});

const plans = ref<PageResult<GuidancePlan>>();
const loading = ref(false);

const dialogVisible = ref(false);
const saving = ref(false);
const current = ref<GuidancePlan | null>(null);
const form = reactive({
  userId: '',
  type: 'BP',
  frequency: 'DAILY',
  dailyTime: '08:00',
  startAt: '',
  endAt: '',
  rules: ''
});

const resolveUserId = () => {
  const fallback = canManage.value ? query.userId : query.userId || authUserId.value;
  const id = Number(fallback);
  if (Number.isNaN(id)) {
    throw new Error(canManage.value ? '请填写居民用户 ID' : '缺少居民用户 ID');
  }
  return id;
};

const load = async () => {
  loading.value = true;
  try {
    const userId = resolveUserId();
    plans.value = await fetchGuidancePlans({
      userId,
      page: query.page,
      size: query.size
    });
  } catch (error) {
    ElMessage.error((error as Error).message);
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

const openDialog = (plan?: GuidancePlan) => {
  current.value = plan ?? null;
  const baseUserId = plan?.userId ?? (query.userId ? Number(query.userId) : authUserId.value);
  form.userId = baseUserId ? String(baseUserId) : '';
  form.type = plan?.type ?? 'BP';
  form.frequency = plan?.frequency ?? 'DAILY';
  form.dailyTime = plan?.dailyTime ?? '08:00';
  form.startAt = plan?.startAt ? plan.startAt.replace('T', ' ') : '';
  form.endAt = plan?.endAt ? plan.endAt.replace('T', ' ') : '';
  form.rules = plan?.rules ?? '';
  dialogVisible.value = true;
};

const submit = async () => {
  const userId = Number(form.userId);
  if (Number.isNaN(userId)) {
    ElMessage.warning('请输入合法的用户 ID');
    return;
  }
  saving.value = true;
  try {
    if (current.value) {
      await updateGuidancePlan(current.value.planId, {
        userId,
        type: form.type,
        frequency: form.frequency,
        dailyTime: form.dailyTime,
        startAt: form.startAt ? form.startAt.replace(' ', 'T') : undefined,
        endAt: form.endAt ? form.endAt.replace(' ', 'T') : undefined,
        rules: form.rules
      });
      ElMessage.success('计划已更新');
    } else {
      await createGuidancePlan({
        userId,
        type: form.type,
        frequency: form.frequency,
        dailyTime: form.dailyTime,
        startAt: form.startAt ? form.startAt.replace(' ', 'T') : undefined,
        endAt: form.endAt ? form.endAt.replace(' ', 'T') : undefined,
        rules: form.rules
      });
      ElMessage.success('计划已创建');
    }
    dialogVisible.value = false;
    load();
  } finally {
    saving.value = false;
  }
};

const remove = async (planId: number) => {
  await deleteGuidancePlan(planId);
  ElMessage.success('已删除');
  load();
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
