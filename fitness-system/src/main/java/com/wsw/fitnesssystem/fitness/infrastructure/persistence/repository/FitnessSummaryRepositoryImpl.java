package com.wsw.fitnesssystem.fitness.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsw.fitnesssystem.fitness.domain.model.StudentFitnessSummary;
import com.wsw.fitnesssystem.fitness.domain.port.FitnessSummaryRepository;
import com.wsw.fitnesssystem.fitness.infrastructure.persistence.converter.StudentFitnessSummaryConverter;
import com.wsw.fitnesssystem.fitness.infrastructure.persistence.entity.StudentFitnessSummaryPo;
import com.wsw.fitnesssystem.fitness.infrastructure.persistence.mapper.StudentFitnessSummaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 学生体测汇总仓储实现（按 userId upsert）
 * <p>高并发首插冲突由 DuplicateKeyException 兜底：冲突时降级为更新。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class FitnessSummaryRepositoryImpl implements FitnessSummaryRepository {

    private final StudentFitnessSummaryMapper summaryMapper;
    private final StudentFitnessSummaryConverter summaryConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertByUserId(StudentFitnessSummary summary) {
        if (summary.getUserId() == null) {
            throw new IllegalArgumentException("summary.userId must not be null");
        }

        StudentFitnessSummaryPo existing = summaryMapper.selectOne(
            new LambdaQueryWrapper<StudentFitnessSummaryPo>()
                .eq(StudentFitnessSummaryPo::getUserId, summary.getUserId())
                .eq(StudentFitnessSummaryPo::getDeleted, 0)
                .last("LIMIT 1")
        );

        StudentFitnessSummaryPo po = summaryConverter.toPo(summary);

        try {
            if (existing != null) {
                // 覆盖为最近一次体测
                po.setSummaryId(existing.getSummaryId());
                summaryMapper.updateById(po);
                summary.setSummaryId(existing.getSummaryId());
            } else {
                summaryMapper.insert(po);
                summary.setSummaryId(po.getSummaryId());
            }
        } catch (DuplicateKeyException e) {
            // 并发首插：唯一键冲突时改为更新（幂等兜底）
            log.warn("Summary insert raced for userId={}, fallback to update", summary.getUserId());
            StudentFitnessSummaryPo raced = summaryMapper.selectOne(
                new LambdaQueryWrapper<StudentFitnessSummaryPo>()
                    .eq(StudentFitnessSummaryPo::getUserId, summary.getUserId())
                    .eq(StudentFitnessSummaryPo::getDeleted, 0)
                    .last("LIMIT 1")
            );
            if (raced != null) {
                po.setSummaryId(raced.getSummaryId());
                summaryMapper.updateById(po);
                summary.setSummaryId(raced.getSummaryId());
            } else {
                throw e;
            }
        }
    }

}
