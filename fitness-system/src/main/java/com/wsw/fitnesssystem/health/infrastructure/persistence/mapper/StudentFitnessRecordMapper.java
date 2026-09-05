package com.wsw.fitnesssystem.health.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.health.infrastructure.persistence.entity.StudentFitnessRecordPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生体测记录主表 Mapper
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
@Mapper
public interface StudentFitnessRecordMapper extends BaseMapper<StudentFitnessRecordPo> {
}
