package com.wsw.fitnesssystem.iam.authentication.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.iam.authentication.infrastructure.persistence.entity.SysUserPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/16 14:58
 * @since 1.0
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserPo> {
}
