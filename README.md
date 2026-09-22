# study-project-Jwt

study-project-Jwt 是一个前后端分离的学习平台示例项目，当前包含用户注册、邮箱验证码、登录、JWT 鉴权、退出登录和密码重置等基础功能。

## 功能概览

- 用户名/邮箱登录
- 邮箱验证码注册
- 邮箱验证码重置密码
- Spring Security + JWT 无状态认证
- Redis 保存验证码、JWT 黑名单和限流状态
- RabbitMQ 异步投递邮件
- Vue 3 + Element Plus 前端页面
- MySQL 持久化用户账号

## 技术栈

### 前端

- Vue 3
- Vite
- Vue Router
- Element Plus
- Axios

### 后端

- Java 17
- Spring Boot 4.0.7
- Spring Security
- Spring Web MVC
- MyBatis-Plus
- MySQL
- Redis
- RabbitMQ
- Java Mail
- JWT（java-jwt）

## 项目结构

```text
RayVue/
├─ RayVue-backend/          # Spring Boot 后端
│  ├─ src/main/java/org/example/
│  │  ├─ config/            # Spring Security、RabbitMQ、Web 配置
│  │  ├─ controller/        # HTTP 接口
│  │  ├─ entity/            # 实体、请求参数和响应对象
│  │  ├─ filter/            # CORS、JWT、请求限流过滤器
│  │  ├─ listener/          # RabbitMQ 邮件消费者
│  │  ├─ mapper/            # MyBatis-Plus Mapper
│  │  ├─ service/           # 业务逻辑
│  │  └─ utils/             # JWT、限流等工具类
│  └─ src/main/resources/
│     └─ application.yml    # 后端配置
└─ rayvue-frontend/         # Vue 3 前端
   ├─ src/net/              # Axios 请求和 token 管理
   ├─ src/router/           # 路由和登录状态守卫
   └─ src/views/            # 登录、注册、重置密码和首页
```

## 环境要求

启动完整功能前，请准备：

- JDK 17+
- Maven 3.9+（或使用 IDE 自带 Maven）
- Node.js 22.18+（项目 `package.json` 已声明版本要求）
- MySQL 8+
- Redis 6+
- RabbitMQ 3+
- 一个可用的 SMTP 邮箱账号

## 初始化数据库

后端默认连接 MySQL 的 `test` 数据库，并使用 `db_account` 表保存账号。可以先执行以下 SQL：

```sql
CREATE DATABASE IF NOT EXISTS test
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE test;

CREATE TABLE IF NOT EXISTS db_account (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

> 如果数据库已经存在，请根据实际表结构执行迁移，不要重复覆盖生产数据。

## 配置后端

编辑 [`RayVue-backend/src/main/resources/application.yml`](RayVue-backend/src/main/resources/application.yml)，至少确认以下配置：

- MySQL 地址、数据库名、用户名和密码
- Redis 地址、端口和密码
- RabbitMQ 地址、虚拟主机、用户名和密码
- SMTP 主机、邮箱用户名和授权码
- JWT 签名密钥和过期时间

当前配置文件中包含开发环境凭据。部署或提交代码前，请立即替换这些值，并通过环境变量、配置中心或未纳入版本控制的配置文件管理密钥。

RabbitMQ 需要能够访问名为 `mailQueue` 的队列；应用启动时会自动声明该队列。用户请求验证码后，后端先将邮件任务放入队列，再由消费者异步发送邮件；验证码默认在 Redis 中保存 3 分钟。

## 启动后端

在项目根目录执行：

```powershell
cd RayVue-backend
mvn spring-boot:run
```

也可以先打包再运行：

```powershell
mvn clean package
java -jar target/RayVue-backend-0.0.1-SNAPSHOT.jar
```

后端默认使用 Spring Boot 的 `8080` 端口。启动后可用下面的接口做连通性检查：

```text
GET http://localhost:8080/api/test/hello
```

## 启动前端

另开一个终端：

```powershell
cd rayvue-frontend
npm install
npm run dev
```

Vite 会输出本地开发地址，通常是 `http://localhost:5173`。前端通过 `/api` 调用后端接口；如果前后端不在同一来源，请在开发服务器或部署网关中配置反向代理，并确认后端 CORS 配置允许前端来源。

生产构建：

```powershell
npm run build
npm run preview
```

## 主要接口

| 方法 | 路径 | 说明 | 是否需要 JWT |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | 登录，Spring Security 接管 | 否 |
| GET | `/api/auth/ask-code?email=...&type=register` | 获取注册验证码 | 否 |
| GET | `/api/auth/ask-code?email=...&type=reset` | 获取重置密码验证码 | 否 |
| POST | `/api/auth/register` | 注册账号 | 否 |
| POST | `/api/auth/reset-confirm` | 校验重置验证码 | 否 |
| POST | `/api/auth/reset-password` | 修改密码 | 否 |
| GET | `/api/auth/logout` | 退出登录并使 JWT 进入黑名单 | 是 |
| GET | `/api/test/hello` | 测试接口 | 是 |

登录成功后，响应中会返回 JWT。访问受保护接口时需要携带：

```http
Authorization: Bearer <token>
```

前端会根据“记住我”选项将 token 保存到 `localStorage` 或 `sessionStorage`，并在 token 过期后自动清理。

## 认证与限流说明

- 登录请求由 Spring Security 的 `formLogin` 处理，不对应一个普通 Controller 方法。
- JWT 默认有效期由 `spring.security.jwt.expireTime` 控制，单位为天。
- 退出登录会把当前 JWT 的 `jti` 写入 Redis 黑名单，直到 token 自然过期。
- 验证码请求按 IP 限制频率；全局请求限流也依赖 Redis。
- 密码使用 BCrypt 保存，数据库中不会保存明文密码。

## 常见问题

### 验证码收不到

检查 SMTP 主机、邮箱授权码、RabbitMQ 连接和 `mailQueue` 消费者日志。验证码发送是异步的，RabbitMQ 不可用时请求可能无法正常完成。

### 前端提示网络错误

确认后端已经启动，并检查浏览器开发者工具中的请求地址、端口和 CORS 配置。前端请求默认使用 `/api` 路径，不会自动把请求转发到另一个端口。

### 登录成功但访问接口返回 401

确认请求头使用的是 `Authorization: Bearer <token>`，并检查 Redis 是否可用、JWT 密钥是否在前后端重启后保持一致，以及 token 是否已经过期或被注销。

## 开发建议

- 不要把真实邮箱密码、RabbitMQ/Redis 密码或 JWT 密钥提交到 Git。
- 生产环境请使用 HTTPS，并限制 MySQL、Redis 和 RabbitMQ 的网络暴露范围。
- 修改接口请求参数时，同时更新前端表单校验和后端 `VO` 校验。
- 部署前建议补充数据库迁移脚本、统一的 API 错误码和自动化测试。

## License

当前仓库未声明许可证。如需公开发布或被其他项目使用，请补充合适的开源许可证。
