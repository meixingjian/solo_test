package com.childrenpark.membership.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.childrenpark.membership.context.UserContext;
import com.childrenpark.membership.entity.User;
import com.childrenpark.membership.mapper.UserMapper;
import com.childrenpark.membership.service.UserService;
import com.childrenpark.membership.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${wechat.miniapp.app-id}")
    private String appId;

    @Value("${wechat.miniapp.app-secret}")
    private String appSecret;

    private static final String WECHAT_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Override
    public User getByOpenid(String openid) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(String openid, String nickName, String avatarUrl, Integer gender) {
        User user = new User();
        user.setOpenid(openid);
        user.setNickName(nickName != null ? nickName : "微信用户");
        user.setAvatarUrl(avatarUrl);
        user.setGender(gender != null ? gender : 0);
        user.setBalance(BigDecimal.ZERO);
        user.setRole("user");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);
        save(user);
        return user;
    }

    @Override
    public User loginByCode(String code) throws Exception {
        String openid = getOpenidFromWechat(code);
        
        if (openid == null || openid.isEmpty()) {
            throw new Exception("微信登录失败，无法获取openid");
        }

        User user = getByOpenid(openid);
        
        if (user == null) {
            user = createUser(openid, null, null, null);
        }

        String token = jwtUtils.generateToken(user.getId(), user.getRole());
        user.setToken(token);
        
        return user;
    }

    @Override
    public User getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return null;
        }
        return getById(userId);
    }

    private String getOpenidFromWechat(String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("appid", appId);
        params.put("secret", appSecret);
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");

        try {
            String response = HttpUtil.get(WECHAT_LOGIN_URL, params);
            JSONObject json = JSON.parseObject(response);
            
            if (json.containsKey("errcode") && json.getInteger("errcode") != 0) {
                log.error("微信登录失败: {}", json.getString("errmsg"));
                return null;
            }
            
            return json.getString("openid");
        } catch (Exception e) {
            log.error("调用微信登录接口失败", e);
            return null;
        }
    }
}
