# 本地开发

## 环境

- Java 21
- Node.js 20 或更高
- 微信开发者工具
- 可选：Docker（项目本身不依赖 Docker 才能启动）

## 启动

```bash
./mvnw test
npm test
./mvnw -pl backend spring-boot:run
```

默认配置：

- API：`http://127.0.0.1:8080`
- 数据库：H2 文件 `.data/takeaway`
- 图片：`.data/uploads`
- 身份：请求头 `X-Debug-Openid`
- 通知：日志模拟，每 10 秒发送一次

健康检查：

```bash
curl http://127.0.0.1:8080/actuator/health
```

会话检查：

```bash
curl -H 'X-Debug-Openid: local-customer' \
  http://127.0.0.1:8080/api/session
```

## 微信开发者工具

1. 导入仓库根目录，而不是只导入 `miniprogram/`。
2. 使用测试号或自己的 AppID。
3. 确认 `miniprogram/config/env.js` 的 `mode` 为 `local`。
4. 在“详情 / 本地设置”中启用“不校验合法域名”。
5. 点击编译。

`127.0.0.1` 指开发者工具所在的电脑，因此备案前可在模拟器中测试完整流程。真机预览涉及微信网络域名和 HTTPS 限制，建议等云托管配置完成后再验收。

## 本地测试账号

| 身份 | 调试 OpenID | 初始邀请码 |
|---|---|---|
| 顾客 | `local-customer` | `LOVE-CUSTOMER` |
| 商家 | `local-merchant` | `LOVE-MERCHANT` |

邀请码只能使用一次，但绑定后的 OpenID 下次无需再次输入。页面上的“本地验收”按钮负责切换这两个 OpenID。

## 数据重置

如果想从第一次邀请重新验收：

1. 停止 Java 服务。
2. 将 `.data/` 重命名为备份目录。
3. 重新启动服务。

服务会自动创建新数据库并填充示例菜单。保留备份可以随时恢复旧数据。

## MySQL 模式

安装 Docker 后可运行：

```bash
docker compose up --build
```

或者自行创建 MySQL 数据库并设置 `.env.example` 中的变量，再用 `mysql` profile 启动。Flyway 会自动建表。
