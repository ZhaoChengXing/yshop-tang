package co.yixiang.yshop.module.order.controller.app.order.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "用户 APP - 店长订单操作 Request VO")
@Data
public class AppManagerOrderIdParam implements Serializable {

    @Schema(description = "订单ID", required = true, example = "31716")
    @NotNull(message = "订单ID不能为空")
    private Long id;

}
