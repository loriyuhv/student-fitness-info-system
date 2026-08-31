package com.wsw.fitnesssystem.handle_excel.core.helper;

import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollector;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * BatchPersistHelper 单元测试
 *
 * @author loriyuhv
 * @version 1.0 2026/8/31 10:33
 * @since 1.0
 */
@DisplayName("BatchPersistHelper 单元测试")
class BatchPersistHelperTest {

    @InjectMocks
    private BatchPersistHelper helper;

    private ErrorCollector collector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        collector = new ErrorCollector();
    }

    // ============================================================
    // 场景1：所有数据正常插入（批量成功）
    // ============================================================
    @Test
    @DisplayName("正常场景：全部批量插入成功")
    void shouldReturnTotalCount_whenAllBatchInsertSuccess() {
        // 准备数据
        List<String> entities = List.of("A", "B", "C", "D", "E");

        // Mock：批量插入返回传入的数量
        @SuppressWarnings("unchecked")
        Function<List<String>, Integer> batchInsertFunc = mock(Function.class);
        when(batchInsertFunc.apply(anyList())).thenAnswer(invocation -> {
            List<String> list = invocation.getArgument(0);
            return list.size();
        });

        // 单条插入不应该被调用
        @SuppressWarnings("unchecked")
        Function<String, Integer> singleInsertFunc = mock(Function.class);

        // 执行
        int result = helper.safeBatchInsert(
            entities,
            batchInsertFunc,
            singleInsertFunc,
            2,  // batchSize = 2，预期分 3 批
            collector
        );

        // 验证
        assertThat(result).isEqualTo(5);
        assertThat(collector.hasErrors()).isFalse();
        verify(batchInsertFunc, times(3)).apply(anyList());
        verify(singleInsertFunc, never()).apply(any());
    }

    // ============================================================
    // 场景2：大块批量插入失败，降级为小组插入
    // ============================================================
    @Test
    @DisplayName("降级场景：大块失败 → 小组成功")
    void shouldFallbackToSubBatch_whenBigBatchFails() {
        // 准备数据
        List<String> entities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entities.add("Item" + i);
        }

        // Mock：大块批量插入失败，小组插入成功
        @SuppressWarnings("unchecked")
        Function<List<String>, Integer> batchInsertFunc = mock(Function.class);
        // 大块（batchSize=5）会分成2批
        when(batchInsertFunc.apply(anyList()))
            .thenThrow(new DataIntegrityViolationException("Batch failed")) // 第一批大块失败
            .thenThrow(new DataIntegrityViolationException("Batch failed")); // 第二批大块失败

        @SuppressWarnings("unchecked")
        Function<String, Integer> singleInsertFunc = mock(Function.class);
        when(singleInsertFunc.apply(anyString())).thenReturn(1);

        // 执行：batchSize=5，大块分2批，每批大块失败后拆小组（50条/组，实际只有5条）
        int result = helper.safeBatchInsert(
            entities,
            batchInsertFunc,
            singleInsertFunc,
            5,
            collector
        );

        // 验证：所有10条都成功插入（通过单条插入）
        assertThat(result).isEqualTo(10);
        assertThat(collector.hasErrors()).isFalse();
        verify(batchInsertFunc, times(4)).apply(anyList());
        verify(singleInsertFunc, times(10)).apply(anyString());
    }

    // ============================================================
    // 场景3：小组插入失败，降级为逐条插入（部分成功）
    // ============================================================
    @Test
    @DisplayName("降级场景：小组失败 → 逐条插入，部分成功")
    void shouldFallbackToSingleInsert_whenSubBatchFails() {
        // 准备数据
        List<String> entities = List.of("A", "B", "C");

        // Mock：大块失败，小组也失败
        @SuppressWarnings("unchecked")
        Function<List<String>, Integer> batchInsertFunc = mock(Function.class);
        when(batchInsertFunc.apply(anyList()))
            .thenThrow(new DataIntegrityViolationException("Batch failed")); // 大块失败

        @SuppressWarnings("unchecked")
        Function<String, Integer> singleInsertFunc = mock(Function.class);
        when(singleInsertFunc.apply("A")).thenReturn(1);
        when(singleInsertFunc.apply("B")).thenThrow(new DuplicateKeyException("Duplicate key: B"));
        when(singleInsertFunc.apply("C")).thenReturn(1);

        // 执行
        int result = helper.safeBatchInsert(
            entities,
            batchInsertFunc,
            singleInsertFunc,
            10,  // batchSize=10，只有1批大块
            collector
        );

        // 验证：成功插入2条（A和C），B失败被记录
        assertThat(result).isEqualTo(2);
        assertThat(collector.hasErrors()).isTrue();
        assertThat(collector.getErrorCount()).isEqualTo(1);
        assertThat(collector.getErrors().get(0).getErrorReason())
            .contains("Duplicate key: B");

        verify(batchInsertFunc, times(2)).apply(anyList());
        verify(singleInsertFunc, times(3)).apply(anyString());
    }

    // ============================================================
    // 场景4：空列表或 null 输入
    // ============================================================
    @Test
    @DisplayName("边界场景：空列表输入")
    void shouldReturnZero_whenEntitiesIsEmpty() {
        @SuppressWarnings("unchecked")
        Function<List<String>, Integer> batchInsertFunc = mock(Function.class);
        @SuppressWarnings("unchecked")
        Function<String, Integer> singleInsertFunc = mock(Function.class);

        int result = helper.safeBatchInsert(
            List.of(),
            batchInsertFunc,
            singleInsertFunc,
            10,
            collector
        );

        assertThat(result).isEqualTo(0);
        assertThat(collector.hasErrors()).isFalse();
        verify(batchInsertFunc, never()).apply(any());
        verify(singleInsertFunc, never()).apply(any());
    }

    @Test
    @DisplayName("边界场景：null 输入")
    void shouldReturnZero_whenEntitiesIsNull() {
        @SuppressWarnings("unchecked")
        Function<List<String>, Integer> batchInsertFunc = mock(Function.class);
        @SuppressWarnings("unchecked")
        Function<String, Integer> singleInsertFunc = mock(Function.class);

        int result = helper.safeBatchInsert(
            null,
            batchInsertFunc,
            singleInsertFunc,
            10,
            collector
        );

        assertThat(result).isEqualTo(0);
        assertThat(collector.hasErrors()).isFalse();
        verify(batchInsertFunc, never()).apply(any());
        verify(singleInsertFunc, never()).apply(any());
    }


    // ============================================================
    // 场景5：未知异常（非 DataIntegrityViolationException）
    // ============================================================
    @Test
    @DisplayName("异常场景：大块插入抛出未知异常")
    void shouldRecordError_whenUnknownExceptionOccurred() {
        List<String> entities = List.of("A", "B", "C");

        @SuppressWarnings("unchecked")
        Function<List<String>, Integer> batchInsertFunc = mock(Function.class);
        when(batchInsertFunc.apply(anyList()))
            .thenThrow(new RuntimeException("Unknown system error"));

        @SuppressWarnings("unchecked")
        Function<String, Integer> singleInsertFunc = mock(Function.class);

        int result = helper.safeBatchInsert(
            entities,
            batchInsertFunc,
            singleInsertFunc,
            10,
            collector
        );

        // 验证：全部失败，3条错误记录
        assertThat(result).isEqualTo(0);
        assertThat(collector.hasErrors()).isTrue();
        assertThat(collector.getErrorCount()).isEqualTo(3);

        assertThat(collector.getErrors().get(0).getErrorReason())
            .contains("系统异常：Unknown system error");

        verify(batchInsertFunc, times(1)).apply(anyList());
        verify(singleInsertFunc, never()).apply(any());
    }

    // ============================================================
    // 场景6：自定义行号提取（通过 entity 的 getRowIndex）
    // ============================================================
    @Test
    @DisplayName("错误记录：包含行号信息")
    void shouldIncludeRowIndex_whenEntityHasRowIndex() {
        // 使用模拟实体（假设有 getRowIndex 方法）
        @Getter
        @AllArgsConstructor
        @SuppressWarnings("ClassCanBeRecord")
        class TestEntity {
            private final String value;
            private final int rowIndex;
        }

        List<TestEntity> entities = List.of(
            new TestEntity("A", 5),
            new TestEntity("B", 10)
        );

        @SuppressWarnings("unchecked")
        Function<List<TestEntity>, Integer> batchInsertFunc = mock(Function.class);
        when(batchInsertFunc.apply(anyList()))
            .thenThrow(new DataIntegrityViolationException("Batch failed"));

        @SuppressWarnings("unchecked")
        Function<TestEntity, Integer> singleInsertFunc = mock(Function.class);
        when(singleInsertFunc.apply(any(TestEntity.class)))
            .thenThrow(new DuplicateKeyException("Duplicate key"));

        int result = helper.safeBatchInsert(
            entities,
            batchInsertFunc,
            singleInsertFunc,
            10,
            collector
        );

        assertThat(result).isEqualTo(0);
        assertThat(collector.getErrorCount()).isEqualTo(2);
        // 验证行号是否正确提取
        assertThat(collector.getErrors().get(0).getRowIndex()).isEqualTo(5);
        assertThat(collector.getErrors().get(1).getRowIndex()).isEqualTo(10);
    }

}