package com.childrenpark.membership.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.childrenpark.membership.entity.User;

public interface UserService extends IService<User> {

    User getByOpenid(String openid);

    User createUser(String openid, String nickName, String avatarUrl, Integer gender);

    User loginByCode(String code) throws Exception;

    User getCurrentUser();
}
