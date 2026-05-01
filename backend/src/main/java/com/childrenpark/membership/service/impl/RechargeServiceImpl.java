package com.childrenpark.membership.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.childrenpark.membership.entity.RechargeOrder;
import com.childrenpark.membership.entity.User;
import com.childrenpark.membership.mapper.RechargeOrderMapper;
import com.childrenpark.membership.mapper.UserMapper;
import com.childrenpark.membership.service.RechargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RechargeServiceImpl extends ServiceImpl<RechargeOrderMapper, RechargeOrder> implements RechargeService {

    @Autowired
    private UserMapper userMapper;

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public List<Map<String, Object>> getRechargeOptions() {
        List<Map<String, Object>> options = new ArrayList<>();
        
        options.add(createOption(1L, 10, 10, 0));
        options.add(createOption(2L, 30, 30, 3));
        options.add(createOption(3L, 50, 50, 6));
        options.add(createOption(4L, 100, 100, 15));
        options.add(createOption(5L, 200, 200, 40));
        options.add(createOption(6L, 500, 500, 100));
        
        return options;
    }

    private Map<String, Object> createOption(Long id, int amount, int price, int gift) {
        Map<String, Object> option = new HashMap<>();
        option.put("id", id);
        option.put("amount", amount);
        option.put("price", price);
        option.put("gift", gift);
        return option;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createRechargeOrder(Long userId, int amount) throws Exception {
        if (amount <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        String orderNo = "RC" + LocalDateTime.now().format(ORDER_NO_FORMATTER) + 
                String.format("%04d", (int)(Math.random() * 10000));
        
        int giftCoins = calculateGiftCoins(amount);
        int coinsReceived = amount + giftCoins;
        
        RechargeOrder order = new RechargeOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setAmount(BigDecimal.valueOf(amount));
        order.setCoinsReceived(coinsReceived);
        order.setGiftCoins(giftCoins);
        order.setPaymentMethod("wechat");
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(0);
        save(order);
        
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("amount", amount);
        result.put("coinsReceived", coinsReceived);
        result.put("giftCoins", giftCoins);
        
        result.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        result.put("nonceStr", generateNonceStr());
        result.put("package", "prepay_id=mock_prepay_id");
        result.put("signType", "RSA");
        result.put("paySign", "mock_pay_sign");
        
        return result;
    }

    @Override
    public Page<RechargeOrder> getUserRechargeRecords(Long userId, int page, int size, LocalDate date) {
        LambdaQueryWrapper<RechargeOrder> wrapper = new LambdaQueryWrapper<RechargeOrder>()
                .eq(RechargeOrder::getUserId, userId)
                .eq(RechargeOrder::getDeleted, 0)
                .orderByDesc(RechargeOrder::getCreateTime);
        
        if (date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            wrapper.between(RechargeOrder::getCreateTime, startOfDay, endOfDay);
        }
        
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processPaymentSuccess(String orderNo, String transactionId) {
        RechargeOrder order = getOne(new LambdaQueryWrapper<RechargeOrder>()
                .eq(RechargeOrder::getOrderNo, orderNo)
                .eq(RechargeOrder::getStatus, 0));
        
        if (order == null) {
            throw new RuntimeException("订单不存在或已支付");
        }
        
        order.setStatus(1);
        order.setTransactionId(transactionId);
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            user.setBalance(user.getBalance().add(BigDecimal.valueOf(order.getCoinsReceived())));
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }

    private int calculateGiftCoins(int amount) {
        if (amount >= 500) {
            return 100;
        } else if (amount >= 200) {
            return 40;
        } else if (amount >= 100) {
            return 15;
        } else if (amount >= 50) {
            return 6;
        } else if (amount >= 30) {
            return 3;
        }
        return 0;
    }

    private String generateNonceStr() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        return sb.toString();
    }
}
