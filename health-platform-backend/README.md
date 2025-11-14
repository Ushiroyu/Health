# Health Platform Backend

Spring Cloud 微服务集群，为社区健康管理系统提供认证、居民档案、医生排班、预约挂号、在线问诊、健康资讯、社区活动及运营后台等能力。

## 模块一览
| 模块 | 说明 |
| --- | --- |
| common | 共享依赖：ApiResponse、BizException、AES-GCM 字段加密、JWT 工具、统一错误码。 |
| eureka-server | 服务注册发现，默认端口 8761。 |
| gateway-service | Spring Cloud Gateway，内置 JwtAuthGlobalFilter、白名单配置、全局异常输出。 |
| uth-service | 账户/角色管理、登录/注册、管理员审核、审计日志。 |
| user-service | 居民档案、健康记录曲线、管理端统计。 |
| doctor-service | 科室、医生、症状标签、排班号源管理与复杂检索。 |
| ppointment-service | 预约、取消、在线问诊、健康指导、测量提醒、处方与排班同步，含 Redis/Lua 并发控制与 RocketMQ 通知。 |
| content-service | 健康资讯 CRUD、MinIO 文件上传/预签名、资讯事件推送。 |
| ctivity-service | 社区活动发布、报名、容量校验、报名查询、活动事件推送。 |

> 根目录下的 pom.xml 聚合以上模块，同时也保留了一个空的 src/main/java/HealthPlatformBackendApplication 以便必要时运行单体模式。

## 核心功能
- **统一鉴权**：Gateway 校验 JWT 并透传 X-User-*，各服务依赖拦截器或 @RequestHeader 获取用户身份、角色、医生资质，实现细粒度权限控制。
- **居民/医生数据域**：user-service 管理健康档案与指标，doctor-service 支持按科室、关键字、症状标签、日期/时间段筛选医生，并联合排班表判断可预约状态。
- **预约 & 并发控制**：AppointmentService#book 通过 Redis LUA 脚本和幂等键实现抢号；取消预约时回补余量并写入 RocketMQ 以驱动通知。AppointmentReminderJob/MeasurementReminderJob 定时推送即将到诊或测量任务。
- **在线问诊/健康指导**：ConsultController 负责会话与消息流，GuidanceController 让医生下发指导方案、居民自行管理提醒。NotificationSender 目前提供站内信、短信、邮件扩展点。
- **健康资讯 & 文件**：content-service 与 MinIO 集成，完成类型/大小校验、对象化存储与预签名访问，前端仅需保存 object key。
- **社区活动**：活动发布、报名容量校验、报名记录查询、取消报名及活动事件广播。
- **运营能力**：各服务的 AdminAuditController 写入 AuditLog；AdminStatsController 聚合预约数据并支持 CSV 导出；`database/init.sql` 可快速初始化演示数据。

## 运行步骤
1. **准备依赖**：MySQL 8（默认库 healthdb）、Redis 7、RocketMQ 5.3、MinIO（默认桶 health-bucket）。可通过仓库根目录的 docker-compose.yml 拉起 redis/rocketmq/minio 及各服务容器。
2. **配置环境变量**（示例）：
   - SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/healthdb?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
   - SPRING_DATASOURCE_USERNAME=xxx / SPRING_DATASOURCE_PASSWORD=xxx
   - SPRING_DATA_REDIS_HOST=127.0.0.1、SPRING_DATA_REDIS_PORT=6379
   - ROCKETMQ_NAME_SERVER=localhost:9876
   - MINIO_ENDPOINT=http://localhost:9000、MINIO_ACCESS_KEY=minioadmin、MINIO_SECRET_KEY=minioadmin
   - CRYPTO_KEY=<Base64 AES-256 密钥>、SECURITY_JWT_SECRET=<JWT 签名>
3. **构建与启动**：
`powershell
# 构建所有模块
./mvnw clean install

# 依次启动（或使用 IDE）
./mvnw -pl eureka-server spring-boot:run
./mvnw -pl gateway-service -am spring-boot:run
./mvnw -pl auth-service -am spring-boot:run
./mvnw -pl user-service -am spring-boot:run
./mvnw -pl doctor-service -am spring-boot:run
./mvnw -pl appointment-service -am spring-boot:run
./mvnw -pl content-service -am spring-boot:run
./mvnw -pl activity-service -am spring-boot:run
`
> 也可以直接 docker compose up gateway auth user doctor appointment content activity，其余依赖容器会被自动拉起。

## 数据与脚本
- database/init.sql：合并 schema + data，覆盖用户/医生/预约/排班/资讯/活动/审计等实体与示例数据，可用于演示。
- scripts/：可存放初始化脚本或 CI 辅助命令（当前为空目录，可视需要补充）。

## 集成要点
- **消息队列**：
  - Topic ppointment-events：预约创建/取消事件，消费者包括通知中心；
  - Topic ctivity-events：活动发布更新事件，可用于推送或推荐；
  - Topic content-events：由 content-service 发布，ppointment-service 可消费以触发相关提醒。
- **调度任务**：所有 job 类使用 @Scheduled，默认开启；若需关闭可通过 spring.task.scheduling.enabled=false。
- **安全**：
  - gateway-service 维护 security.whitelist，默认放行 /auth/**、/actuator/**；
  - uth-service 的 SecurityConfig 仅允许认证用户访问除白名单以外的接口；
  - 业务服务普遍通过 WebConfig/拦截器校验 X-User-Role。

## 开发建议
1. **调试并发预约**：可在 Redis 中观察 slot:*、idem:* 键，或通过 AppointmentService#setSlot 临时调整余量。
2. **扩展通知**：实现 NotificationSender 接口即可向 RocketMQ 消费器注入新的通知通道。
3. **多环境配置**：建议新增 pplication-dev.yml/pplication-prod.yml，并结合 Spring Config Server 或 Nacos 进行集中管理。
4. **监控指标**：可在各服务开启 Actuator 指标端点，暴露给 Prometheus；RocketMQ 消费 lag 需重点关注。

## 常见问题
- **容器访问宿主 MySQL**：docker-compose.yml 通过 extra_hosts: host.docker.internal:host-gateway 解决宿主访问问题，若在 Linux 需确保 Docker 版本支持。
- **MinIO 桶初始化**：首次运行需登录 http://localhost:19001 创建 health-bucket，或在 content-service 中开启自动创建逻辑。
- **RocketMQ 可选**：若暂不使用消息队列，可移除相关配置，@ConditionalOnProperty 会让 Producer/Consumer 自动失效。
