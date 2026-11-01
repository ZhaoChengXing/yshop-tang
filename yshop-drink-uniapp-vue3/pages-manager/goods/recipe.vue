<template>
	<uv-navbar
	  :fixed="false"
	  title="制作方法"
	  left-arrow
	  @leftClick="$onClickLeft"
	/>
	<view class="container recipe-page">
		<view class="recipe-empty" v-if="loading">加载中...</view>
		<block v-else>
			<view class="recipe-head">
				<image class="recipe-head__image" :src="detail.image" mode="aspectFill"></image>
				<view class="recipe-head__info">
					<view class="recipe-head__name">{{ detail.storeName || '菜品' }}</view>
					<view class="recipe-head__desc" v-if="detail.storeInfo">{{ detail.storeInfo }}</view>
				</view>
			</view>

			<view class="recipe-card" v-if="detail.videoUrl">
				<view class="recipe-card__title">抖音教学视频</view>
				<view class="recipe-video" @tap="openVideo">
					<view class="recipe-video__icon">播放</view>
					<view class="recipe-video__info">
						<view class="recipe-video__name">{{ detail.videoTitle || '复制链接，打开抖音观看' }}</view>
						<view class="recipe-video__hint">点击复制链接，再打开抖音 App</view>
					</view>
				</view>
			</view>

			<view class="recipe-card" v-if="steps.length">
				<view class="recipe-card__title">菜谱</view>
				<view class="recipe-step" v-for="(step, index) in steps" :key="index">
					<view class="recipe-step__index">{{ index + 1 }}</view>
					<view class="recipe-step__text">{{ step }}</view>
				</view>
			</view>

			<view class="recipe-empty-text" v-if="!detail.videoUrl && !steps.length">
				这道菜还没有添加菜谱或教学视频
			</view>
		</block>
	</view>
</template>

<script setup>
import {
	ref,
	computed
} from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { managerGoodsDetail } from '@/api/goods'

const detail = ref({})
const loading = ref(true)

const steps = computed(() => {
	return (detail.value.recipe || '')
		.split(/\r?\n/)
		.map(item => item.trim().replace(/^\d+[.、)）]\s*/, ''))
		.filter(Boolean)
})

onLoad(async (option) => {
	if (!option.id) {
		loading.value = false
		return
	}
	try {
		detail.value = (await managerGoodsDetail(option.id)) || {}
	} catch (e) {
		detail.value = {}
	} finally {
		loading.value = false
	}
})

const openVideo = () => {
	const url = detail.value.videoUrl
	if (!url) return
	uni.setClipboardData({
		data: url,
		success() {
			uni.showModal({
				title: '已复制链接',
				content: '打开抖音 App 即可观看这个教学视频',
				showCancel: false,
				confirmText: '知道了'
			})
		}
	})
}
</script>

<style lang="scss" scoped>
$recipe-radius: 8rpx;

.recipe-page {
	padding: $spacing-row-base;
	padding-bottom: 60rpx;
}

.recipe-empty {
	padding: 200rpx 0;
	font-size: $font-size-sm;
	text-align: center;
	color: $text-color-assist;
}

.recipe-head {
	display: flex;
	align-items: center;
	padding: $spacing-row-base;
	background-color: $text-color-white;
	border-radius: $recipe-radius;

	&__image {
		flex-shrink: 0;
		width: 140rpx;
		height: 140rpx;
		margin-right: $spacing-row-base;
		border-radius: $recipe-radius;
	}

	&__info {
		flex: 1;
		overflow: hidden;
	}

	&__name {
		margin-bottom: 10rpx;
		font-size: $font-size-lg;
		font-weight: 600;
		color: $text-color-base;
	}

	&__desc {
		font-size: $font-size-sm;
		color: $text-color-assist;
	}
}

.recipe-card {
	margin-top: $spacing-row-base;
	padding: $spacing-row-base;
	background-color: $text-color-white;
	border-radius: $recipe-radius;

	&__title {
		margin-bottom: $spacing-row-base;
		font-size: $font-size-base;
		font-weight: 600;
		color: $text-color-base;
	}
}

.recipe-video {
	display: flex;
	align-items: center;
	padding: $spacing-row-base;
	background-color: $bg-color-grey;
	border-radius: $recipe-radius;

	&__icon {
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		width: 80rpx;
		height: 80rpx;
		margin-right: $spacing-row-base;
		font-size: 22rpx;
		color: $text-color-white;
		background-color: $color-primary;
		border-radius: 50%;
	}

	&__info {
		flex: 1;
		overflow: hidden;
	}

	&__name {
		margin-bottom: 6rpx;
		font-size: $font-size-base;
		color: $text-color-base;
	}

	&__hint {
		font-size: $font-size-sm;
		color: $text-color-assist;
	}
}

.recipe-step {
	display: flex;
	align-items: flex-start;
	padding: 12rpx 0;

	&__index {
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		width: 40rpx;
		height: 40rpx;
		margin-right: $spacing-row-base;
		font-size: $font-size-sm;
		color: $text-color-white;
		background-color: $color-primary;
		border-radius: 50%;
	}

	&__text {
		flex: 1;
		font-size: $font-size-base;
		line-height: 1.6;
		color: $text-color-base;
		white-space: pre-wrap;
		word-break: break-all;
	}
}

.recipe-empty-text {
	padding: 120rpx 0;
	font-size: $font-size-sm;
	text-align: center;
	color: $text-color-assist;
}
</style>
