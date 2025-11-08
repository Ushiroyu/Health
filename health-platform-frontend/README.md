# Health Platform Frontend

社区健康管理系统的前端子项目，基于 **Vue 3 + TypeScript + Vite** 实现居民端与管理端一体化界面，复用了 Pinia 状态和统一的 API 客户端来对接后端网关。

## 功能模块
- **身份认证**：登录/注册页使用 Pinia useAuthStore 缓存 JWT 与用户角色，路由守卫根据 	o.meta.roles 做访问控制，并自动重定向到工作台。
- **仪表盘**：iews/dashboard/OverviewView.vue 展示身份、账号、系统状态以及快捷入口，动态读取 Pinia 用户信息。
- **居民专区**：
  - 档案（esident/ProfileView）用于维护基础信息和慢病标签；
  - 健康记录（esident/HealthRecordsView）支持按血压/血糖/体重/心率筛选、分页、对话框新增；
  - 健康趋势（esident/HealthTrendsView）通过 ECharts 渲染 30 天指标折线；
  - 提醒（esident/RemindersView）创建/启用/停用测量提醒。
- **医生与问诊**：医生检索页结合科室、症状标签、排班时段筛选；consult/ConsultView 管理在线问诊会话和消息流。
- **预约挂号**：
  - ppointment/BookingView 支持按科室→医生→日期三级联动、实时读取剩余号源；
  - MyAppointmentsView、ScheduleAdminView 和 PrescriptionListView 覆盖居民查看、医生排班、处方导出等场景。
- **健康资讯与社区活动**：文章列表/详情、后台内容管理、社区活动报名/取消/管理。
- **健康指导与后台管理**：
  - guidance/GuidancePlanView 展示医生下发的健康计划；
  - dmin/AdminUserView、doctor/DoctorAdminView、dmin/AdminAuditView、dmin/AdminStatsView 分别负责用户管理、医生资质、操作审计以及预约统计。

## 技术栈
| 类别 | 说明 |
| --- | --- |
| 框架 | Vue 3 (Composition API) + TypeScript |
| 构建 | Vite 5 + 
pm run dev/build/preview |
| 组件 | Element Plus、@element-plus/icons-vue、@vueuse/core |
| 状态 | Pinia（src/stores）+ 本地缓存 TOKEN/USER |
| 路由 | Vue Router 4，src/router/routes.ts 列出所有页面及角色元信息 |
| 数据 | Axios 封装（src/services/api.ts）+ 领域服务（resident/appointment/...） |
| 可视化 | ECharts（健康趋势、统计面板） |

## 目录速览
`
src/
├── assets/            # 全局样式、图标
├── components/        # 通用组件（图表卡片、上传等）
├── layouts/           # 登录前/登录后布局，含顶栏与侧边菜单
├── router/            # 路由定义与守卫
├── stores/            # Pinia store（auth、app、settings 等）
├── services/          # Axios 实例与按领域拆分的 API 封装
├── views/             # 功能页面（resident/doctor/appointment/...）
├── types/             # TS 类型定义（Appointment、Resident、Common 等）
└── utils/             # 日期、人性化展示、表单校验等工具
`

## 与后端的约定
- 所有请求默认走 /api 前缀（.env.development 设置了 VITE_API_BASE=/api），由本地代理转发到 8080 网关。
- services/api.ts 在请求拦截器中自动附加 Authorization: Bearer <token>，并在响应为非 code=0 时弹出 Element Plus 错误提示。
- 登录/注册接口位于 /auth/**，被 skipAuthHeader 白名单处理；其余接口依赖 Gateway 注入的 X-User-* 头部。

## 本地开发
`powershell
# 安装依赖
npm install

# 启动开发服务器，默认监听 http://localhost:5173
npm run dev

# 类型检查 + 打包
npm run build

# 预览生产构建
npm run preview
`
> 推荐 Node.js 18+。如需自定义后端地址，新增 .env.local 并覆盖 VITE_API_BASE。

## 代码风格与辅助说明
- 全面使用 <script setup lang="ts">，减少样板代码；
- Element Plus 组件结合 :aria-* 属性与 sr-only class，兼顾可访问性；
- src/services 中按业务域拆分 API，统一返回 PageResult<T> 或领域模型，便于复用；
- 某些页面（如 AdminStatsView）直接消费后端 CSV 导出能力，可在浏览器中下载报表；
- 如需新增菜单，请同步更新 src/router/routes.ts 与侧边栏配置，确保 meta.roles 与后端角色常量一致。

## 常见问题
1. **401 立即退出**：当网关返回 401 时，响应拦截器会调用 uth.logout() 并重定向到登录页。
2. **ECharts 尺寸问题**：HealthTrendsView 已监听 esize，如自定义图表请在 onUnmounted 中销毁实例。
3. **多环境部署**：生产环境可将编译结果托管到任何静态服务器，并通过反向代理转发 /api 到 Spring Cloud Gateway。
