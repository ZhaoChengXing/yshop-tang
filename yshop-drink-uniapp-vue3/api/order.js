import api from './api'

/**
 * 订单列表  
 */
export function orderTakeFoods(data) {
  return api.get('/order/list', data, { login: true })
}

/**
 * 订单创建  
 */
export function orderSubmit(data) {
  return api.post(`/order/create`, data, { login: true })
}



/**
 * 订单列表  
 */
export function orderGetOrders(data) {
  return api.get(`/order/list`, data, { login: true })
}


/**
 * 计算详情 
 */
export function orderDetail(data) {
  return api.get(`/order/detail/${data}`, data, { login: true })
}



/**
 * 订单收货 
 */
export function orderReceive(data) {
  return api.post(`/order/take`, data, { login: true })
}

/**
 * 订单退款 
 */
export function orderRefund(data) {
  return api.post(`/order/refund`, data, { login: true })
}


/**
 * 订单支付 
 */
export function payUnify(data) {
  return api.post(`/order/pay`, data, { login: true })
}

/**
 * getWechatConfig 	
 */
export function getWechatConfig() {
  return api.get(`/member/wx-mp/create-jsapi-signature`, { url: location.href }, { login: true })
}

/**
 * 店长订单列表 type: -1 全部 0 未支付 1 待制作 2 待取货 4 已完成 -3 退款
 */
export function managerOrderList(data) {
  return api.get('/order/manager/list', data, { login: true })
}

/**
 * 店长订单详细
 */
export function managerOrderDetail(id) {
  return api.get('/order/manager/detail?id=' + id, {}, { login: true })
}

/**
 * 店长查看任意用户的订单详情
 */
export function managerOrderInfo(orderId) {
  return api.get('/order/manager/order-detail?orderId=' + orderId, {}, { login: true })
}

/**
 * 出单
 */
export function managerOrderSend(data) {
  return api.post('/order/manager/send', data, { login: true })
}

/**
 * 删除订单
 */
export function managerOrderDelete(data) {
  return api.post('/order/manager/delete', data, { login: true })
}
