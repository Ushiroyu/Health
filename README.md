# 社区健康管理系统 (Community Health Management Platform)

## 项目背景
随着居民健康意识的提升，社区医疗服务需要一套覆盖健康档案、在线问诊、预约挂号、健康资讯与社区活动的统一平台。本项目提供前后端一体的解决方案，帮助居民随时记录健康数据、与医生互动，帮助管理者掌握运营数据，提升社区健康管理的便利性与精细化水平。

## 功能总览
- **居民服务**：个人档案维护、随手记录血压/血糖/体重/心率、可视化趋势分析、提醒设置。
- **医生服务**：多条件医生检索（按科室、症状标签、排班）、在线问诊、健康指导、处方开具。
- **预约挂号**：号源排班、并发预约、取消/改约、科室/医生空闲时段查询、处方/就诊记录查询。
- **健康资讯**：文章推送、图文/附件上传（MinIO）、富文本发布、资讯后台审核。
- **社区活动**：健康讲座与体检活动发布、在线报名、名额校验、活动推送与提醒。
- **管理后台**：用户与角色管理、医生入驻审核、操作审计、预约与健康数据统计分析（CSV 导出）。

## 整体架构
`
Vue3 + Vite + Pinia + Element Plus + ECharts (health-platform-frontend)
         |
Spring Cloud Gateway (JWT 统一鉴权、路由、灰度白名单)
         |
├── auth-service        (认证/账户/审计)
├── user-service        (居民档案、健康记录、统计)
├── doctor-service      (医生/科室/排班/症状标签)
├── appointment-service (预约、在线问诊、指导、处方、提醒)
├── content-service     (健康资讯、MinIO 文件服务)
├── activity-service    (社区活动/报名/RocketMQ 推送)
├── common              (通用异常、加密、JWT)
├── eureka-server       (服务注册发现)
└── gateway-service     (边界安全)
        ↙   ↘
 MySQL · Redis · RocketMQ · MinIO
`
- docker-compose.yml 编排了 Redis、RocketMQ、MinIO 以及各微服务容器，可直接复现基础设施。
- db/schema.sql 与 db/data.sql 提供建表与示例数据，便于初始化。
- docs/release-test-checklist.md 收录了发布自查清单，可根据功能点补充测试用例。

## 推荐技术栈
| 层 | 选型 |
| --- | --- |
| 前端 | Vue 3 + TypeScript + Vite + Vue Router + Pinia + Element Plus + ECharts + Axios |
| 网关 | Spring Cloud Gateway + Spring Security + JWT |
| 微服务 | Spring Boot 3.3 · Spring Cloud 2023 · Spring Data JPA · Validation |
| 数据 & 基础设施 | MySQL · Redis · RocketMQ · MinIO |

## 技术难点与落地
- **数据安全**：共用模块内置 AES-GCM AesGcmStringConverter，结合网关 JWT 鉴权与服务端角色/科室拦截器，敏感字段（病症、联系方式等）落库即加密；CRYPTO_KEY 环境变量可热切换密钥。
- **复杂检索**：doctor-service 与 ppointment-service 大量使用 JPA Specification/子查询，支持按症状标签、日期、时间段等组合过滤；content-service、ctivity-service 同样支持分页/模糊/时间区间检索。
- **并发预约**：ppointment-service 结合 Redis + Lua 脚本、幂等键与排班缓存，保障多端同时抢号时仍能正确扣减并回滚；配套作业 AppointmentReminderJob/MeasurementReminderJob 负责即将到诊与测量任务提醒。
- **数据可视化**：前端健康趋势页基于 ECharts 动态渲染指标曲线，后台统计页消费 /appointment/admin/stats 聚合接口完成多维图表与 CSV 导出。
- **消息与解耦**：RocketMQ 主题（如 ppointment-events、ctivity-events）串联预约、活动、通知，NotificationSender 适配站内信、短信、邮件多渠道。
- **文件存储**：content-service 内置 MinIO 上传/预签名接口，前端富文本与资讯附件统一走对象存储，支持访问有效期控制。

## 仓库结构
`
.
├── health-platform-frontend/   # Vue3 前台 + 管理端
├── health-platform-backend/    # Spring Cloud 微服务聚合工程
├── db/                         # MySQL schema 与示例数据
├── docs/                       # 发布/测试文档
├── docker-compose.yml          # 一键启动依赖与服务
├── mvnw* / pom.xml             # 后端构建脚本
└── start.cmd                   # Windows 辅助脚本
`

## 快速开始
### 1. 环境准备
- Node.js 18+、npm 或 pnpm
- JDK 21、Maven 3.9+ (仓库附带 mvnw 可直接使用)
- 本地 MySQL 8、Redis 7、RocketMQ 5.3、MinIO（可用 docker compose 快速起服务）
- CRYPTO_KEY（Base64 编码 256bit）和 security.jwt.secret 等敏感配置请通过环境变量或配置中心注入。

### 2. 启动基础设施（可选）
`powershell
# 默认会将 MySQL 指向宿主机 3306，如需容器化 MySQL 请同步修改各服务 SPRING_DATASOURCE_URL
docker compose up -d redis rocketmq-namesrv rocketmq-broker minio
`

### 3. 启动后端服务
`powershell
# 1) 构建所有模块
./mvnw clean install

# 2) 启动注册中心与网关
./mvnw -pl eureka-server spring-boot:run
./mvnw -pl gateway-service -am spring-boot:run

# 3) 按需启动业务服务（示例）
./mvnw -pl auth-service -am spring-boot:run
./mvnw -pl user-service -am spring-boot:run
./mvnw -pl doctor-service -am spring-boot:run
./mvnw -pl appointment-service -am spring-boot:run
./mvnw -pl content-service -am spring-boot:run
./mvnw -pl activity-service -am spring-boot:run
`
> 提示：docker-compose 里已为各服务预置数据库、Redis、RocketMQ、MinIO 的环境变量，可参考并在本地调试时写入 pplication-*.yml 或 IDE Run Configuration。

### 4. 启动前端
`powershell
cd health-platform-frontend
npm install
npm run dev        # 默认代理 /api -> 8080 网关
npm run build      # 生成生产构建
`
生产构建可由 Nginx/静态服务器托管，并通过网关暴露 /api 即可完成前后端联调。

## 关键配置与约定
- **接口约定**：所有业务服务仅暴露给 Gateway，服务间依赖 Eureka 发现；Gateway 统一加上 X-User-Id/X-User-Role 头部，后端通过拦截器/注解鉴权。
- **环境变量**：SPRING_DATASOURCE_URL/USERNAME/PASSWORD、SPRING_DATA_REDIS_*、ROCKETMQ_NAME_SERVER、MINIO_*、CRYPTO_KEY 必须根据部署环境设置。
- **静态资源**：资讯附件均上传至 MinIO health-bucket，content-service 负责回传直链与预签名 URL；前端只保存对象名。
- **日志与审计**：每个服务都写入 AuditLog，管理员可在前端“操作审计”页查看。

## 进一步工作建议
1. **自动化测试**：结合 docs/release-test-checklist，为复杂业务（预约抢号、医生排班、RocketMQ 通知、MinIO 上传）补充集成测试。
2. **监控告警**：接入 Prometheus/Grafana 或 SkyWalking，监控 Redis/Lua 执行情形与 RocketMQ 积压。
3. **部署**：可将前后端容器化后纳入 docker compose，或上传至 K8s，注意配置外部 MySQL/Redis/RocketMQ/MinIO 访问信息。

---
前端与后端的更细使用方式分别见 health-platform-frontend/README.md 与 health-platform-backend/README.md。
