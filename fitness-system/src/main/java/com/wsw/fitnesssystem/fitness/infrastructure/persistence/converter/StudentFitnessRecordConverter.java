package com.wsw.fitnesssystem.fitness.infrastructure.persistence.converter;

import com.wsw.fitnesssystem.fitness.domain.model.StudentFitnessRecord;
import com.wsw.fitnesssystem.fitness.infrastructure.persistence.entity.StudentFitnessRecordPo;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/5 18:46
 * @since 1.0
 */
@Component
public class StudentFitnessRecordConverter {

    public StudentFitnessRecordPo toPo(StudentFitnessRecord record) {
        StudentFitnessRecordPo po = new StudentFitnessRecordPo();
        po.setRecordId(record.getRecordId());
        po.setStudentUserId(record.getStudentUserId());
        po.setOperatorUserId(record.getOperatorUserId());
        po.setTestTime(record.getTestTime());
        po.setTestRound(record.getTestRound());
        po.setTestType(record.getTestType());
        po.setTotalScore(record.getTotalScore());
        po.setTotalLevel(record.getTotalLevel());
        po.setStatus(record.getStatus());
        po.setConfirmStatus(record.getConfirmStatus());
        po.setConfirmTime(record.getConfirmTime());
        po.setRemark(record.getRemark());
        po.setDeleted(0);
        return po;
    }

}
