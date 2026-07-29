# Security Policy

## Supported version

当前只维护主分支最新版本。

## 报告问题

请不要在公开 Issue 中提交 AppSecret、数据库凭据、OpenID、邀请码或真实订单内容。安全问题请通过仓库所有者提供的私密联系方式报告。

## 部署底线

- 生产环境使用 `APP_IDENTITY_MODE=wechat-cloud`。
- 所有秘密放在部署平台环境变量中。
- 初始商家邀请码使用后立即从环境变量删除。
- MySQL 不直接暴露到公网。
- 云数据库和云存储开启备份与最小权限。
