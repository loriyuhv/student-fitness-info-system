package com.wsw.fitnesssystem.fitness.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.fitness.infrastructure.persistence.entity.StudentFitnessRecordItemPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生体测记录明细 Mapper
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
@Mapper
public interface StudentFitnessRecordItemMapper extends BaseMapper<StudentFitnessRecordItemPo> {
}
