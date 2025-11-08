<template>
  <main class="register" aria-labelledby="register-title">
    <section class="register__intro" aria-hidden="true">
      <span class="register__badge">社区居民自助注册</span>
      <h1 id="register-title">几步完成账户创建</h1>
      <p>
        使用手机号或常用账号作为登录名，设置至少 6 位密码。注册完成后我们会自动为您登录，立即体验健康数据记录、预约挂号与社区资讯等服务。
      </p>
      <ul class="register__steps">
        <li>
          <strong>01</strong>
          填写基础信息
        </li>
        <li>
          <strong>02</strong>
          设置安全密码
        </li>
        <li>
          <strong>03</strong>
          自动登录开始使用
        </li>
      </ul>
    </section>

    <el-card class="register__card" shadow="never">
      <template #header>
        <div class="register__card-header">
          <span class="register__card-badge">欢迎加入社区健康平台</span>
          <h2>创建居民账号</h2>
          <p>注册后即可自助管理健康档案、预约医生与报名社区活动。</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="register__form"
        @submit.prevent="submit"
      >
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入 4-32 位账号，可使用手机号或邮箱前缀"
            autocomplete="username"
            clearable
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            autocomplete="new-password"
            placeholder="请输入至少 6 位密码，建议同时包含字母与数字"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            autocomplete="new-password"
            placeholder="请再次输入密码"
          />
        </el-form-item>
        <el-form-item prop="agree">
          <el-checkbox v-model="form.agree">
            已阅读并同意
            <a class="register__link" href="#" @click.prevent>《用户服务协议》</a>
            与
            <a class="register__link" href="#" @click.prevent>《隐私保护政策》</a>
          </el-checkbox>
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          class="register__submit"
          :loading="registering"
          @click="submit"
        >
          立即注册并登录
        </el-button>
        <p class="register__footer">
          已有账号？
          <button type="button" class="register__link register__link--button" @click="goLogin">
            返回登录
          </button>
        </p>
      </el-form>
    </el-card>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { registerApi } from '@/services/auth';

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();
const formRef = ref<FormInstance>();
const registering = ref(false);

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  agree: true
});

const validateConfirm = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请再次输入密码'));
    return;
  }
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'));
    return;
  }
  callback();
};

const validateAgreement = (_rule: unknown, value: boolean, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请阅读并同意相关协议后继续'));
    return;
  }
  callback();
};

const rules: FormRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, max: 32, message: '账号长度需为 4-32 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度需不少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirm, trigger: 'blur' }],
  agree: [{ validator: validateAgreement, trigger: 'change' }]
};

watch(
  () => form.password,
  () => {
    if (form.confirmPassword) {
      formRef.value?.validateField('confirmPassword');
    }
  }
);

const submit = () => {
  if (!formRef.value) return;
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    const trimmedUsername = form.username.trim();
    registering.value = true;
    try {
      await registerApi({
        username: trimmedUsername,
        password: form.password
      });
      ElMessage.success('注册成功，正在为您登录');
      await auth.login(trimmedUsername, form.password);
      const redirect = route.query.redirect as string | undefined;
      router.push(redirect ?? { name: 'dashboard' });
    } finally {
      registering.value = false;
    }
  });
};

const goLogin = () => {
  const redirect = route.query.redirect as string | undefined;
  router.push({
    name: 'login',
    ...(redirect ? { query: { redirect } } : {})
  });
};
</script>

<style scoped>
.register {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr minmax(360px, 460px);
  gap: 48px;
  align-items: stretch;
  padding: 60px 88px 60px 72px;
  background: var(--bg-gradient);
}

.register__intro {
  padding: 32px 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
  color: var(--app-text);
  max-width: 520px;
}

.register__badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(56, 189, 248, 0.24);
  color: rgba(15, 23, 42, 0.8);
  font-size: 12px;
  letter-spacing: 0.6px;
}

.register__intro h1 {
  margin: 0;
  font-size: 36px;
  font-weight: 800;
  line-height: 1.2;
}

.register__intro p {
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
  color: var(--app-muted);
  max-width: 520px;
}

.register__steps {
  list-style: none;
  display: flex;
  gap: 16px;
  padding: 0;
  margin: 12px 0 0;
}

.register__steps li {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding: 18px 20px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.92) 0%, rgba(20, 184, 166, 0.85) 100%);
  color: #fff;
  font-weight: 600;
  min-width: 140px;
  box-shadow: 0 18px 32px rgba(79, 70, 229, 0.28);
}

.register__steps strong {
  font-size: 20px;
}

.register__card {
  border-radius: 28px;
  padding: 32px 36px 40px;
  background: var(--app-surface);
  box-shadow: var(--card-shadow);
  border: 1px solid rgba(255, 255, 255, 0.28);
  backdrop-filter: blur(12px);
}

.register__card-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.register__card-badge {
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

.register__card h2 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
}

.register__card p {
  margin: 0;
  font-size: 14px;
  color: var(--app-muted);
}

.register__form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.register__submit {
  width: 100%;
  height: 46px;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.4px;
  background: linear-gradient(90deg, var(--app-primary) 0%, var(--app-accent) 100%);
  border: none;
  box-shadow: 0 18px 38px rgba(79, 70, 229, 0.25);
}

.register__footer {
  margin: 18px 0 0;
  font-size: 13px;
  color: var(--app-muted);
  text-align: center;
}

.register__link {
  color: var(--app-primary);
  text-decoration: none;
}

.register__link--button {
  background: transparent;
  border: none;
  padding: 0;
  cursor: pointer;
  font: inherit;
}

@media (max-width: 1080px) {
  .register {
    grid-template-columns: 1fr;
    padding: 40px 24px;
  }

  .register__intro {
    display: none;
  }

  .register__card {
    width: min(460px, 100%);
    margin: 0 auto;
  }
}
</style>
