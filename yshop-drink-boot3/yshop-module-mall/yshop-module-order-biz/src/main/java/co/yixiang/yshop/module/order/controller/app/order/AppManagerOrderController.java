package co.yixiang.yshop.module.order.controller.app.order;

import co.yixiang.yshop.framework.common.exception.ErrorCode;
import co.yixiang.yshop.framework.common.pojo.CommonResult;
import co.yixiang.yshop.framework.security.core.annotations.PreAuthenticated;
import co.yixiang.yshop.module.member.api.user.MemberUserApi;
import co.yixiang.yshop.module.order.controller.admin.storeorder.vo.StoreOrderRespVO;
import co.yixiang.yshop.module.order.controller.app.order.param.AppManagerOrderIdParam;
import co.yixiang.yshop.module.order.controller.app.order.vo.AppStoreOrderQueryVo;
import co.yixiang.yshop.module.order.service.storeorder.AppStoreOrderService;
import co.yixiang.yshop.module.order.service.storeorder.StoreOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static co.yixiang.yshop.framework.common.exception.util.ServiceExceptionUtil.exception;
import static co.yixiang.yshop.framework.common.pojo.CommonResult.success;
import static co.yixiang.yshop.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static co.yixiang.yshop.module.order.enums.ErrorCodeConstants.STORE_ORDER_NOT_EXISTS;

/**
 * 用户 APP - 店长订单（对应后台订单中心工作台）
 */
@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "用户 APP - 店长订单")
@RequestMapping("/order/manager")
public class AppManagerOrderController {

    private static final ErrorCode MANAGER_ONLY = new ErrorCode(403, "仅店长可操作");

    private final AppStoreOrderService appStoreOrderService;
    private final StoreOrderService storeOrderService;
    private final MemberUserApi memberUserApi;

    @PreAuthenticated
    @GetMapping("/list")
    @Operation(summary = "店长订单列表（全部店铺）")
    @Parameters({
            @Parameter(name = "type", description = "-1 全部 0 未支付 1 待制作 2 待取货 4 已完成 -3 退款", example = "-1"),
            @Parameter(name = "page", description = "页码", example = "1"),
            @Parameter(name = "limit", description = "条数", example = "10")
    })
    public CommonResult<List<AppStoreOrderQueryVo>> list(
            @RequestParam(value = "type", defaultValue = "-1") int type,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        validateManager();
        return success(appStoreOrderService.orderList(null, type, page, limit));
    }

    @PreAuthenticated
    @GetMapping("/detail")
    @Operation(summary = "店长订单详细（对应工作台“详情”）")
    @Parameter(name = "id", description = "订单ID", required = true, example = "31716")
    public CommonResult<StoreOrderRespVO> detail(@RequestParam("id") Long id) {
        validateManager();
        return success(storeOrderService.getStoreOrder(id));
    }

    @PreAuthenticated
    @GetMapping("/order-detail")
    @Operation(summary = "店长查看任意用户的订单详情")
    @Parameter(name = "orderId", description = "订单号", required = true, example = "YS202610310001")
    public CommonResult<AppStoreOrderQueryVo> orderDetail(@RequestParam("orderId") String orderId) {
        validateManager();
        AppStoreOrderQueryVo order = appStoreOrderService.getOrderInfo(orderId, null);
        if (order == null) {
            throw exception(STORE_ORDER_NOT_EXISTS);
        }
        return success(appStoreOrderService.handleOrder(order));
    }

    @PreAuthenticated
    @PostMapping("/send")
    @Operation(summary = "出单（对应工作台“出单”）")
    public CommonResult<Boolean> send(@RequestBody @Valid AppManagerOrderIdParam param) {
        validateManager();
        storeOrderService.orderSend(param.getId());
        return success(true);
    }

    @PreAuthenticated
    @PostMapping("/delete")
    @Operation(summary = "删除订单（对应工作台“删除”）")
    public CommonResult<Boolean> delete(@RequestBody @Valid AppManagerOrderIdParam param) {
        validateManager();
        storeOrderService.deleteStoreOrder(param.getId());
        return success(true);
    }

    private void validateManager() {
        if (!memberUserApi.isShopManager(getLoginUserId())) {
            throw exception(MANAGER_ONLY);
        }
    }

}
