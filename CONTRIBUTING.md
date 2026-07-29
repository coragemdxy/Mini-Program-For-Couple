# Contributing

感谢参与改进。提交变更前请：

1. 使用 Java 21 和 Node.js 20+。
2. 不提交 `.env`、AppSecret、数据库密码、OpenID 或真实订单数据。
3. 保持小程序无构建依赖，除非变更中明确说明理由。
4. 数据库结构只通过新的 Flyway migration 修改，不改写已经发布的 migration。
5. 运行：

```bash
./mvnw test
npm test
```

Pull Request 请说明用户行为变化、验证步骤以及是否涉及数据库、微信模板或部署配置。
