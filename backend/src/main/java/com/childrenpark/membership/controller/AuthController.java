package com.childrenpark.membership.controller;

import com.childrenpark.membership.common.Result;
import com.childrenpark.membership.context.UserContext;
import com.childrenpark.membership.entity.User;
import com.childrenpark.membership.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Api(tags = "认证控制器")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @ApiOperation("微信登录")
    @PostMapping("/wx-login")
    public Result<Map<String, Object>> wxLogin(
            @ApiParam("微信登录code") @RequestBody Map<String, String> params) {
        try {
            String code = params.get("code");
            if (code == null || code.isEmpty()) {
                return Result.error("缺少登录凭证");
            }

            User user = userService.loginByCode(code);
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", user.getToken());
            result.put("userInfo", buildUserInfo(user));
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("微信登录失败", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/user-info")
    public Result<Map<String, Object>> getUserInfo() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        return Result.success(buildUserInfo(user));
    }

    @ApiOperation("更新用户信息")
    @PutMapping("/user-info")
    public Result<Map<String, Object>> updateUserInfo(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if (params.containsKey("nickName")) {
            user.setNickName(params.get("nickName"));
        }
        if (params.containsKey("avatarUrl")) {
            user.setAvatarUrl(params.get("avatarUrl"));
        }
        
        userService.updateById(user);
        
        return Result.success(buildUserInfo(user));
    }

    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("openid", user.getOpenid());
        userInfo.put("nickName", user.getNickName());
        userInfo.put("avatarUrl", user.getAvatarUrl());
        userInfo.put("gender", user.getGender());
        userInfo.put("phone", user.getPhone());
        userInfo.put("balance", user.getBalance());
        userInfo.put("role", user.getRole());
        userInfo.put("status", user.getStatus());
        return userInfo;
    }
}
