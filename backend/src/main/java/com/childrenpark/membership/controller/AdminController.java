package com.childrenpark.membership.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.childrenpark.membership.common.PageResult;
import com.childrenpark.membership.common.Result;
import com.childrenpark.membership.context.UserContext;
import com.childrenpark.membership.entity.ConsumeRecord;
import com.childrenpark.membership.entity.Project;
import com.childrenpark.membership.entity.QRCode;
import com.childrenpark.membership.entity.User;
import com.childrenpark.membership.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Api(tags = "管理员控制器")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ConsumeRecordService consumeRecordService;

    @Autowired
    private RechargeService rechargeService;

    @ApiOperation("扫码验证二维码")
    @PostMapping("/scan-qrcode")
    public Result<Map<String, Object>> scanQRCode(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        if (!UserContext.isAdmin()) {
            return Result.forbidden();
        }
        
        String qrcodeData = params.get("qrcodeData");
        if (qrcodeData == null || qrcodeData.isEmpty()) {
            return Result.error("缺少二维码数据");
        }
        
        QRCode qrCode = qrCodeService.validateQRCode(qrcodeData);
        if (qrCode == null) {
            return Result.error("二维码无效或已过期");
        }
        
        User user = userService.getById(qrCode.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("openid", user.getOpenid());
        userInfo.put("nickName", user.getNickName());
        userInfo.put("avatarUrl", user.getAvatarUrl());
        userInfo.put("balance", user.getBalance());
        userInfo.put("phone", user.getPhone());
        
        result.put("userInfo", userInfo);
        result.put("qrcodeData", qrcodeData);
        
        return Result.success(result);
    }

    @ApiOperation("扣币操作")
    @PostMapping("/deduct-coins")
    public Result<Void> deductCoins(@RequestBody Map<String, Object> params) {
        Long operatorId = UserContext.getUserId();
        if (operatorId == null) {
            return Result.unauthorized();
        }
        
        if (!UserContext.isAdmin()) {
            return Result.forbidden();
        }
        
        String qrcodeData = (String) params.get("qrcodeData");
        Long projectId = params.get("projectId") != null ? 
                Long.valueOf(params.get("projectId").toString()) : null;
        Integer quantity = params.get("quantity") != null ? 
                Integer.valueOf(params.get("quantity").toString()) : 1;
        Integer amount = params.get("amount") != null ? 
                Integer.valueOf(params.get("amount").toString()) : null;
        
        if (qrcodeData == null || qrcodeData.isEmpty()) {
            return Result.error("缺少二维码数据");
        }
        
        if (amount == null || amount <= 0) {
            return Result.error("扣币数量必须大于0");
        }
        
        QRCode qrCode = qrCodeService.validateQRCode(qrcodeData);
        if (qrCode == null) {
            return Result.error("二维码无效或已过期");
        }
        
        Project project = null;
        String projectName = "消费";
        
        if (projectId != null) {
            project = projectService.getById(projectId);
            if (project != null) {
                projectName = project.getName();
            }
        }
        
        User operator = userService.getById(operatorId);
        String operatorName = operator != null ? operator.getNickName() : "管理员";
        
        try {
            consumeRecordService.deductCoins(
                    qrCode.getUserId(),
                    projectId,
                    projectName,
                    quantity,
                    amount,
                    operatorId,
                    operatorName
            );
            
            qrCodeService.markAsUsed(qrcodeData);
            
            return Result.success();
        } catch (Exception e) {
            log.error("扣币失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("获取二维码有效期设置")
    @GetMapping("/qrcode-validity")
    public Result<Map<String, Integer>> getQRCodeValidity() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        if (!UserContext.isAdmin()) {
            return Result.forbidden();
        }
        
        int validityMinutes = qrCodeService.getQRCodeValidityMinutes();
        
        Map<String, Integer> result = new HashMap<>();
        result.put("validityTime", validityMinutes);
        
        return Result.success(result);
    }

    @ApiOperation("更新二维码有效期设置")
    @PutMapping("/qrcode-validity")
    public Result<Void> updateQRCodeValidity(@RequestBody Map<String, Integer> params) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        if (!UserContext.isAdmin()) {
            return Result.forbidden();
        }
        
        Integer validityTime = params.get("validityTime");
        if (validityTime == null || validityTime <= 0) {
            return Result.error("有效期必须大于0");
        }
        
        try {
            qrCodeService.updateQRCodeValidity(validityTime);
            return Result.success();
        } catch (Exception e) {
            log.error("更新二维码有效期失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取消费记录列表（管理员）")
    @GetMapping("/records/consume")
    public Result<PageResult<ConsumeRecord>> getConsumeRecords(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long userId) {
        Long operatorId = UserContext.getUserId();
        if (operatorId == null) {
            return Result.unauthorized();
        }
        
        if (!UserContext.isAdmin()) {
            return Result.forbidden();
        }
        
        if (userId == null) {
            userId = UserContext.getUserId();
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
}
