<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>个人健康档案</span>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </div>
    </template>
    <el-form :model="form" label-width="100px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="姓名">
            <el-input v-model="form.fullName" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别">
            <el-select v-model="form.gender">
              <el-option label="男" value="MALE" />
              <el-option label="女" value="FEMALE" />
              <el-option label="未知" value="UNKNOWN" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="出生日期">
            <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="血型">
            <el-select v-model="form.bloodType">
              <el-option label="A" value="A" />
              <el-option label="B" value="B" />
              <el-option label="AB" value="AB" />
              <el-option label="O" value="O" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="联系电话">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="电子邮箱">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="联系地址">
        <el-input v-model="form.address" />
      </el-form-item>
      <el-form-item label="慢性疾病">
        <el-input v-model="form.chronicConditions" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="过敏史">
        <el-input v-model="form.allergies" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="既往病史">
        <el-input v-model="form.medicalHistory" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="用药情况">
        <el-input v-model="form.medications" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="紧急联系人">
        <el-input v-model="form.emergencyContact" />
      </el-form-item>
      <el-form-item label="紧急电话">
        <el-input v-model="form.emergencyPhone" />
      </el-form-item>
      <el-form-item label="生活习惯">
        <el-input v-model="form.lifestyleNotes" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { HealthProfile } from '@/types/resident';
import { fetchMyProfile, updateMyProfile } from '@/services/resident';

const form = reactive<Partial<HealthProfile>>({});
const saving = ref(false);

const load = async () => {
  const data = await fetchMyProfile();
  Object.assign(form, data);
};

onMounted(load);

const save = async () => {
  saving.value = true;
  try {
    await updateMyProfile(form);
    ElMessage.success('档案已保存');
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
</style>
