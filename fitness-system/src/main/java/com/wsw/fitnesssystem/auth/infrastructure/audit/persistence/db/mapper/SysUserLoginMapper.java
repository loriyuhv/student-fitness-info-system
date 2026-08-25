package com.wsw.fitnesssystem.auth.infrastructure.audit.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.auth.infrastructure.audit.persistence.db.entity.SysUserLogin;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/15 21:57
 * @since 1.0
 */
@Mapper
public interface SysUserLoginMapper extends BaseMapper<SysUserLogin> {
}
