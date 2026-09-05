package com.wsw.fitnesssystem.health.interfaces.web.controller;

import com.wsw.fitnesssystem.health.application.dto.command.FitnessRecordSubmitCommand;
import com.wsw.fitnesssystem.health.application.dto.result.FitnessRecordSubmitResult;
import com.wsw.fitnesssystem.health.application.service.FitnessRecordSubmitService;
import com.wsw.fitnesssystem.health.interfaces.web.dto.request.FitnessRecordSubmitRequest;
import com.wsw.fitnesssystem.shared.context.RequestContextHolder;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 体测记录提交接口
 * <p>教师前端提交一条学生体测记录：学号 + 各项目原始成绩，
 * 后端完成 评分 → 记录主表/明细落库 → 汇总更新 全链路。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/5
 */
@Slf4j
@RestController
@RequestMapping("/health/fitness-records")
@RequiredArgsConstructor
public class FitnessRecordController {

    private final FitnessRecordSubmitService fitnessRecordSubmitService;

    /**
     * 提交学生体测记录
     *
     * @param request 请求体（学号 + 原始成绩）
     * @return 落库后的体测记录结果
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResult<FitnessRecordSubmitResult> submit(
        @RequestBody @Valid FitnessRecordSubmitRequest request) {

        // 操作人从安全上下文取（JWT 认证过滤器注入）
        Operator operator = RequestContextHolder.getRequiredOperator();

        FitnessRecordSubmitCommand command = FitnessRecordSubmitCommand.builder()
            .studentNo(request.getStudentNo())
            .operatorUserId(operator.userId())
            .testRound(request.getTestRound())
            .testTime(request.getTestTime())
            .rawScores(request.getRawScores())
            .build();

        FitnessRecordSubmitResult result = fitnessRecordSubmitService.submit(command);

        log.info("体测记录提交成功: recordId={}, studentNo={}, operatorUserId={}",
            result.getRecordId(), request.getStudentNo(), operator.userId());

        return ApiResult.success(result);
    }

}
