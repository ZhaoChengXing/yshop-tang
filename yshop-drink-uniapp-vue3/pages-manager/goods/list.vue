<template>
	<uv-navbar
	  :fixed="false"
	  title="菜品管理"
	  left-arrow
	  @leftClick="$onClickLeft"
	/>
	<view class="container goods-page">
		<view class="goods-head">
			<view class="goods-head__title">菜品管理</view>
			<view class="goods-head__btn" @tap="goAdd">+ 新增菜品</view>
		</view>
		<view class="goods-list">
			<view class="goods-empty" v-if="loading">加载中...</view>
			<view class="goods-empty" v-else-if="cates.length === 0">还没有菜品，点右上角新增</view>
			<block v-else>
				<view class="goods-cate" v-for="cate in cates" :key="cate.id">
					<view class="goods-cate__title">{{ cate.name }}</view>
					<view class="goods-item" v-for="good in cate.goodsList" :key="good.id">
						<image class="goods-item__image" :src="good.image" mode="aspectFill" @tap="editGoods(good)"></image>
						<view class="goods-item__info">
							<view class="goods-item__name">{{ good.storeName }}</view>
							<view class="goods-item__meta">
								<text class="goods-item__price">￥{{ good.price }}</text>
								<text>库存 {{ good.stock }}</text>
								<text class="goods-item__state" :class="{ 'goods-item__state--off': good.isShow != 1 }">
									{{ good.isShow == 1 ? '在售' : '已下架' }}
								</text>
							</view>
						</view>
						<view class="goods-item__actions">
							<view class="goods-item__btn" @tap="showRecipe(good)">做法</view>
							<view class="goods-item__btn" @tap="editGoods(good)">编辑</view>
							<view class="goods-item__btn" @tap="toggleSale(good)">{{ good.isShow == 1 ? '下架' : '上架' }}</view>
							<view class="goods-item__btn goods-item__btn--danger" @tap="removeGoods(good)">删除</view>
						</view>
					</view>
				</view>
			</block>
		</view>
	</view>
</template>

<script setup>
import {
  ref,
  computed
} from 'vue'
import { useMainStore } from '@/store/store'
import { storeToRefs } from 'pinia'
import { onShow } from '@dcloudio/uni-app'
import { shopGetList } from '@/api/market'
import { managerGoodsList, managerGoodsDelete, managerGoodsSale } from '@/api/goods'

const main = useMainStore()
const { store, location } = storeToRefs(main)

const goods = ref([])
const loading = ref(false)
const cates = computed(() => goods.value.filter(item => item.goodsList && item.goodsList.length > 0))

onShow(() => {
	loadGoods()
})

const resolveShopId = async () => {
	if (store.value.id) return store.value.id
	const loc = location.value && location.value.latitude ? location.value : { latitude: 39.919990, longitude: 116.456270 }
	const shops = await shopGetList({
		lat: loc.latitude,
		lng: loc.longitude,
		kw: '',
		shop_id: 0
	})
	if (shops && shops.length > 0) {
		main.SET_STORE(shops[0])
		return shops[0].id
	}
	return 0
}

const loadGoods = async () => {
	loading.value = true
	try {
		const shopId = await resolveShopId()
		const data = await managerGoodsList({ shopId: shopId || 0 })
		goods.value = data || []
	} finally {
		loading.value = false
	}
}

const goAdd = async () => {
	const shopId = await resolveShopId()
	if (!shopId) {
		uni.showToast({ title: '还没选门店', icon: 'none' })
		return
	}
	uni.navigateTo({
		url: '/pages-manager/goods/edit?shopId=' + shopId
	})
}

const editGoods = async (good) => {
	const shopId = await resolveShopId()
	uni.navigateTo({
		url: '/pages-manager/goods/edit?id=' + good.id + '&shopId=' + (shopId || 0)
	})
}

const showRecipe = (good) => {
	uni.navigateTo({
		url: '/pages-manager/goods/recipe?id=' + good.id
	})
}

const toggleSale = async (good) => {
	const data = await managerGoodsSale({
		id: good.id,
		isShow: good.isShow == 1 ? 0 : 1
	})
	if (data) {
		loadGoods()
	}
}

const removeGoods = (good) => {
	uni.showModal({
		title: '删除菜品',
		content: '确定删除「' + good.storeName + '」吗？',
		success: async ({
			confirm
		}) => {
			if (!confirm) return
			const data = await managerGoodsDelete(good.id)
			if (data) {
				loadGoods()
			}
		}
	})
}
</script>

<style lang="scss" scoped>
$goods-radius: 8rpx;
$goods-list-bottom: 60rpx;

.goods-page {
	padding: 0 $spacing-row-base;
}

.goods-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: $spacing-row-lg 0;

	&__title {
		font-size: $font-size-lg;
		font-weight: 600;
		color: $text-color-base;
	}

	&__btn {
		padding: 12rpx $spacing-row-lg;
		font-size: $font-size-sm;
		color: $text-color-white;
		background-color: $color-primary;
		border-radius: 38rpx;
	}
}

.goods-list {
	width: 100%;
	padding-bottom: $goods-list-bottom;
}

.goods-empty {
	padding: 200rpx 0;
	font-size: $font-size-sm;
	text-align: center;
	color: $text-color-assist;
}

.goods-cate {
	margin-bottom: $spacing-row-base;

	&__title {
		padding: $spacing-row-base 0 10rpx;
		font-size: $font-size-base;
		font-weight: 600;
		color: $text-color-assist;
	}
}

.goods-item {
	display: flex;
	align-items: center;
	padding: $spacing-row-base;
	margin-bottom: 10rpx;
	background-color: $text-color-white;
	border-radius: $goods-radius;

	&__image {
		flex-shrink: 0;
		width: 120rpx;
		height: 120rpx;
		margin-right: $spacing-row-base;
		border-radius: $goods-radius;
	}

	&__info {
		flex: 1;
		overflow: hidden;
	}

	&__name {
		margin-bottom: 10rpx;
		font-size: $font-size-base;
		color: $text-color-base;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	&__meta {
		display: flex;
		align-items: center;
		font-size: $font-size-sm;
		color: $text-color-assist;
	}

	&__price {
		margin-right: $spacing-col-base;
		font-weight: 600;
		color: $text-color-base;
	}

	&__state {
		margin-left: $spacing-col-base;
		color: $color-primary;

		&--off {
			color: $text-color-grey;
		}
	}

	&__actions {
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		margin-left: 10rpx;
	}

	&__btn {
		padding: 4rpx 0 4rpx $spacing-row-base;
		font-size: $font-size-sm;
		color: $color-primary;

		&--danger {
			color: $color-error;
		}
	}
}
</style>
