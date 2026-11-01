package co.yixiang.yshop.module.product.controller.app.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 店长菜品做法 Response VO")
@Data
public class ManagerProductRecipeRespVO {

    @Schema(description = "菜品id", required = true, example = "1")
    private Long id;

    @Schema(description = "菜品名称", example = "番茄炒蛋")
    private String storeName;

    @Schema(description = "菜品图片")
    private String image;

    @Schema(description = "菜品简介")
    private String storeInfo;

    @Schema(description = "菜谱/制作步骤")
    private String recipe;

    @Schema(description = "教学视频链接（抖音分享链接）")
    private String videoUrl;

    @Schema(description = "教学视频标题")
    private String videoTitle;

}
