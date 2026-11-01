package co.yixiang.yshop.module.product.controller.app.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 APP - 店长商品上下架 Request VO")
@Data
public class ManagerProductSaleReqVO {

    @Schema(description = "商品id", required = true)
    @NotNull(message = "商品不能为空")
    private Long id;

    @Schema(description = "是否上架：0 否 1 是", required = true)
    @NotNull(message = "上下架状态不能为空")
    private Integer isShow;

}
