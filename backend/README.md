# B2B 平台后端（经销商 PC 商城 — 注册 / 登录）

## 环境要求

- **JDK**：8 或以上（推荐 11+）
- **Maven**：3.6+
- **数据库**：默认 **MySQL 8**（见下文）；本地无 MySQL 可使用 **`dev` profile 走 H2 内存库**。

## MySQL（默认）

1. 在项目根目录启动容器（与 `docker-compose.yml` 同级）：

   ```bash
   cd /path/to/B2B
   docker compose up -d
   ```

   默认：`localhost:3306`，库名 `b2b_mall`，用户 `root`，密码 `root`（可通过环境变量 `MYSQL_ROOT_PASSWORD` 覆盖）。

2. 启动应用（**默认连接 MySQL**，见 `src/main/resources/application.yml`）：

   ```bash
   cd backend
   mvn spring-boot:run
   ```

环境变量（可选）：

- `MYSQL_USER` / `MYSQL_PASSWORD`：覆盖默认 `root` / `root`

## 无 MySQL 时使用 H2（开发）

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

`dev` profile 下使用 H2 内存库，且发码接口会返回 `debugCode`（**仅联调，生产务必关闭**）。

## 登录网页（静态页）

**http://localhost:8080/mall/index.html**

包含：**密码登录**、**短信登录**、**注册**（注册需先获取短信验证码）。令牌写入 `sessionStorage`（`mall_access_token` 等）。

## 接口说明（经销商 / PC 商城）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/mall/auth/sms/send` | 发送短信验证码，body: `{"phone":"13800138000"}` |
| POST | `/api/mall/auth/register` | 注册，body: `username`,`password`,`phone`,`smsCode` |
| POST | `/api/mall/auth/password/login` | 用户名密码登录，body: `username`,`password` |
| POST | `/api/mall/auth/sms/login` | 手机验证码登录（**已注册手机号**），body: `phone`,`code` |
| GET | `/api/mall/auth/me` | 当前用户，Header: `Authorization: Bearer <accessToken>` |

校验规则：

- **用户名**：4–32 位，字母 `a-zA-Z`、数字、下划线。
- **密码**：8–64 位，须同时包含**大写、小写、数字**。

登录成功返回 `accessToken`、`expiresInMs`、`memberId`、`username`、`phone`、`memberType` 等。

## 配置项

- `b2b.jwt.secret`：至少 32 字节，生产必须替换。
- `b2b.mall.sms.*`：验证码长度、有效期、重发间隔；生产对接真实短信需实现 `SmsGateway`。
- 验证码当前为 **进程内内存**存储，多实例请改为 Redis。

## 测试

```bash
mvn test
```

`test` profile 使用 H2 与缩短的重发间隔。

## 短信网关

默认 `LoggingSmsGateway` 仅打日志。生产可新增 `@Primary` 实现类对接阿里云/腾讯云短信。
