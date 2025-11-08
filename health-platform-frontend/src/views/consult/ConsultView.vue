<template>
  <div class="consult">
    <el-row :gutter="16">
      <el-col :md="8" :sm="24">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>问诊会话</span>
              <el-space>
                <el-button type="primary" size="small" @click="openCreateDialog">发起问诊</el-button>
                <el-button size="small" @click="loadSessions">刷新</el-button>
              </el-space>
            </div>
          </template>
          <el-form label-position="top" class="filter-form">
            <el-form-item label="患者用户 ID">
              <el-input
                v-model="sessionQuery.userId"
                placeholder="默认使用当前登录用户"
                :disabled="!canFilterByUser"
              />
            </el-form-item>
            <el-form-item label="医生编号">
              <el-input
                v-model="sessionQuery.doctorId"
                placeholder="仅医生/管理员可指定"
                :disabled="!canFilterByDoctor"
              />
            </el-form-item>
            <el-form-item label="分页">
              <el-pagination
                small
                layout="prev, pager, next"
                :current-page="sessionQuery.page + 1"
                :page-size="sessionQuery.size"
                :total="sessions?.totalElements ?? 0"
                @current-change="onSessionPageChange"
              />
            </el-form-item>
          </el-form>
          <el-table
            :data="sessions?.content ?? []"
            style="width: 100%"
            @row-click="selectSession"
            highlight-current-row
            :height="360"
          >
            <el-table-column prop="sessionId" label="会话编号" width="110" />
            <el-table-column prop="doctorId" label="医生" width="90" />
            <el-table-column prop="status" label="状态" width="90" />
            <el-table-column prop="chiefComplaint" label="主诉" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :md="16" :sm="24">
        <el-card shadow="never" class="messages-card">
          <template #header>
            <div class="card-header">
              <span v-if="selectedSession">
                会话 #{{ selectedSession.sessionId }} · 状态 {{ selectedSession.status }}
              </span>
              <span v-else>请选择会话</span>
              <el-space v-if="selectedSession && isDoctorRole">
                <el-button
                  type="success"
                  size="small"
                  @click="handleAccept(selectedSession.sessionId)"
                >
                  接诊
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleClose(selectedSession.sessionId)"
                >
                  结束
                </el-button>
              </el-space>
            </div>
          </template>

          <div class="messages" v-loading="messageLoading">
            <el-empty description="暂无消息" v-if="messages.length === 0 && !messageLoading" />
            <el-timeline v-else>
              <el-timeline-item
                v-for="item in messages"
                :key="item.msgId"
                :timestamp="formatTime(item.createdAt)"
                :type="item.senderType === 'DOCTOR' ? 'primary' : 'success'"
              >
                <div class="message-item">
                  <strong>{{ item.senderType === 'DOCTOR' ? '医生' : '居民' }}</strong>
                  <p>{{ item.content }}</p>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>

          <el-form v-if="selectedSession" :model="messageForm" class="message-form">
            <el-form-item>
              <el-input
                v-model="messageForm.content"
                type="textarea"
                :rows="3"
                placeholder="输入要发送的消息"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="sending" @click="sendMessage">发送</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="createDialog" title="发起在线问诊" width="420px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="医生编号">
          <el-input v-model="createForm.doctorId" placeholder="请输入目标医生编号" />
        </el-form-item>
        <el-form-item label="主诉内容">
          <el-input
            v-model="createForm.chiefComplaint"
            type="textarea"
            :rows="3"
            placeholder="请描述您的主要症状"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createSession">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import {
  acceptConsultSession,
  closeConsultSession,
  createConsultSession,
  fetchConsultMessages,
  fetchConsultSessions,
  sendConsultMessage
} from '@/services/consult';
import type { ConsultMessage, ConsultSession } from '@/types/consult';
import type { PageResult } from '@/types/common';
import { formatDate } from '@/utils/format';

const auth = useAuthStore();
const role = computed(() => auth.user?.role ?? 'RESIDENT');
const isDoctorRole = computed(() => role.value === 'DOCTOR' || role.value === 'ADMIN');
const canFilterByUser = computed(() => isDoctorRole.value || role.value === 'ADMIN');
const canFilterByDoctor = computed(() => isDoctorRole.value || role.value === 'ADMIN');

const sessions = ref<PageResult<ConsultSession>>();
const sessionLoading = ref(false);
const sessionQuery = reactive({
  page: 0,
  size: 10,
  userId: '',
  doctorId: ''
});

const selectedSession = ref<ConsultSession | null>(null);
const messages = ref<ConsultMessage[]>([]);
const messageLoading = ref(false);
const sending = ref(false);
const messageForm = reactive({
  content: ''
});

const createDialog = ref(false);
const creating = ref(false);
const createForm = reactive({
  doctorId: '',
  chiefComplaint: ''
});

const authUserId = computed(() => auth.user?.userId);

const buildSessionParams = () => {
  const params: Record<string, number> = {
    page: sessionQuery.page,
    size: sessionQuery.size
  };
  const doctorId = Number(sessionQuery.doctorId);
  const userId = Number(sessionQuery.userId || authUserId.value);
  if (!Number.isNaN(doctorId) && sessionQuery.doctorId) {
    params.doctorId = doctorId;
  }
  if (!Number.isNaN(userId)) {
    params.userId = userId;
  }
  return params;
};

const loadSessions = async () => {
  sessionLoading.value = true;
  try {
    const data = await fetchConsultSessions(buildSessionParams());
    sessions.value = data;
    if (data.content.length > 0) {
      const first = data.content.find((item) => item.sessionId === selectedSession.value?.sessionId);
      selectSession(first ?? data.content[0]);
    } else {
      selectedSession.value = null;
      messages.value = [];
    }
  } finally {
    sessionLoading.value = false;
  }
};

const onSessionPageChange = (page: number) => {
  sessionQuery.page = page - 1;
  loadSessions();
};

const selectSession = (session: ConsultSession) => {
  selectedSession.value = session;
  loadMessages(session.sessionId);
};

const loadMessages = async (sessionId: number) => {
  messageLoading.value = true;
  try {
    const data = await fetchConsultMessages({ sessionId, page: 0, size: 200 });
    messages.value = data.content;
  } finally {
    messageLoading.value = false;
  }
};

const sendMessage = async () => {
  if (!selectedSession.value) {
    ElMessage.warning('请选择会话');
    return;
  }
  if (!messageForm.content) {
    ElMessage.warning('请输入消息内容');
    return;
  }
  sending.value = true;
  try {
    await sendConsultMessage({
      sessionId: selectedSession.value.sessionId,
      content: messageForm.content
    });
    messageForm.content = '';
    await loadMessages(selectedSession.value.sessionId);
  } finally {
    sending.value = false;
  }
};

const handleAccept = async (sessionId: number) => {
  await acceptConsultSession(sessionId);
  ElMessage.success('已接诊');
  await loadSessions();
};

const handleClose = async (sessionId: number) => {
  await closeConsultSession(sessionId);
  ElMessage.success('会话已结束');
  await loadSessions();
};

const openCreateDialog = () => {
  createForm.doctorId = '';
  createForm.chiefComplaint = '';
  createDialog.value = true;
};

const createSession = async () => {
  const doctorId = Number(createForm.doctorId);
  if (Number.isNaN(doctorId) || doctorId <= 0) {
    ElMessage.warning('请输入有效的医生编号');
    return;
  }
  creating.value = true;
  try {
    const sessionId = await createConsultSession({
      doctorId,
      chiefComplaint: createForm.chiefComplaint || undefined
    });
    ElMessage.success(`问诊已创建，编号 ${sessionId}`);
    createDialog.value = false;
    sessionQuery.page = 0;
    await loadSessions();
    const newly = sessions.value?.content.find((item) => item.sessionId === sessionId);
    if (newly) {
      selectSession(newly);
    }
  } finally {
    creating.value = false;
  }
};

const formatTime = (value?: string) => formatDate(value, 'YYYY-MM-DD HH:mm');

onMounted(() => {
  if (!sessionQuery.userId && authUserId.value) {
    sessionQuery.userId = String(authUserId.value);
  }
  loadSessions();
});
</script>

<style scoped>
.consult {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.filter-form {
  margin-bottom: 12px;
}

.messages-card {
  height: 100%;
}

.messages {
  min-height: 280px;
  max-height: 420px;
  overflow-y: auto;
  padding-right: 8px;
}

.message-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-form {
  margin-top: 16px;
}
</style>
