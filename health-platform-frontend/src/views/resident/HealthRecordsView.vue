<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>健康数据记录</span>
        <el-space>
          <el-select v-model="query.type" placeholder="选择类型" style="width: 160px">
            <el-option label="血压" value="BP" />
            <el-option label="血糖" value="BG" />
            <el-option label="体重" value="WEIGHT" />
            <el-option label="心率" value="HR" />
          </el-select>
          <el-button type="primary" @click="openDialog">新增记录</el-button>
        </el-space>
      </div>
    </template>

    <el-table :data="records?.content ?? []" style="width: 100%">
      <el-table-column prop="recordDate" label="日期" width="140" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column prop="value" label="数值" width="140" />
      <el-table-column prop="note" label="备注" />
    </el-table>

    <el-pagination
      v-if="records"
      class="pagination"
      layout="prev, pager, next, jumper"
      :total="records.totalElements"
      :page-size="records.size"
      :current-page="records.number + 1"
      @current-change="onPageChange"
    />
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增健康记录" width="420px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="类型">
        <el-select v-model="form.type">
          <el-option label="血压" value="BP" />
          <el-option label="血糖" value="BG" />
          <el-option label="体重" value="WEIGHT" />
          <el-option label="心率" value="HR" />
        </el-select>
      </el-form-item>
      <el-form-item label="数值">
        <el-input v-model="form.value" placeholder="例如 120/80 或 5.4" />
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.note" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import type { PageResult } from '@/types/common';
import type { HealthRecord } from '@/types/resident';
import { fetchHealthRecords, saveHealthRecord } from '@/services/resident';

const records = ref<PageResult<HealthRecord>>();
const query = reactive({
  type: 'BP',
  page: 0
});

const dialogVisible = ref(false);
const saving = ref(false);

const form = reactive({
  type: 'BP',
  value: '',
  date: '',
  note: ''
});

const load = async () => {
  const res = await fetchHealthRecords({ type: query.type, page: query.page, size: 10 });
  records.value = res;
};

onMounted(load);

watch(
  () => query.type,
  () => {
    query.page = 0;
    load();
  }
);

const onPageChange = (page: number) => {
  query.page = page - 1;
  load();
};

const openDialog = () => {
  form.type = query.type;
  form.value = '';
  form.note = '';
  form.date = '';
  dialogVisible.value = true;
};

const save = async () => {
  saving.value = true;
  try {
    await saveHealthRecord(form);
    dialogVisible.value = false;
    ElMessage.success('记录已保存');
    load();
  } finally {
    saving.value = false;
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
