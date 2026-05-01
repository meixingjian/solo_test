package com.childrenpark.membership.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.childrenpark.membership.entity.ConsumeRecord;

import java.time.LocalDate;

public interface ConsumeRecordService extends IService<ConsumeRecord> {

    Page<ConsumeRecord> getUserRecords(Long userId, int page, int size, LocalDate date);

    void deductCoins(Long userId, Long projectId, String projectName, 
                      int quantity, int amount, Long operatorId, String operatorName);
}
