package com.wsw.fitnesssystem.fitness.infrastructure.persistence.converter;

import com.wsw.fitnesssystem.fitness.domain.model.StudentFitnessRecordItem;
import com.wsw.fitnesssystem.fitness.infrastructure.persistence.entity.StudentFitnessRecordItemPo;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/5 18:46
 * @since 1.0
 */
@Component
public class StudentFitnessRecordItemConverter {

    public StudentFitnessRecordItemPo toPo(StudentFitnessRecordItem item) {
        StudentFitnessRecordItemPo po = new StudentFitnessRecordItemPo();
        po.setId(item.getId());
        po.setRecordId(item.getRecordId());
        po.setItemId(item.getItemId());
        po.setItemValue(item.getItemValue());
        po.setScore(item.getScore() != null ? java.math.BigDecimal.valueOf(item.getScore()) : null);
        po.setDeleted(-1);
        return po;
    }

}
