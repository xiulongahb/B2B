# B2B 平台后端（经销商 PC 商城 — 手机验证码登录）

## 环境要求

- **JDK**：8 或以上（推荐 11+）
- **Maven**：3.6+

本机若无 Maven，可使用 [SDKMAN](https://sdkman.io/) 或 IDE 自带 Maven 运行。

## 运行

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

默认端口：`8080`。使用 `dev`  profile 时，发码接口会在 JSON 中返回 `debugCode`（仅联调用，**生产务必关闭** `b2b.mall.sms.dev-expose-code-in-response`）。

## 登录网页（静态页）

启动后端后浏览器访问：

**http://localhost:8080/mall/**

页面调用同源 `/api/mall/auth/sms/send` 与 `/api/mall/auth/sms/login`，登录成功后将 JWT 写入 `sessionStorage`（键名 `mall_access_token`）。若将 `static/mall/index.html` 单独部署到其他域名，请修改页面内脚本顶部的 `API` 变量为后端根地址，并在后端配置 CORS。

## 接口说明（经销商 / PC 商城）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/mall/auth/sms/send` | 发送登录验证码，body: `{"phone":"13800138000"}` |
| POST | `/api/mall/auth/sms/login` | 验证码登录，body: `{"phone":"13800138000","code":"123456"}` |
| GET | `/api/mall/auth/me` | 当前登录会员，Header: `Authorization: Bearer <accessToken>` |

登录成功返回 `accessToken`（JWT）、`expiresInMs`、`memberId`、`memberType` 等。  
**首次验证码登录成功**会自动创建 `RETAIL` 类型会员（可按产品改为必须已注册）。

## 配置项

- `b2b.jwt.secret`：至少 32 字节，生产必须替换。
- `b2b.mall.sms.*`：验证码长度、有效期、重发间隔；生产对接真实短信需实现 `SmsGateway` 并关闭 `dev-expose-code-in-response`。
- 当前验证码存储为 **进程内内存**，多实例部署请改为 Redis 等共享存储。

## 测试

```bash
mvn test
```

使用 `test` profile（见 `src/test/resources/application-test.yml`）缩短重发间隔并在响应中带出验证码。

## 短信网关

默认 `LoggingSmsGateway` 仅打日志。生产可新增 `@Primary` 实现类对接阿里云/腾讯云短信。
