package com.wsw.fitnesssystem.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.user.infrastructure.persistence.entity.StudentProfilePo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/2 08:52
 * @since 1.0
 */
@Mapper
public interface StudentProfileMapper extends BaseMapper<StudentProfilePo> {
}
