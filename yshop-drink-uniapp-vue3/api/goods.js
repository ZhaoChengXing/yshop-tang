import api from './api'

/**
 * 获得banner列表 
 */
export function shopNearby(data) {
  return api.get('/store/nearby', data, { login: true })
}
/**
 * 获取首页信息
 */
export function menuGoods(data) {
  return api.get('/product/products', data, { login: true })
}

/**
 * 店长商品列表（含未上架）
 */
export function managerGoodsList(data) {
  return api.get('/product/manager/list', data, { login: true })
}

/**
 * 新增/编辑商品
 */
export function managerGoodsSave(data) {
  return api.post('/product/manager/save', data, { login: true })
}

/**
 * 删除商品
 */
export function managerGoodsDelete(id) {
  return api.post('/product/manager/delete?id=' + id, {}, { login: true })
}

/**
 * 商品上下架
 */
export function managerGoodsSale(data) {
  return api.post('/product/manager/sale', data, { login: true })
}

