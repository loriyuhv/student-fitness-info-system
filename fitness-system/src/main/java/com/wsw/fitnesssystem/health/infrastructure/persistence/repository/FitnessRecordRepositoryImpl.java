package com.wsw.fitnesssystem.health.infrastructure.persistence.repository;

import com.wsw.fitnesssystem.health.domain.model.StudentFitnessRecord;
import com.wsw.fitnesssystem.health.domain.model.StudentFitnessRecordItem;
import com.wsw.fitnesssystem.health.domain.port.FitnessRecordRepository;
import com.wsw.fitnesssystem.health.infrastructure.persistence.converter.StudentFitnessRecordConverter;
import com.wsw.fitnesssystem.health.infrastructure.persistence.converter.StudentFitnessRecordItemConverter;
import com.wsw.fitnesssystem.health.infrastructure.persistence.entity.StudentFitnessRecordItemPo;
import com.wsw.fitnesssystem.health.infrastructure.persistence.entity.StudentFitnessRecordPo;
import com.wsw.fitnesssystem.health.infrastructure.persistence.mapper.StudentFitnessRecordItemMapper;
import com.wsw.fitnesssystem.health.infrastructure.persistence.mapper.StudentFitnessRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 体测记录仓储实现
 * <p>落库顺序：先插主表拿到 recordId，再插明细，保证明细外键可用。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
@Repository
@RequiredArgsConstructor
public class FitnessRecordRepositoryImpl implements FitnessRecordRepository {

    private final StudentFitnessRecordMapper recordMapper;
    private final StudentFitnessRecordItemMapper itemMapper;
    private final StudentFitnessRecordConverter studentFitnessRecordConverter;
    private final StudentFitnessRecordItemConverter itemConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentFitnessRecord save(StudentFitnessRecord record) {
        // 1. 主表
        StudentFitnessRecordPo po = studentFitnessRecordConverter.toPo(record);
        recordMapper.insert(po);
        record.setRecordId(po.getRecordId());

        // 2. 明细
        List<StudentFitnessRecordItem> items = record.getItems();
        if (items == null || items.isEmpty()) {
            record.setItems(Collections.emptyList());
            return record;
        }

        for (StudentFitnessRecordItem item : items) {
            StudentFitnessRecordItemPo itemPo = itemConverter.toPo(item);
            itemPo.setRecordId(po.getRecordId());
            itemMapper.insert(itemPo);
            item.setId(itemPo.getId());
        }
        return record;
    }

}
