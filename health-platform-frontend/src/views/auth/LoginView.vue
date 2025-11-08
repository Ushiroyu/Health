<template>
  <main class="login" aria-labelledby="login-title">
    <section class="login__hero" aria-hidden="true">
      <span class="login__badge">社区健康守护 · 24/7</span>
      <h1 id="login-title">
        欢迎回来
        <br />
        社区健康指挥中心
      </h1>
      <p>
        一体化协同居民、医生与运营团队。实时跟踪健康趋势，主动推送关怀，让社区的每一次脉搏都清晰可见。
      </p>
      <ul class="login__highlights">
        <li>
          <el-icon><TrendCharts /></el-icon>
          健康数据洞察
        </li>
        <li>
          <el-icon><Bell /></el-icon>
          个性化健康提醒
        </li>
        <li>
          <el-icon><Calendar /></el-icon>
          一键预约与分诊
        </li>
      </ul>
      <div class="login__stats">
        <div>
          <strong>12,680+</strong>
          <span>服务居民</span>
        </div>
        <div>
          <strong>98%</strong>
          <span>服务满意度</span>
        </div>
        <div>
          <strong>320</strong>
          <span>合作医生</span>
        </div>
      </div>
    </section>

    <section class="login__card">
      <header class="login__card-header">
        <span class="login__card-badge">安全登录</span>
        <h2>登录您的账号</h2>
        <p>使用平台账号，解锁完整的社区健康管理体验。</p>
      </header>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="login__form"
        autocomplete="on"
        @submit.prevent="submit"
      >
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入账号"
            autocomplete="username"
            clearable
          >
            <template #prepend>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            placeholder="请输入密码"
            type="password"
            show-password
            autocomplete="current-password"
          >
            <template #prepend>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <div class="login__actions">
          <el-button
            type="primary"
            size="large"
            :loading="auth.loading"
            class="login__submit"
            @click="submit"
          >
            登录
          </el-button>
        </div>
      </el-form>
      <footer class="login__tips">
        <span>首次使用？</span>
        <span class="login__link" @click="goRegister">立即注册居民账号</span>
        <span class="login__link login__link--home" @click="goHome">返回首页了解功能</span>
      </footer>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { Bell, Calendar, Lock, TrendCharts, User } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();
const formRef = ref<FormInstance>();

const form = reactive({
  username: '',
  password: ''
});

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const submit = () => {
  if (!formRef.value) return;
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    await auth.login(form.username, form.password);
    const redirect = route.query.redirect as string | undefined;
    router.push(redirect ?? { name: 'dashboard' });
  });
};

const goHome = () => {
  router.push({ name: 'home' });
};

const goRegister = () => {
  const redirect = route.query.redirect as string | undefined;
  router.push({
    name: 'register',
    ...(redirect ? { query: { redirect } } : {})
  });
};
</script>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr minmax(360px, 440px);
  gap: 48px;
  align-items: stretch;
  padding: 56px 80px 56px 64px;
  background: var(--bg-gradient);
  color: var(--app-text);
}

.login__hero {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px 0;
  max-width: 520px;
}

.login__badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(56, 189, 248, 0.24);
  font-size: 12px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  color: rgba(15, 23, 42, 0.8);
}

.login__hero h1 {
  margin: 0;
  font-size: 46px;
  line-height: 1.12;
  font-weight: 800;
  letter-spacing: 0.8px;
}

.login__hero p {
  max-width: 460px;
  margin: 0;
  font-size: 16px;
  line-height: 1.7;
  color: #0f172a;
}

.login__highlights {
  list-style: none;
  display: flex;
  gap: 18px;
  padding: 0;
  margin: 0;
}

.login__highlights li {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.95) 0%, rgba(20, 184, 166, 0.85) 100%);
  font-size: 13px;
  color: #ffffff;
  box-shadow: 0 16px 32px rgba(79, 70, 229, 0.25);
  font-weight: 600;
}

.login__highlights li :deep(.el-icon) {
  font-size: 16px;
  color: #ffffff;
}

.login__stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 140px));
  gap: 18px;
  margin-top: 12px;
}

.login__stats div {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px;
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.login__stats strong {
  font-size: 22px;
  font-weight: 700;
}

.login__stats span {
  font-size: 12px;
  color: rgba(226, 232, 240, 0.8);
}

.login__card {
  position: relative;
  padding: 42px 40px;
  border-radius: 28px;
  background: var(--app-surface);
  box-shadow: var(--card-shadow);
  color: var(--app-text);
  display: flex;
  flex-direction: column;
  gap: 28px;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  transition: background var(--transition-base), border-color var(--transition-base),
    box-shadow var(--transition-base);
}

.login__card-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.login__card-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(79, 70, 229, 0.12);
  font-size: 12px;
  color: var(--app-primary);
  width: fit-content;
}

.login__card h2 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
}

.login__card p {
  margin: 0;
  color: var(--app-muted);
  font-size: 14px;
}

.login__form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.login__actions {
  margin-top: 18px;
}

.login__submit {
  width: 100%;
  height: 46px;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.4px;
  background: linear-gradient(90deg, var(--app-primary) 0%, var(--app-accent) 100%);
  border: none;
  box-shadow: 0 16px 32px rgba(79, 70, 229, 0.25);
}

.login__submit:hover {
  filter: brightness(1.05);
}

.login__tips {
  display: flex;
  gap: 8px;
  font-size: 13px;
  color: var(--app-muted);
}

.login__link {
  color: var(--app-primary);
  cursor: pointer;
}

.login__link--home {
  margin-left: auto;
}

@media (max-width: 1080px) {
  .login {
    grid-template-columns: 1fr;
    padding: 40px 0;
  }

  .login__hero {
    display: none;
  }

  .login__card {
    margin: 0 auto;
    width: min(420px, 92%);
  }
}
</style>
