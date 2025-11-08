<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>健康提醒</span>
        <el-button type="primary" @click="openDialog">新增提醒</el-button>
      </div>
    </template>
    <el-table :data="list" style="width: 100%">
      <el-table-column prop="type" label="类型" width="160" />
      <el-table-column prop="dailyTime" label="时间" width="140" />
      <el-table-column label="启用" width="120">
        <template #default="{ row }">
          <el-switch v-model="row.enabled" @change="toggle(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增提醒" width="360px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="类型">
        <el-select v-model="form.type">
          <el-option label="血压" value="BP" />
          <el-option label="血糖" value="BG" />
          <el-option label="体重" value="WEIGHT" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间">
        <el-time-picker v-model="form.dailyTime" value-format="HH:mm" />
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
import { ElMessage, ElMessageBox } from 'element-plus';
import type { Reminder } from '@/types/resident';
import { fetchReminders, createReminder, updateReminder, deleteReminder } from '@/services/resident';

const list = ref<Reminder[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);

const form = reactive({
  type: 'BP',
  dailyTime: '08:00'
});

const load = async () => {
  list.value = await fetchReminders();
};

onMounted(load);

const openDialog = () => {
  form.type = 'BP';
  form.dailyTime = '08:00';
  dialogVisible.value = true;
};

const save = async () => {
  saving.value = true;
  try {
    await createReminder(form);
    dialogVisible.value = false;
    ElMessage.success('提醒已创建');
    load();
  } finally {
    saving.value = false;
  }
};

const toggle = async (reminder: Reminder) => {
  await updateReminder(reminder.reminderId, { enabled: reminder.enabled });
  ElMessage.success('状态已更新');
};

const remove = async (reminder: Reminder) => {
  await ElMessageBox.confirm('确认删除该提醒？', '提示');
  await deleteReminder(reminder.reminderId);
  ElMessage.success('已删除');
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
