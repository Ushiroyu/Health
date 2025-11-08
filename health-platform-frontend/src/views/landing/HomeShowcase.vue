<template>
  <div class="landing">
    <section class="landing__hero" id="hero">
      <div class="landing__hero-copy">
        <span class="landing__badge">社区健康管理系统</span>
        <h1>掌握社区健康脉动，从这里开始</h1>
        <p>
          聚合居民档案、医生服务、预约分诊、资讯推送与社区活动于一体，
          为社区提供安全、高效、可持续的健康管理体验。
        </p>
        <div class="landing__actions">
          <el-button type="primary" size="large" @click="goLogin">
            立即登录
            <el-icon class="landing__btn-icon"><ArrowRight /></el-icon>
          </el-button>
          <el-button size="large" text @click="goDashboard">
            体验工作台
          </el-button>
        </div>
        <div class="landing__meta">
          <div>
            <strong>5</strong>
            <span>核心业务模块</span>
          </div>
          <div>
            <strong>6+</strong>
            <span>可视化分析视图</span>
          </div>
          <div>
            <strong>12+</strong>
            <span>关键技术能力</span>
          </div>
        </div>
      </div>
      <div class="landing__hero-preview" aria-hidden="true">
        <div class="preview-card">
          <header>
            <span>今日健康看板</span>
            <el-tag size="small" round type="success">实时</el-tag>
          </header>
          <div class="preview-card__stats">
            <div>
              <span class="label">新增健康记录</span>
              <span class="value">128</span>
            </div>
            <div>
              <span class="label">预约成功率</span>
              <span class="value value--accent">96%</span>
            </div>
            <div>
              <span class="label">活动报名</span>
              <span class="value">342</span>
            </div>
          </div>
          <div class="preview-card__chart">
            <span>一周健康提示发送量</span>
            <ul>
              <li v-for="(num, idx) in [42, 55, 71, 64, 88, 93, 76]" :key="idx">
                <span>{{ weekLabels[idx] }}</span>
                <div class="bar">
                  <div class="bar__inner" :style="{ width: `${num}%` }"></div>
                </div>
              </li>
            </ul>
          </div>
          <footer>
            <el-icon><Bell /></el-icon>
            个性化健康提醒自动匹配风险人群
          </footer>
        </div>
      </div>
    </section>

    <section class="landing__features" id="features">
      <div class="section-header">
        <span>功能亮点</span>
        <h2>满足社区健康服务的全流程需求</h2>
        <p>
          面向居民、医生、管理者三类角色提供无缝协作体验，支持复杂查询、预约并发、健康趋势分析等场景。
        </p>
      </div>
      <div class="features-grid">
        <article
          v-for="feature in features"
          :key="feature.title"
          class="feature-card"
        >
          <div class="feature-card__icon">
            <el-icon>
              <component :is="feature.icon" />
            </el-icon>
          </div>
          <h3>{{ feature.title }}</h3>
          <p>{{ feature.description }}</p>
        </article>
      </div>
    </section>

    <section class="landing__modules">
      <div class="section-header">
        <span>应用模块</span>
        <h2>角色驱动的业务闭环</h2>
      </div>
      <div class="modules-grid">
        <article
          v-for="module in modules"
          :key="module.title"
          class="module-card"
        >
          <header>
            <h3>{{ module.title }}</h3>
            <el-tag size="small" effect="dark">{{ module.tag }}</el-tag>
          </header>
          <ul>
            <li v-for="point in module.points" :key="point">
              <el-icon><ArrowRight /></el-icon>
              <span>{{ point }}</span>
            </li>
          </ul>
        </article>
      </div>
    </section>

    <section class="landing__tech">
      <div class="tech-card">
        <h2>成熟技术栈，保障业务稳定与数据安全</h2>
        <div class="tech-card__grid">
          <div>
            <h3>前端</h3>
            <p>Vue 3 · Vue Router · Pinia · ECharts · Element Plus</p>
          </div>
          <div>
            <h3>后端与基础设施</h3>
            <p>Spring Boot · Spring Cloud · Spring Security · MySQL · Redis · RocketMQ · MinIO</p>
          </div>
          <div>
            <h3>数据与安全</h3>
            <p>敏感信息加密、分层权限控制、预约并发限流、健康趋势可视化</p>
          </div>
        </div>
        <div class="tech-card__actions">
          <el-button type="primary" @click="goLogin">
            开始使用
            <el-icon class="landing__btn-icon"><ArrowRight /></el-icon>
          </el-button>
          <el-button @click="goDashboard" text>查看演示</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import {
  ArrowRight,
  Bell,
  Calendar,
  Document,
  Flag,
  Histogram,
  UserFilled
} from '@element-plus/icons-vue';

const router = useRouter();

const weekLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];

const features = [
  {
    title: '居民健康档案',
    description: '个人资料、健康数据、危险因素一站式管理，生成长期趋势。',
    icon: UserFilled
  },
  {
    title: '个性化健康提醒',
    description: '多指标实时追踪，联合 AI 分析推送用药、复诊与随访提醒。',
    icon: Bell
  },
  {
    title: '多维度预约分诊',
    description: '支持按症状、科室、医生综合筛选，秒级预约与排班管理。',
    icon: Calendar
  },
  {
    title: '健康知识中心',
    description: '权威健康资讯、疾病预防与生活方式内容持续更新。',
    icon: Document
  },
  {
    title: '社区活动运营',
    description: '健康讲座、体检活动、志愿服务一键发布与报名统计。',
    icon: Flag
  },
  {
    title: '管理驾驶舱',
    description: '用户、医生、健康数据与预约情况可视化分析，实时掌控运营。',
    icon: Histogram
  }
];

const modules = [
  {
    title: '居民服务',
    tag: 'RESIDENT',
    points: ['个人档案与健康数据', '提醒计划与目标监控', '活动报名与资讯订阅']
  },
  {
    title: '医生工作台',
    tag: 'DOCTOR',
    points: ['医生排班与问诊管理', '诊疗记录与处方审核', '患者随访与健康指导']
  },
  {
    title: '预约挂号',
    tag: 'APPOINTMENT',
    points: ['多条件智能分诊', '并发预约与排队管理', '短信/站内消息双通道提醒']
  },
  {
    title: '运维管理',
    tag: 'ADMIN',
    points: ['用户与医生权限管理', '健康数据可视化', '平台操作审计与风控']
  }
];

const goLogin = () => {
  router.push({ name: 'login' });
};

const goDashboard = () => {
  router.push({ name: 'dashboard' });
};
</script>

<style scoped>
.landing {
  min-height: 100vh;
  background: var(--bg-gradient);
  color: var(--app-text);
}

.landing__hero {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 48px;
  align-items: center;
  padding: 96px clamp(24px, 6vw, 120px) 72px;
}

.landing__hero-copy h1 {
  margin: 18px 0;
  font-size: clamp(36px, 5vw, 56px);
  line-height: 1.1;
  font-weight: 800;
  letter-spacing: 1px;
}

.landing__hero-copy p {
  font-size: 16px;
  line-height: 1.65;
  max-width: 520px;
  color: var(--app-muted);
}

.landing__badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 999px;
  background: rgba(79, 70, 229, 0.14);
  color: var(--app-primary);
  font-size: 13px;
  letter-spacing: 0.5px;
}

.landing__actions {
  margin-top: 28px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.landing__btn-icon {
  margin-left: 6px;
}

.landing__meta {
  margin-top: 36px;
  display: flex;
  gap: 32px;
}

.landing__meta div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.landing__meta strong {
  font-size: 32px;
  font-weight: 700;
  color: var(--app-primary);
}

.landing__meta span {
  font-size: 13px;
  color: var(--app-muted);
}

.landing__hero-preview {
  position: relative;
}

.preview-card {
  border-radius: 28px;
  padding: 28px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 40px 80px rgba(15, 23, 42, 0.16);
  backdrop-filter: blur(18px);
}

.preview-card header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-weight: 600;
}

.preview-card__stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin-bottom: 28px;
}

.preview-card__stats .label {
  font-size: 12px;
  color: var(--app-muted);
}

.preview-card__stats .value {
  margin-top: 4px;
  font-size: 22px;
  font-weight: 700;
  color: var(--app-text);
}

.preview-card__stats .value--accent {
  color: var(--app-primary);
}

.preview-card__chart span {
  font-size: 13px;
  color: var(--app-muted);
}

.preview-card__chart ul {
  margin: 12px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.preview-card__chart li {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}

.preview-card__chart .bar {
  flex: 1;
  height: 6px;
  border-radius: 999px;
  background: rgba(79, 70, 229, 0.12);
  overflow: hidden;
}

.preview-card__chart .bar__inner {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--app-primary) 0%, var(--app-accent) 100%);
}

.preview-card footer {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--app-muted);
}

.section-header {
  text-align: center;
  max-width: 720px;
  margin: 0 auto 36px;
}

.section-header span {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 999px;
  background: rgba(79, 70, 229, 0.14);
  color: var(--app-primary);
  font-size: 12px;
  letter-spacing: 0.3px;
}

.section-header h2 {
  margin: 16px 0 12px;
  font-size: clamp(28px, 4vw, 36px);
}

.section-header p {
  margin: 0;
  color: var(--app-muted);
  line-height: 1.65;
}

.landing__features {
  padding: 40px clamp(24px, 6vw, 120px) 32px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 24px;
}

.feature-card {
  padding: 24px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 24px 45px rgba(15, 23, 42, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  height: 100%;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 32px 60px rgba(15, 23, 42, 0.12);
}

.feature-card__icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: rgba(79, 70, 229, 0.15);
  color: var(--app-primary);
  display: grid;
  place-items: center;
  margin-bottom: 14px;
  font-size: 20px;
}

.feature-card h3 {
  margin-bottom: 8px;
  font-size: 18px;
  font-weight: 600;
}

.feature-card p {
  margin: 0;
  font-size: 14px;
  color: var(--app-muted);
  line-height: 1.65;
}

.landing__modules {
  padding: 56px clamp(24px, 6vw, 120px);
  background: rgba(255, 255, 255, 0.76);
  backdrop-filter: blur(18px);
}

.modules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
}

.module-card {
  border-radius: 24px;
  padding: 24px;
  background: rgba(248, 250, 252, 0.96);
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 22px 42px rgba(15, 23, 42, 0.08);
  height: 100%;
}

.module-card header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.module-card ul {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.module-card li {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  color: var(--app-muted);
  font-size: 14px;
  line-height: 1.6;
}

.module-card li .el-icon {
  color: var(--app-primary);
  font-size: 16px;
  margin-top: 2px;
}

.landing__tech {
  padding: 72px clamp(24px, 6vw, 120px);
}

.tech-card {
  border-radius: 28px;
  padding: clamp(28px, 5vw, 48px);
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.95), rgba(30, 64, 175, 0.85));
  color: #f8fafc;
  box-shadow: 0 50px 80px rgba(15, 23, 42, 0.25);
}

.tech-card h2 {
  margin: 0 0 24px;
  font-size: clamp(28px, 4vw, 36px);
}

.tech-card__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 24px;
}

.tech-card__grid h3 {
  margin-bottom: 8px;
  font-size: 18px;
  color: #bfdbfe;
}

.tech-card__grid p {
  margin: 0;
  color: rgba(226, 232, 240, 0.85);
  line-height: 1.6;
}

.tech-card__actions {
  margin-top: 28px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

@media (max-width: 1024px) {
  .landing__hero {
    padding-top: 72px;
    padding-bottom: 56px;
  }
}

@media (max-width: 768px) {
  .landing__hero {
    padding-top: 60px;
    padding-bottom: 40px;
  }

  .landing__meta {
    gap: 20px;
  }

  .landing__meta strong {
    font-size: 28px;
  }
}

@media (max-width: 640px) {
  .landing__meta {
    flex-direction: column;
  }
}
</style>
