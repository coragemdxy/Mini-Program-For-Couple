# 备案后微信部署清单

本地功能不需要备案。以下步骤等小程序主体、备案和微信侧资源可用后执行，具体入口名称以当时微信公众平台控制台为准。

## 1. 小程序配置

1. 将 `project.config.json` 的 `appid` 从 `touristappid` 改成真实 AppID。
2. 创建微信云开发环境和云托管服务。
3. 修改 `miniprogram/config/env.js`：

```js
module.exports = {
  mode: 'cloud',
  localBaseUrl: 'http://127.0.0.1:8080',
  cloudEnv: '你的云环境 ID',
  cloudService: '你的云托管服务名',
  newOrderTemplateId: '新订单模板 ID',
  orderStatusTemplateId: '订单状态模板 ID'
}
```

4. 在微信公众平台申请两个订阅消息模板：商家新订单、顾客订单状态。
5. 模板字段必须与后端 `NotificationService` 使用的字段匹配；如平台给出的字段名不同，需要同步修改后端 payload。

## 2. 后端与数据库

云托管使用仓库根目录的 `backend/Dockerfile` 构建，容器监听环境变量 `PORT`。

准备 MySQL，并设置：

```text
SPRING_PROFILES_ACTIVE=mysql
DB_URL=jdbc:mysql://...
DB_USERNAME=...
DB_PASSWORD=...
APP_IDENTITY_MODE=wechat-cloud
APP_NOTIFICATION_MODE=wechat
WECHAT_APP_ID=...
WECHAT_APP_SECRET=...
WECHAT_NEW_ORDER_TEMPLATE_ID=...
WECHAT_ORDER_STATUS_TEMPLATE_ID=...
APP_BOOTSTRAP_MERCHANT_CODE=一个足够长且随机的一次性邀请码
```

不要设置 `app.local-seed.enabled=true`。Flyway 会在空库中自动创建结构。

## 3. 首位商家

1. 第一次部署前设置 `APP_BOOTSTRAP_MERCHANT_CODE`。
2. 用自己的微信进入小程序，输入该邀请码完成商家绑定。
3. 成功后从云托管环境变量中删除这个明文邀请码。
4. 后续顾客邀请码都从“小店设置”生成。

初始商家邀请码只允许使用一次，明文不会写入数据库或日志。

## 4. 图片

云模式下，小程序直接把商品图片上传到微信云存储，并把 `cloud://` 文件 ID 保存到商品。Java 容器本地磁盘只用于本地模式，不应作为云环境的长期图片存储。

## 5. 身份检查

部署前必须确认：

- `APP_IDENTITY_MODE=wechat-cloud`。
- 外部请求伪造 `X-Debug-Openid` 不能登录。
- Java 服务只信任云托管注入的 `X-WX-OPENID`。
- AppSecret、数据库密码没有提交到 Git。

## 6. 订阅消息验收

1. 商家在“小店设置”点击允许新单提醒。
2. 顾客下单时允许订单状态提醒。
3. 顾客创建订单，商家微信收到新订单。
4. 商家接受、准备、完成，顾客微信收到相应状态。
5. 检查云托管日志中没有持续的 `43101`（用户未授权或授权次数已用完）。

微信订阅消息通常依赖用户主动授权，不能把它当作永久推送渠道；界面内订单列表始终是最终事实来源。

## 7. 发布

1. 在开发者工具完成真机调试。
2. 上传代码，设置体验版。
3. 用商家和顾客两个微信完成一遍验收清单。
4. 提交微信审核。
5. 审核通过后发布正式版。

正式发布前再次查看微信官方最新的类目、备案、隐私保护和订阅消息要求。
