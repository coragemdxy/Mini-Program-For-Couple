# HTTP API

所有 JSON 接口都位于 `/api`。本地请求需要 `X-Debug-Openid`；云托管由微信注入 `X-WX-OPENID`。

## 会话和邀请

| 方法 | 路径 | 角色 | 说明 |
|---|---|---|---|
| GET | `/api/session` | 未激活也可 | 返回 `NEED_INVITE`、`ACTIVE` 或 `BLOCKED` |
| POST | `/api/invitations/redeem` | 未激活也可 | 使用邀请码绑定 OpenID 和角色 |
| POST | `/api/merchant/invitations` | 商家 | 生成一次性/限次顾客邀请码 |

兑换示例：

```json
{
  "code": "LOVE-CUSTOMER",
  "nickname": "小可爱"
}
```

## 商品目录

| 方法 | 路径 | 角色 | 说明 |
|---|---|---|---|
| GET | `/api/categories` | 已激活 | 顾客可见分类 |
| GET | `/api/products` | 已激活 | 按 `categoryId`、`query` 分页检索 |
| GET | `/api/products/{id}` | 已激活 | 顾客可见商品详情 |
| GET/POST | `/api/merchant/categories` | 商家 | 查询或创建分类 |
| PUT/DELETE | `/api/merchant/categories/{id}` | 商家 | 更新或删除分类 |
| GET/POST | `/api/merchant/products` | 商家 | 查询或创建商品 |
| GET/PUT/DELETE | `/api/merchant/products/{id}` | 商家 | 商品详情、更新、删除 |
| PATCH | `/api/merchant/products/{id}/availability` | 商家 | 上下架 |
| POST | `/api/merchant/media` | 商家 | 本地模式 multipart 图片上传 |

商品规格由 `optionGroups` 表达。每组包含 `required`、`minSelect`、`maxSelect` 和 `options`；额外价格由服务端计入。

## 订单

| 方法 | 路径 | 角色 | 说明 |
|---|---|---|---|
| POST | `/api/orders` | 顾客 | 创建订单 |
| GET | `/api/orders/my` | 顾客 | 我的订单 |
| GET | `/api/orders/{id}` | 顾客/商家 | 订单详情；顾客只能看自己的 |
| POST | `/api/orders/{id}/cancel` | 顾客 | 取消未接单订单 |
| GET | `/api/merchant/orders` | 商家 | 订单列表，可按 `status` 筛选 |
| GET | `/api/merchant/orders/{id}` | 商家 | 订单详情 |
| PATCH | `/api/merchant/orders/{id}/status` | 商家 | 更新订单状态 |

下单示例：

```json
{
  "idempotencyKey": "wx-unique-request-id",
  "remark": "晚上八点再准备",
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "optionIds": [1, 5]
    }
  ]
}
```

## 通知

| 方法 | 路径 | 角色 | 说明 |
|---|---|---|---|
| POST | `/api/notifications/consents` | 已激活 | 记录微信订阅结果 |
| GET | `/api/notifications/settings` | 已激活 | 查看各消息类型的授权/发送计数 |

消息类型为 `NEW_ORDER` 和 `ORDER_STATUS`。

## 错误格式

业务错误统一返回：

```json
{
  "timestamp": "2026-07-29T12:00:00Z",
  "status": 409,
  "code": "ORDER_STATUS_TRANSITION_INVALID",
  "message": "订单不能从“已完成”变为“准备中”",
  "path": "/api/merchant/orders/1/status",
  "fieldErrors": {}
}
```

客户端可以展示 `message`，并用 `code` 做稳定的程序判断。
