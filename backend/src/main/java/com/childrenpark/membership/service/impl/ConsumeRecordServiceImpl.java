package com.childrenpark.membership.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.childrenpark.membership.entity.ConsumeRecord;
import com.childrenpark.membership.entity.User;
import com.childrenpark.membership.mapper.ConsumeRecordMapper;
import com.childrenpark.membership.mapper.UserMapper;
import com.childrenpark.membership.service.ConsumeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class ConsumeRecordServiceImpl extends ServiceImpl<ConsumeRecordMapper, ConsumeRecord> implements ConsumeRecordService {

    @Autowired
    private UserMapper userMapper;

    private static final DateTimeFormatter RECORD_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public Page<ConsumeRecord> getUserRecords(Long userId, int page, int size, LocalDate date) {
        LambdaQueryWrapper<ConsumeRecord> wrapper = new LambdaQueryWrapper<ConsumeRecord>()
                .eq(ConsumeRecord::getUserId, userId)
                .eq(ConsumeRecord::getDeleted, 0)
                .orderByDesc(ConsumeRecord::getCreateTime);
        
        if (date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            wrapper.between(ConsumeRecord::getCreateTime, startOfDay, endOfDay);
        }
        
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductCoins(Long userId, Long projectId, String projectName, 
                            int quantity, int amount, Long operatorId, String operatorName) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        BigDecimal currentBalance = user.getBalance();
        BigDecimal deductAmount = BigDecimal.valueOf(amount);
        
        if (currentBalance.compareTo(deductAmount) < 0) {
            throw new RuntimeException("余额不足");
        }
        
        user.setBalance(currentBalance.subtract(deductAmount));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        String recordNo = "CR" + LocalDateTime.now().format(RECORD_NO_FORMATTER) + 
                String.format("%04d", (int)(Math.random() * 10000));
        
        ConsumeRecord record = new ConsumeRecord();
        record.setRecordNo(recordNo);
        record.setUserId(userId);
        record.setProjectId(projectId);
        record.setProjectName(projectName);
        record.setQuantity(quantity);
        record.setAmount(amount);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setCreateTime(LocalDateTime.now());
        record.setDeleted(0);
        
        save(record);
    }
}
