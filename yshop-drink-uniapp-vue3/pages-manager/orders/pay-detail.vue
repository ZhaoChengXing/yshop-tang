<template>
	<uv-navbar
	  :fixed="false"
	  title="支付详情"
	  left-arrow
	  @leftClick="$onClickLeft"
	/>
	<view class="container pay-detail" v-if="order">
		<!-- 收货信息（外卖） -->
		<view class="pay-detail__section" v-if="order.orderType == 'takeout'">
			<view class="pay-detail__title">收货信息</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">用户昵称</text>
				<text class="pay-detail__value">{{ nickname || '--' }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">收货人</text>
				<text class="pay-detail__value">{{ order.realName || '--' }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">联系电话</text>
				<text class="pay-detail__value">{{ order.userPhone || '--' }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">收货地址</text>
				<text class="pay-detail__value">{{ order.userAddress || '--' }}</text>
			</view>
		</view>

		<!-- 商品明细 -->
		<view class="pay-detail__section">
			<view class="pay-detail__title">商品明细</view>
			<view class="pay-detail__good" v-for="(good, index) in cartInfo" :key="good.id || index">
				<image class="pay-detail__good-image" :src="good.image" mode="aspectFill" lazy-load></image>
				<view class="pay-detail__good-info">
					<view class="pay-detail__good-name">{{ good.title }}</view>
					<view class="pay-detail__good-spec" v-if="good.spec">{{ good.spec }}</view>
					<view class="pay-detail__good-price">￥{{ good.price }} × {{ good.number }}</view>
				</view>
			</view>
			<view class="pay-detail__empty" v-if="cartInfo.length == 0">暂无商品</view>
		</view>

		<!-- 订单信息 -->
		<view class="pay-detail__section">
			<view class="pay-detail__title">
				订单信息
				<text class="pay-detail__tag">{{ orderTypeText }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">门店</text>
				<text class="pay-detail__value">{{ order.shopName || '--' }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">取餐号</text>
				<text class="pay-detail__value">{{ order.numberId || '--' }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">桌位号</text>
				<text class="pay-detail__value">{{ order.deskNumber || '无' }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">就餐人数</text>
				<text class="pay-detail__value">{{ order.deskPeople || '无' }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">订单号</text>
				<text class="pay-detail__value">{{ order.orderId }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">订单状态</text>
				<text class="pay-detail__value">{{ order.statusStr }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">商品总数</text>
				<text class="pay-detail__value">{{ order.totalNum }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">商品总价</text>
				<text class="pay-detail__value">￥{{ order.totalPrice }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">支付邮费</text>
				<text class="pay-detail__value">￥{{ order.payPostage }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">优惠券金额</text>
				<text class="pay-detail__value">￥{{ order.couponPrice }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">积分抵扣</text>
				<text class="pay-detail__value">{{ order.useIntegral }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">实际支付</text>
				<text class="pay-detail__value pay-detail__value--price">￥{{ order.payPrice }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">赠送积分</text>
				<text class="pay-detail__value">{{ order.gainIntegral }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">创建时间</text>
				<text class="pay-detail__value">{{ formatDateTime(order.createTime) }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">支付时间</text>
				<text class="pay-detail__value">{{ order.payTime ? formatDateTime(order.payTime) : '--' }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">支付方式</text>
				<text class="pay-detail__value">{{ payTypeText }}</text>
			</view>
			<view class="pay-detail__row">
				<text class="pay-detail__label">订单备注</text>
				<text class="pay-detail__value">{{ order.mark || '--' }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import {
  ref,
  computed
} from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { managerOrderDetail } from '@/api/order'
import { formatDateTime } from '@/utils/util'

const order = ref(null)

const cartInfo = computed(() => (order.value && order.value.storeOrderCartInfoDOList) || [])
const nickname = computed(() => (order.value && order.value.userRespVO && order.value.userRespVO.nickname) || '')

const orderTypeText = computed(() => {
	const map = {
		takeout: '外卖',
		takein: '自取',
		desk: '堂食'
	}
	return (order.value && map[order.value.orderType]) || ''
})

const payTypeText = computed(() => {
	if (!order.value || order.value.paid != 1) return '--'
	const map = {
		yue: '余额支付',
		weixin: '微信支付',
		alipay: '支付宝支付',
		cash: '现金支付'
	}
	return map[order.value.payType] || '--'
})

onLoad(async (option) => {
	if (!option.id) {
		uni.showToast({ title: '订单不存在', icon: 'none' })
		return
	}
	order.value = await managerOrderDetail(option.id)
})
</script>

<style lang="scss" scoped>
$pay-detail-padding: $spacing-row-base;
$pay-detail-radius: 8rpx;
$pay-detail-image-size: 120rpx;

.pay-detail {
	padding: $pay-detail-padding;
	color: $text-color-base;

	&__section {
		padding: $spacing-row-base;
		margin-bottom: $spacing-row-base;
		background-color: $text-color-white;
		border-radius: $pay-detail-radius;
	}

	&__title {
		display: flex;
		align-items: center;
		margin-bottom: $spacing-row-base;
		font-size: $font-size-lg;
		font-weight: 600;
	}

	&__tag {
		margin-left: $spacing-row-base;
		padding: 2rpx 12rpx;
		font-size: $font-size-sm;
		font-weight: 400;
		color: $text-color-white;
		background-color: $color-primary;
		border-radius: $pay-detail-radius;
	}

	&__row {
		display: flex;
		align-items: flex-start;
		padding: 8rpx 0;
		font-size: $font-size-base;
	}

	&__label {
		flex-shrink: 0;
		width: 180rpx;
		color: $text-color-assist;
	}

	&__value {
		flex: 1;
		word-break: break-all;

		&--price {
			font-size: $font-size-lg;
			color: $color-primary;
		}
	}

	&__good {
		display: flex;
		align-items: center;
		padding: $spacing-row-base 0;
	}

	&__good-image {
		flex-shrink: 0;
		width: $pay-detail-image-size;
		height: $pay-detail-image-size;
		margin-right: $spacing-row-base;
		border-radius: $pay-detail-radius;
	}

	&__good-info {
		flex: 1;
		overflow: hidden;
	}

	&__good-name {
		margin-bottom: 8rpx;
		font-size: $font-size-base;
	}

	&__good-spec {
		margin-bottom: 8rpx;
		font-size: $font-size-sm;
		color: $text-color-assist;
	}

	&__good-price {
		font-size: $font-size-sm;
		color: $text-color-assist;
	}

	&__empty {
		padding: $spacing-row-lg 0;
		font-size: $font-size-sm;
		text-align: center;
		color: $text-color-assist;
	}
}
</style>
