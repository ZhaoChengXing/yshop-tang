package co.yixiang.yshop.module.product.controller.app.product;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import co.yixiang.yshop.framework.common.exception.ErrorCode;
import co.yixiang.yshop.framework.common.pojo.CommonResult;
import co.yixiang.yshop.framework.security.core.annotations.PreAuthenticated;
import co.yixiang.yshop.module.member.api.user.MemberUserApi;
import co.yixiang.yshop.module.product.controller.app.category.vo.AppCategoryRespVO;
import co.yixiang.yshop.module.product.controller.app.product.vo.ManagerProductRecipeRespVO;
import co.yixiang.yshop.module.product.controller.app.product.vo.ManagerProductSaleReqVO;
import co.yixiang.yshop.module.product.controller.app.product.vo.ManagerProductSaveReqVO;
import co.yixiang.yshop.module.product.dal.dataobject.storeproduct.StoreProductDO;
import co.yixiang.yshop.module.product.enums.product.SpecTypeEnum;
import co.yixiang.yshop.module.product.service.storeproduct.AppStoreProductService;
import co.yixiang.yshop.module.product.service.storeproduct.StoreProductService;
import co.yixiang.yshop.module.product.service.storeproduct.dto.FromatDetailDto;
import co.yixiang.yshop.module.product.service.storeproduct.dto.ProductFormatDto;
import co.yixiang.yshop.module.product.service.storeproduct.dto.StoreProductDto;
import co.yixiang.yshop.module.product.service.storeproductattr.StoreProductAttrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static co.yixiang.yshop.framework.common.exception.util.ServiceExceptionUtil.exception;
import static co.yixiang.yshop.framework.common.pojo.CommonResult.success;
import static co.yixiang.yshop.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static co.yixiang.yshop.module.product.enums.ErrorCodeConstants.STORE_PRODUCT_NOT_EXISTS;

/**
 * 用户 APP - 店长商品管理
 */
@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "用户 APP - 店长商品")
@RequestMapping("/product/manager")
public class AppManagerProductController {

    private static final ErrorCode MANAGER_ONLY = new ErrorCode(403, "仅店长可操作");

    private final AppStoreProductService appStoreProductService;
    private final StoreProductService storeProductService;
    private final StoreProductAttrService storeProductAttrService;
    private final MemberUserApi memberUserApi;

    @PreAuthenticated
    @GetMapping("/list")
    @Operation(summary = "店长商品列表")
    @Parameter(name = "shopId", description = "店铺id，0 表示全部", example = "2")
    public CommonResult<List<AppCategoryRespVO>> list(
            @RequestParam(value = "shopId", required = false) Integer shopId) {
        validateManager();
        return success(appStoreProductService.getManagerGoodsList(shopId));
    }

    @PreAuthenticated
    @GetMapping("/detail")
    @Operation(summary = "店长菜品做法详情")
    @Parameter(name = "id", description = "菜品id", required = true, example = "1")
    public CommonResult<ManagerProductRecipeRespVO> detail(@RequestParam("id") Long id) {
        validateManager();
        StoreProductDO product = storeProductService.getStoreProduct(id);
        if (product == null) {
            throw exception(STORE_PRODUCT_NOT_EXISTS);
        }
        ManagerProductRecipeRespVO respVO = new ManagerProductRecipeRespVO();
        BeanUtil.copyProperties(product, respVO);
        return success(respVO);
    }

    @PreAuthenticated
    @PostMapping("/save")
    @Operation(summary = "新增或编辑商品")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Boolean> save(@RequestBody @Valid ManagerProductSaveReqVO reqVO) {
        validateManager();
        if (reqVO.getPrice().compareTo(BigDecimal.ZERO) < 0 || reqVO.getStock() < 0) {
            throw exception(new ErrorCode(400, "价格和库存不能为负数"));
        }
        StoreProductDO old = reqVO.getId() != null ? storeProductService.getStoreProduct(reqVO.getId()) : null;
        if (reqVO.getId() != null && old == null) {
            throw exception(STORE_PRODUCT_NOT_EXISTS);
        }
        Integer shopId = old != null ? old.getShopId() : reqVO.getShopId();
        if (shopId == null || shopId <= 0) {
            throw exception(new ErrorCode(400, "请选择店铺"));
        }
        String image = reqVO.getImage();
        String description = StrUtil.blankToDefault(reqVO.getDescription(),
                StrUtil.blankToDefault(reqVO.getStoreInfo(), reqVO.getStoreName()));
        Integer isShow = reqVO.getIsShow() == null ? 1 : reqVO.getIsShow();

        if (old == null) {
            StoreProductDto dto = new StoreProductDto();
            dto.setShopId(shopId);
            dto.setCateId(reqVO.getCateId());
            dto.setStoreName(reqVO.getStoreName());
            dto.setStoreInfo(reqVO.getStoreInfo());
            dto.setImage(image);
            dto.setSliderImage(ListUtil.toList(image));
            dto.setDescription(description);
            dto.setRecipe(reqVO.getRecipe());
            dto.setVideoUrl(reqVO.getVideoUrl());
            dto.setVideoTitle(reqVO.getVideoTitle());
            dto.setIsShow(isShow);
            dto.setSpecType(SpecTypeEnum.TYPE_0.getValue());
            dto.setUnitName("份");
            dto.setSort(0L);
            dto.setSales(0L);
            dto.setIsHot(0);
            dto.setIsBenefit(0);
            dto.setIsBest(0);
            dto.setIsNew(0);
            dto.setIsPostage(0);
            dto.setIsIntegral(0);
            dto.setAttrs(ListUtil.toList(buildFormat(reqVO.getPrice(), reqVO.getStock(), image)));
            storeProductService.insertAndEditYxStoreProduct(dto);
            return success(true);
        }

        StoreProductDO updateObj = new StoreProductDO();
        updateObj.setId(old.getId());
        updateObj.setCateId(reqVO.getCateId());
        updateObj.setStoreName(reqVO.getStoreName());
        updateObj.setStoreInfo(reqVO.getStoreInfo());
        updateObj.setImage(image);
        updateObj.setSliderImage(image);
        updateObj.setDescription(description);
        updateObj.setRecipe(reqVO.getRecipe());
        updateObj.setVideoUrl(reqVO.getVideoUrl());
        updateObj.setVideoTitle(reqVO.getVideoTitle());
        updateObj.setPrice(reqVO.getPrice());
        updateObj.setStock(reqVO.getStock());
        updateObj.setIsShow(isShow);
        storeProductService.updateById(updateObj);

        // 同步单规格 SKU，点餐页取的是 productValue 里的价格和库存
        FromatDetailDto item = FromatDetailDto.builder()
                .value("规格")
                .detail(ListUtil.toList("默认"))
                .build();
        ProductFormatDto format = buildFormat(reqVO.getPrice(), reqVO.getStock(), image);
        format.setValue1("规格");
        format.setDetail(Map.of("规格", "默认"));
        storeProductAttrService.insertYxStoreProductAttr(ListUtil.toList(item), ListUtil.toList(format), old.getId());
        return success(true);
    }

    @PreAuthenticated
    @PostMapping("/delete")
    @Operation(summary = "删除商品")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        validateManager();
        if (storeProductService.getStoreProduct(id) == null) {
            throw exception(STORE_PRODUCT_NOT_EXISTS);
        }
        storeProductAttrService.deleteYxStoreProductAttrByProductId(id);
        storeProductService.deleteStoreProduct(id);
        return success(true);
    }

    @PreAuthenticated
    @PostMapping("/sale")
    @Operation(summary = "商品上下架")
    public CommonResult<Boolean> sale(@RequestBody @Valid ManagerProductSaleReqVO reqVO) {
        validateManager();
        if (storeProductService.getStoreProduct(reqVO.getId()) == null) {
            throw exception(STORE_PRODUCT_NOT_EXISTS);
        }
        StoreProductDO updateObj = new StoreProductDO();
        updateObj.setId(reqVO.getId());
        updateObj.setIsShow(reqVO.getIsShow());
        storeProductService.updateById(updateObj);
        return success(true);
    }

    private ProductFormatDto buildFormat(BigDecimal price, Integer stock, String image) {
        double priceValue = price.doubleValue();
        return ProductFormatDto.builder()
                .price(priceValue)
                .otPrice(priceValue)
                .cost(0d)
                .stock(stock)
                .integral(0)
                .pic(image)
                .weight(0d)
                .volume(0d)
                .brokerage(0d)
                .brokerageTwo(0d)
                .build();
    }

    private void validateManager() {
        if (!memberUserApi.isShopManager(getLoginUserId())) {
            throw exception(MANAGER_ONLY);
        }
    }

}
