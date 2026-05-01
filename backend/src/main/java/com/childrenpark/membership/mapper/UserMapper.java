package com.childrenpark.membership.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.childrenpark.membership.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
