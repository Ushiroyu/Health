<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>活动管理</span>
        <el-button type="primary" @click="openDialog()">新建活动</el-button>
      </div>
    </template>
    <el-table :data="list">
      <el-table-column prop="eventId" label="编号" width="100" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="eventDate" label="时间" width="180" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link @click="openDialog(row)">编辑</el-button>
          <el-button link type="success" @click="publish(row, true)">发布</el-button>
          <el-button link type="warning" @click="publish(row, false)">下线</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="current?.eventId ? '编辑活动' : '新增活动'" width="520px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="标题">
        <el-input v-model="form.title" />
      </el-form-item>
      <el-form-item label="时间">
        <el-date-picker v-model="form.eventDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
      <el-form-item label="地点">
        <el-input v-model="form.location" />
      </el-form-item>
      <el-form-item label="容量">
        <el-input-number v-model="form.capacity" :min="1" />
      </el-form-item>
      <el-form-item label="简介">
        <el-input v-model="form.description" type="textarea" :rows="4" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { CommunityActivity } from '@/types/activity';
import { fetchActivities, adminSaveActivity, adminPublishActivity } from '@/services/activity';

const list = ref<CommunityActivity[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);
const current = ref<CommunityActivity | null>(null);

const form = reactive<Partial<CommunityActivity>>({
  title: '',
  eventDate: '',
  location: '',
  capacity: 100,
  description: ''
});

const load = async () => {
  const res = await fetchActivities({ size: 100 });
  list.value = res.content;
};

onMounted(load);

const openDialog = (activity?: CommunityActivity) => {
  current.value = activity ?? null;
  Object.assign(form, {
    eventId: activity?.eventId,
    title: activity?.title ?? '',
    eventDate: activity?.eventDate ? activity.eventDate.replace('T', ' ') : '',
    location: activity?.location ?? '',
    capacity: activity?.capacity ?? 100,
    description: activity?.description ?? ''
  });
  dialogVisible.value = true;
};

const save = async () => {
  saving.value = true;
  try {
    const payload = {
      ...form,
      eventDate: form.eventDate ? form.eventDate.replace(' ', 'T') : undefined
    };
    await adminSaveActivity(payload);
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    load();
  } finally {
    saving.value = false;
  }
};

const publish = async (activity: CommunityActivity, value: boolean) => {
  await adminPublishActivity(activity.eventId, value);
  ElMessage.success(value ? '已发布' : '已下线');
  load();
};
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
