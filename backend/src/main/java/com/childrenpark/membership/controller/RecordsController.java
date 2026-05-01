package com.childrenpark.membership.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.childrenpark.membership.common.PageResult;
import com.childrenpark.membership.common.Result;
import com.childrenpark.membership.context.UserContext;
import com.childrenpark.membership.entity.ConsumeRecord;
import com.childrenpark.membership.service.ConsumeRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Slf4j
@Api(tags = "记录控制器")
@RestController
@RequestMapping("/records")
public class RecordsController {

    @Autowired
    private ConsumeRecordService consumeRecordService;

    @ApiOperation("获取消费记录列表")
    @GetMapping("/consume")
    public Result<PageResult<ConsumeRecord>> getConsumeRecords(
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
        
        Page<ConsumeRecord> pageResult = consumeRecordService.getUserRecords(userId, page, size, localDate);
        
        PageResult<ConsumeRecord> result = PageResult.of(
                pageResult.getRecords(),
                pageResult.getTotal(),
                pageResult.getSize(),
                pageResult.getCurrent()
        );
        
        return Result.success(result);
    }

    @ApiOperation("获取消费记录详情")
    @GetMapping("/{id}")
    public Result<ConsumeRecord> getRecordDetail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        ConsumeRecord record = consumeRecordService.getById(id);
        if (record == null || record.getDeleted() == 1) {
            return Result.error("记录不存在");
        }
        
        if (!record.getUserId().equals(userId) && !UserContext.isAdmin()) {
            return Result.forbidden();
        }
        
        return Result.success(record);
    }
}
