package com.wsw.fitnesssystem.health.infrastructure.persistence.converter;

import com.wsw.fitnesssystem.health.domain.model.StudentFitnessSummary;
import com.wsw.fitnesssystem.health.infrastructure.persistence.entity.StudentFitnessSummaryPo;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/5 18:47
 * @since 1.0
 */
@Component
public class StudentFitnessSummaryConverter {

    public StudentFitnessSummaryPo toPo(StudentFitnessSummary summary) {
        StudentFitnessSummaryPo po = new StudentFitnessSummaryPo();
        po.setSummaryId(summary.getSummaryId());
        po.setUserId(summary.getUserId());
        po.setRecordId(summary.getRecordId());
        po.setTotalScore(summary.getTotalScore());
        po.setTotalLevel(summary.getTotalLevel());
        po.setLatestTestTime(summary.getLatestTestTime());
        po.setStatus(summary.getStatus());
        po.setDeleted(0);
        return po;
    }

}
