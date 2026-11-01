<template>
	<uv-navbar
	  :fixed="false"
	  :title="title"
	  left-arrow
	  @leftClick="$onClickLeft"
	/>
	<view class="container goods-form">
		<list-cell :hover="false">
			<view class="goods-form__field">
				<view class="goods-form__label">菜品名称</view>
				<input class="goods-form__input" placeholder="例如：番茄炒蛋" v-model="form.storeName"
					placeholder-class="text-color-assist" />
			</view>
		</list-cell>
		<list-cell :hover="false">
			<view class="goods-form__field">
				<view class="goods-form__label">分类</view>
				<picker class="goods-form__input" mode="selector" :range="cateNames" :value="cateIndex"
					@change="onCateChange">
					<view :class="{ 'text-color-assist': !form.cateId }">{{ cateNames[cateIndex] || '请选择分类' }}</view>
				</picker>
			</view>
		</list-cell>
		<list-cell :hover="false">
			<view class="goods-form__field">
				<view class="goods-form__label">价格</view>
				<input class="goods-form__input" type="digit" placeholder="0.00" v-model="form.price"
					placeholder-class="text-color-assist" />
			</view>
		</list-cell>
		<list-cell :hover="false">
			<view class="goods-form__field">
				<view class="goods-form__label">库存</view>
				<input class="goods-form__input" type="number" placeholder="99" v-model="form.stock"
					placeholder-class="text-color-assist" />
			</view>
		</list-cell>
		<list-cell :hover="false">
			<view class="goods-form__field">
				<view class="goods-form__label">简介</view>
				<input class="goods-form__input" placeholder="一句话描述" v-model="form.storeInfo"
					placeholder-class="text-color-assist" />
			</view>
		</list-cell>
		<list-cell :hover="false">
			<view class="goods-form__field">
				<view class="goods-form__label">图片</view>
				<view class="goods-form__image-box" @tap="chooseImage">
					<image v-if="form.image" :src="form.image" class="goods-form__image" mode="aspectFill"></image>
					<view v-else class="goods-form__image-empty">+ 上传</view>
				</view>
			</view>
		</list-cell>
		<list-cell :hover="false">
			<view class="goods-form__field">
				<view class="goods-form__label">上架</view>
				<switch :checked="form.isShow == 1" color="#DA5650" @change="onSaleChange" />
			</view>
		</list-cell>
		<view class="goods-form__actions">
			<button type="primary" size="default" class="rounded-pill goods-form__btn" @tap="save">保存</button>
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
import { onLoad } from '@dcloudio/uni-app'
import { managerGoodsList, managerGoodsSave } from '@/api/goods'
import { VUE_APP_UPLOAD_URL } from '@/config'
import cookie from '@/utils/cookie'

const main = useMainStore()
const { store, lang } = storeToRefs(main)

const title = ref('新增菜品')
const cates = ref([])
const cateIndex = ref(0)
const cateNames = computed(() => cates.value.map(item => item.name))

const form = ref({
	id: null,
	shopId: 0,
	cateId: '',
	storeName: '',
	storeInfo: '',
	image: '',
	price: '',
	stock: '',
	isShow: 1
})

const toast = (message) => uni.showToast({
	title: message,
	icon: 'none'
})

onLoad(async (option) => {
	form.value.shopId = option.shopId ? Number(option.shopId) : (store.value.id || 0)
	if (option.id) {
		title.value = '编辑菜品'
		form.value.id = Number(option.id)
	}
	await loadCates()
	if (form.value.id) {
		fillGoods(form.value.id)
	} else if (cates.value.length > 0) {
		form.value.cateId = String(cates.value[0].id)
	}
})

const loadCates = async () => {
	const data = await managerGoodsList({ shopId: form.value.shopId || 0 })
	cates.value = data || []
}

const fillGoods = (id) => {
	for (const cate of cates.value) {
		const good = (cate.goodsList || []).find(item => item.id == id)
		if (!good) continue
		form.value = {
			id: good.id,
			shopId: form.value.shopId,
			cateId: String(good.cateId),
			storeName: good.storeName,
			storeInfo: good.storeInfo || '',
			image: good.image,
			price: good.price,
			stock: good.stock,
			isShow: good.isShow
		}
		cateIndex.value = Math.max(0, cates.value.findIndex(item => item.id == good.cateId))
		return
	}
	toast('菜品不存在')
}

const onCateChange = (e) => {
	cateIndex.value = Number(e.detail.value)
	form.value.cateId = String(cates.value[cateIndex.value].id)
}

const onSaleChange = (e) => {
	form.value.isShow = e.detail.value ? 1 : 0
}

const chooseImage = () => {
	uni.chooseImage({
		count: 1,
		success: (res) => {
			uploadImage(res.tempFilePaths[0])
		}
	})
}

const uploadImage = (filePath) => {
	uni.showLoading({
		title: '上传中'
	})
	uni.uploadFile({
		url: VUE_APP_UPLOAD_URL,
		filePath: filePath,
		name: 'file',
		header: {
			Authorization: 'Bearer ' + (main.token || cookie.get('accessToken')),
			lang: lang.value,
			'content-type': 'application/x-www-form-urlencoded;charset=UTF-8'
		},
		success(uploadFileResult) {
			uni.hideLoading()
			let upload = null
			try {
				upload = JSON.parse(uploadFileResult.data)
			} catch (e) {
				upload = null
			}
			if (upload && upload.code === 0 && upload.data) {
				form.value.image = upload.data
			} else {
				toast((upload && upload.msg) || '上传失败')
			}
		},
		fail() {
			uni.hideLoading()
			toast('上传失败')
		}
	})
}

const save = async () => {
	const name = (form.value.storeName || '').trim()
	if (!name) return toast('请输入菜品名称')
	if (!form.value.cateId) return toast('请选择分类')
	if (form.value.price === '' || isNaN(Number(form.value.price)) || Number(form.value.price) < 0) {
		return toast('请输入正确的价格')
	}
	const stock = parseInt(form.value.stock)
	if (isNaN(stock) || stock < 0) return toast('请输入正确的库存')
	if (!form.value.image) return toast('请上传菜品图片')

	const data = await managerGoodsSave({
		id: form.value.id || undefined,
		shopId: form.value.shopId,
		cateId: form.value.cateId,
		storeName: name,
		storeInfo: form.value.storeInfo,
		image: form.value.image,
		price: Number(form.value.price),
		stock: stock,
		isShow: form.value.isShow
	})
	if (data) {
		toast('已保存')
		setTimeout(() => uni.navigateBack(), 600)
	}
}

</script>

<style lang="scss" scoped>
$goods-form-padding: $spacing-row-lg;
$goods-form-label-width: 200rpx;
$goods-form-image-size: 180rpx;
$goods-form-btn-height: 90rpx;
$goods-form-btn-width: 85%;

.goods-form {
	--goods-form-label-width: #{$goods-form-label-width};
	--goods-form-image-size: #{$goods-form-image-size};
	--goods-form-btn-height: #{$goods-form-btn-height};
	--goods-form-btn-width: #{$goods-form-btn-width};

	display: flex;
	flex-direction: column;
	width: 100%;
	min-height: 100%;
	padding: $goods-form-padding;
	color: $text-color-base;

	&__field {
		display: flex;
		align-items: center;
		width: 100%;
	}

	&__label {
		flex-shrink: 0;
		width: var(--goods-form-label-width);
		font-size: $font-size-lg;
		font-weight: 500;
	}

	&__input {
		flex: 1;
		display: flex;
		align-items: center;
	}

	&__image-box {
		width: var(--goods-form-image-size);
		height: var(--goods-form-image-size);
		border-radius: $border-radius-base;
		background-color: $bg-color-grey;
		overflow: hidden;
	}

	&__image {
		width: 100%;
		height: 100%;
	}

	&__image-empty {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 100%;
		height: 100%;
		font-size: $font-size-base;
		color: $text-color-assist;
	}

	&__actions {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: $spacing-row-lg 0 60rpx;
	}

	&__btn {
		display: flex;
		align-items: center;
		justify-content: center;
		width: var(--goods-form-btn-width);
		height: var(--goods-form-btn-height);
		font-size: $font-size-base;
	}
}
</style>
