package com.childrenpark.membership.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.childrenpark.membership.common.PageResult;
import com.childrenpark.membership.common.Result;
import com.childrenpark.membership.context.UserContext;
import com.childrenpark.membership.entity.RechargeOrder;
import com.childrenpark.membership.service.RechargeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "充值控制器")
@RestController
@RequestMapping("/recharge")
public class RechargeController {

    @Autowired
    private RechargeService rechargeService;

    @ApiOperation("获取充值选项")
    @GetMapping("/options")
    public Result<List<Map<String, Object>>> getRechargeOptions() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        List<Map<String, Object>> options = rechargeService.getRechargeOptions();
        return Result.success(options);
    }

    @ApiOperation("创建充值订单")
    @PostMapping("/order")
    public Result<Map<String, Object>> createRechargeOrder(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        Integer amount = params.get("amount") != null ? 
                Integer.valueOf(params.get("amount").toString()) : null;
        
        if (amount == null || amount <= 0) {
            return Result.error("充值金额必须大于0");
        }
        
        try {
            Map<String, Object> result = rechargeService.createRechargeOrder(userId, amount);
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建充值订单失败", e);
            return Result.error("创建充值订单失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取充值记录")
    @GetMapping("/records")
    public Result<PageResult<Map<String, Object>>> getRechargeRecords(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String date) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        LocalDate localDate = null;
        if (date != null && !date.isEmpty()) {
            try {
                localDate = LocalDate.parse(date);
            } catch (DateTimeParseException e) {
                // ignore
            }
        }
        
        Page<RechargeOrder> pageResult = rechargeService.getUserRechargeRecords(userId, page, size, localDate);
        
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(order -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", order.getId());
                    item.put("orderNo", order.getOrderNo());
                    item.put("amount", order.getAmount());
                    item.put("coinsReceived", order.getCoinsReceived());
                    item.put("giftCoins", order.getGiftCoins());
                    item.put("paymentMethod", order.getPaymentMethod());
                    item.put("transactionId", order.getTransactionId());
                    item.put("status", order.getStatus());
                    item.put("payTime", order.getPayTime());
                    item.put("createTime", order.getCreateTime());
                    item.put("type", "recharge");
                    return item;
                })
                .toList();
        
        PageResult<Map<String, Object>> result = PageResult.of(
                records,
                pageResult.getTotal(),
                pageResult.getSize(),
                pageResult.getCurrent()
        );
        
        return Result.success(result);
    }

    @ApiOperation("模拟支付成功回调")
    @PostMapping("/pay-callback")
    public Result<Void> payCallback(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo");
        String transactionId = params.get("transactionId");
        
        if (orderNo == null || orderNo.isEmpty()) {
            return Result.error("缺少订单号");
        }
        
        try {
            rechargeService.processPaymentSuccess(orderNo, 
                    transactionId != null ? transactionId : "mock_transaction_" + System.currentTimeMillis());
            return Result.success();
        } catch (Exception e) {
            log.error("处理支付回调失败", e);
            return Result.error("处理支付回调失败：" + e.getMessage());
        }
    }
}
