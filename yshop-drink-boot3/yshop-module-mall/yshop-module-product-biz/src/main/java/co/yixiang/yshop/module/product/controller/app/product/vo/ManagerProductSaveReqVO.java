package co.yixiang.yshop.module.product.controller.app.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "用户 APP - 店长商品保存 Request VO")
@Data
public class ManagerProductSaveReqVO {

    @Schema(description = "商品id，新增时为空")
    private Long id;

    @Schema(description = "店铺id", required = true, example = "2")
    @NotNull(message = "店铺不能为空")
    private Integer shopId;

    @Schema(description = "分类id", required = true, example = "1")
    @NotBlank(message = "请选择分类")
    private String cateId;

    @Schema(description = "商品名称", required = true, example = "番茄炒蛋")
    @NotBlank(message = "商品名称不能为空")
    private String storeName;

    @Schema(description = "商品简介", example = "少放盐")
    private String storeInfo;

    @Schema(description = "商品图片", required = true)
    @NotBlank(message = "商品图片不能为空")
    private String image;

    @Schema(description = "价格", required = true, example = "18.00")
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @Schema(description = "库存", required = true, example = "20")
    @NotNull(message = "库存不能为空")
    private Integer stock;

    @Schema(description = "是否上架：0 否 1 是", example = "1")
    private Integer isShow;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "菜谱/制作步骤")
    private String recipe;

    @Schema(description = "教学视频链接（抖音分享链接）", example = "https://v.douyin.com/xxxx/")
    private String videoUrl;

    @Schema(description = "教学视频标题")
    private String videoTitle;

}
