package com.qk.tangren.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qk.tangren.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
