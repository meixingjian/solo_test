package com.childrenpark.membership.controller;

import com.childrenpark.membership.common.Result;
import com.childrenpark.membership.context.UserContext;
import com.childrenpark.membership.service.QRCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Api(tags = "二维码控制器")
@RestController
@RequestMapping("/qrcode")
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;

    @ApiOperation("生成动态二维码")
    @PostMapping("/generate")
    public Result<Map<String, Object>> generateQRCode() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        try {
            Map<String, Object> result = qrCodeService.generateQRCode(userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("生成二维码失败", e);
            return Result.error("生成二维码失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取当前二维码信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getQRCodeInfo() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        try {
            Map<String, Object> result = qrCodeService.getCurrentQRCode(userId);
            if (result == null) {
                result = qrCodeService.generateQRCode(userId);
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取二维码信息失败", e);
            return Result.error("获取二维码信息失败：" + e.getMessage());
        }
    }

    @ApiOperation("刷新二维码")
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshQRCode() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        try {
            Map<String, Object> result = qrCodeService.refreshQRCode(userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("刷新二维码失败", e);
            return Result.error("刷新二维码失败：" + e.getMessage());
        }
    }
}
