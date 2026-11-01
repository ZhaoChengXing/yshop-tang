<template>
	<uv-navbar
	  :fixed="false"
	  :title="title"
	  left-arrow
	  @leftClick="$onClickLeft"
	/>
	<view class="container order-page">
		<view class="bg-white">
			<uv-tabs :list="tabList" :current="current" @change="change" keyName="name" :scrollable="false"></uv-tabs>
		</view>
		<view class="order-page__list d-flex flex-column w-100">
			<view class="order-item" v-for="item in orders" :key="item.orderId">
				<list-cell :hover="false">
					<view class="w-100 d-flex align-items-center">
						<view class="flex-fill d-flex flex-column">
							<view class="font-size-lg text-color-base order-item__shop">
								{{ item.shop && item.shop.name ? item.shop.name : '门店' }}
							</view>
							<view class="font-size-sm text-color-assist">取餐号：{{ item.numberId }}</view>
							<view class="font-size-sm text-color-assist">订单编号：{{ item.orderId }}</view>
						</view>
						<view class="font-size-lg text-color-primary">
							{{ item.statusDto.title }}
						</view>
					</view>
				</list-cell>
				<list-cell :hover="false" last>
					<view class="w-100 d-flex flex-column">
						<view class="w-100 text-truncate font-size-lg text-color-base order-item__goods">
							<view class="flex order-item__goods-row mb-2" v-for="(good, gIndex) in item.cartInfo" :key="good.id || `${good.title}-${gIndex}`">
								<image :src="good.image" mode="aspectFill" class="order-item__thumb" lazy-load></image>
								<view class="flex flex-column">
									<view class="font-size-medium mt-1 text-color-base">{{ good.title }}</view>
									<view class="font-size-sm mt-1">{{ good.spec }}</view>
									<view class="font-size-sm mt-2">×{{ good.number }}  ¥{{ good.price }}</view>
								</view>
							</view>
						</view>
						<view class="d-flex justify-content-between align-items-center order-item__meta">
							<view class="font-size-sm text-color-assist">
								{{formatDateTime(item.createTime) }}
							</view>
							<view class="d-flex font-size-sm text-color-base align-items-center">
								<view class="order-item__summary">共{{ goodsNum(item.cartInfo) }}件商品，实付</view>
								<view class="font-size-lg">￥{{ item.payPrice }}</view>
							</view>
						</view>
						<view class="d-flex align-items-center justify-content-end order-item__actions">
							<button
								v-if="isSelfOrder(item) && item.paid > 0 && item.status < 2 && item.refundStatus == 0"
								class="order-item__btn"
								plain
								size="mini"
								@tap.stop="receive(item)"
							>确认收到餐</button>
							<button class="order-item__btn" plain size="mini" @tap="detail(item.orderId)">订单详情</button>
							<template v-if="isShopManager">
								<button class="order-item__btn" plain size="mini" @tap.stop="payDetail(item.id)">支付详情</button>
								<button
									v-if="canSend(item)"
									class="order-item__btn"
									plain
									size="mini"
									@tap.stop="send(item)"
								>出单</button>
								<button class="order-item__btn" plain size="mini" @tap.stop="remove(item)">删除</button>
							</template>
						</view>
					</view>
				</list-cell>
			</view>
		</view>
		<uv-empty v-if="orders.length == 0" mode="order"></uv-empty>
	</view>
</template>


<script setup>
import {
  ref,
  computed,
  watch
} from 'vue'
import { useMainStore } from '@/store/store'
import { storeToRefs } from 'pinia'
import { onLoad,onShow,onPullDownRefresh,onReachBottom} from '@dcloudio/uni-app'
import { formatDateTime } from '@/utils/util'
import {
  orderGetOrders,
  orderReceive,
  managerOrderList,
  managerOrderSend,
  managerOrderDelete
} from '@/api/order'
const main = useMainStore()
const { isLogin, isShopManager, member } = storeToRefs(main)
const title = computed(() => isShopManager.value ? '订单管理' : '我的订单')

const page = ref(1)
const pageSize = ref(10)
const orders = ref([])
const tabList = [{
			type: -1,
			name: '全部',
		}, {
			type: 0,
			name: '待支付',
		}, {
			type: 1,
			name: '进行中'
		}, {
			type: 4,
			name: '已完成'
		}, {
			type: -3,
			name: '退款单'
		}]
const current = ref(0)
const type = ref(-1)

const goodsNum = computed(() => { //计算单个饮品添加到购物车的数量
	return (goods) => {
		let num = 0;
		goods.forEach(good => num += parseInt(good.number))
		return num;
	}
})
onLoad(() => {
	if(!isLogin.value) {
		uni.navigateTo({url: '/pages-user/login/login'})
	}
	getOrders(false)
})
// 角色刷新后，店长改为查全部店铺订单
watch(isShopManager, () => {
	current.value = 0
	type.value = -1
	getOrders(true)
})
// tabBar 页的 onLoad 只会触发一次，支付完成后 switchTab 回来只能靠 onShow 重新拉取
let firstShow = true
onShow(() => {
	// 老会话里可能还没有角色字段，进页面时补一次
	if (isLogin.value) {
		main.refreshMember()
	}
	if (firstShow) {
		firstShow = false
		return
	}
	getOrders(true)
})
onPullDownRefresh(() => {
	 getOrders(false)
})
onReachBottom(() => {
	getOrders(false)
})

// tab栏切换
const change = (e) => {
	type.value = e.type
	getOrders(true)
}

const getOrders = async(isRefresh = false) => {
	uni.showLoading({
		title: '加载中'
	})
	if(isRefresh) {
		orders.value = []
		page.value = 1
	}
	let ordersData = null
	if (isShopManager.value) {
		ordersData = await managerOrderList({
			type: type.value,
			page: page.value,
			limit: pageSize.value
		})
	} else {
		ordersData = await orderGetOrders({page:page.value, limit:pageSize.value,type:type.value});
	}

	if(ordersData) {
		orders.value = orders.value.concat(ordersData)
		page.value += 1
	}
	uni.stopPullDownRefresh();
	uni.hideLoading()
}
const detail = (id) => {
	// 店长看的是所有店铺的订单，详情要走店长接口
	const url = isShopManager.value
		? '/pages-order/orders/detail?id=' + id + '&manage=1'
		: '/pages-order/orders/detail?id=' + id
	uni.navigateTo({
		url: url
	})
}
// 店长：查看订单中心工作台“详情”里的信息
const payDetail = (id) => {
	uni.navigateTo({
		url: '/pages-manager/orders/pay-detail?id=' + id
	})
}
// 确认收到货
const receive  = async(order) => {
	let data = await orderReceive({uni:order.orderId});
	if (data) {
		await getOrders(true)
	}
}
// 店长：出单（与后台工作台一致）
const send = async(order) => {
	let data = await managerOrderSend({id: order.id})
	if (data) {
		uni.showToast({ title: '已出单', icon: 'none' })
		await getOrders(true)
	}
}
// 店长：删除订单
const remove = async(order) => {
	uni.showModal({
		title: '提示',
		content: `确定删除订单 ${order.orderId} 吗？`,
		success: async (res) => {
			if (!res.confirm) return
			let data = await managerOrderDelete({id: order.id})
			if (data) {
				uni.showToast({ title: '已删除', icon: 'none' })
				await getOrders(true)
			}
		}
	})
}

const isSelfOrder = (item) => {
	return !member.value || !member.value.id || item.uid == member.value.id
}

// 已支付、未退款、还没完成/收货的订单才显示出单
const canSend = (item) => {
	return item.paid > 0 && item.refundStatus == 0 && item.status < 2
}
</script>

<style lang="scss" scoped>
// 订单页局部 token（与 uni.scss 全局变量配合）
$order-list-padding-x: $spacing-row-base;
$order-list-padding-bottom: 0;
$order-item-gap: $spacing-row-lg;
$order-section-gap: $spacing-row-base;
$order-btn-gap: $spacing-row-base;
$order-summary-gap: $spacing-row-base;
$order-thumb-size: 160rpx;
$order-thumb-radius: 8rpx;

.order-page {
	--order-thumb-size: #{$order-thumb-size};

	&__list {
		padding: $order-list-padding-x;
		padding-bottom: $order-list-padding-bottom;
	}
}

.order-item {
	margin-bottom: $order-item-gap;

	&__shop {
		margin-bottom: $order-section-gap;
	}

	&__goods {
		margin-bottom: $order-section-gap;
	}

	&__thumb {
		flex-shrink: 0;
		width: var(--order-thumb-size);
		height: var(--order-thumb-size);
		margin-right: $spacing-row-lg;
		border-radius: $order-thumb-radius;
	}

	&__meta {
		margin-bottom: $order-item-gap;
	}

	&__summary {
		margin-right: $order-summary-gap;
	}

	&__btn + &__btn {
		margin-left: $order-btn-gap;
	}
}
</style>
