package com.childrenpark.membership.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.childrenpark.membership.entity.RechargeOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface RechargeService extends IService<RechargeOrder> {

    List<Map<String, Object>> getRechargeOptions();

    Map<String, Object> createRechargeOrder(Long userId, int amount) throws Exception;

    Page<RechargeOrder> getUserRechargeRecords(Long userId, int page, int size, LocalDate date);

    void processPaymentSuccess(String orderNo, String transactionId);
}
