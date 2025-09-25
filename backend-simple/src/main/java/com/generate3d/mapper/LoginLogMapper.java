package com.generate3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generate3d.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志数据访问层
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}