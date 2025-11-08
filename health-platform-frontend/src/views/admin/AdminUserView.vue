<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>用户管理</span>
        <el-button type="primary" @click="openDialog">新增用户</el-button>
      </div>
    </template>

    <el-table :data="users?.content ?? []">
      <el-table-column prop="userId" label="用户 ID" width="120" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="role" label="角色" width="120" />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button link @click="reset(row)">重置密码</el-button>
          <el-button link type="success" @click="setStatus(row, 'enable')">启用</el-button>
          <el-button link type="warning" @click="setStatus(row, 'disable')">禁用</el-button>
          <el-button link type="danger" @click="setStatus(row, 'lock')">锁定</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="users"
      class="pagination"
      layout="prev, pager, next"
      :total="users.totalElements"
      :page-size="users.size"
      :current-page="users.number + 1"
      @current-change="onPageChange"
    />
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增用户" width="400px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="用户名">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="form.role">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="医生" value="DOCTOR" />
          <el-option label="居民" value="RESIDENT" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="create">创建</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { PageResult } from '@/types/common';
import type { JwtUser } from '@/types/user';
import { fetchUsers, createUser, resetPassword, setUserStatus } from '@/services/admin';

const users = ref<PageResult<JwtUser>>();
const dialogVisible = ref(false);
const saving = ref(false);

const form = reactive<{
  username: string;
  password: string;
  role: JwtUser['role'];
}>({
  username: '',
  password: '',
  role: 'RESIDENT'
});

const query = reactive({
  page: 0
});

const load = async () => {
  users.value = await fetchUsers({ page: query.page, size: 10 });
};

onMounted(load);

const onPageChange = (page: number) => {
  query.page = page - 1;
  load();
};

const openDialog = () => {
  form.username = '';
  form.password = '';
  form.role = 'RESIDENT';
  dialogVisible.value = true;
};

const create = async () => {
  saving.value = true;
  try {
    await createUser({
      username: form.username,
      password: form.password,
      role: form.role
    });
    ElMessage.success('用户已创建');
    dialogVisible.value = false;
    load();
  } finally {
    saving.value = false;
  }
};

const reset = async (user: JwtUser) => {
  const { value } = await ElMessageBox.prompt('输入新的密码', '重置密码');
  await resetPassword(user.userId, value);
  ElMessage.success('密码已重置');
};

const setStatus = async (user: JwtUser, action: 'enable' | 'disable' | 'lock' | 'unlock') => {
  await setUserStatus(user.userId, action);
  ElMessage.success('状态已更新');
  load();
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
