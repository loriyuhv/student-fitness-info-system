package com.wsw.fitnesssystem.health.interfaces.web;

import com.wsw.fitnesssystem.health.interfaces.web.dto.response.DiagnosisReportResponse;
import com.wsw.fitnesssystem.health.interfaces.web.dto.response.StudentDashboardResponse;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 健康视图接口（学生端首页 + 诊断报告）
 *
 * <p><b>模块边界：</b></p>
 * <ul>
 *   <li>fitness 模块：体测数据的采集、评分、存储</li>
 *   <li>health 模块：基于体测数据生成诊断、运动处方、健康视图聚合</li>
 * </ul>
 *
 * <p><b>数据权限：</b>两个接口都使用 {@code isAuthenticated()}，
 * 学生看自己、教师看本班、管理员看全部，由数据权限拦截器自动过滤。</p>
 *
 * <p><b>当前状态：</b>两个接口均返回 Mock，待
 * {@code HealthDashboardQueryService} 与 {@code DiagnosisQueryService}
 * 实现后替换。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/13 15:34
 * @since 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/health")
public class HealthController {

    // ==================================================================
    //  首页仪表盘
    // ==================================================================

    /**
     * 学生首页仪表盘
     * 待 {@code HealthDashboardQueryService#getDashboard(Operator)} 实现后替换 buildMockDashboard()
     */
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<StudentDashboardResponse> getDashboard() {
        return ApiResult.success(buildMockDashboard());
    }

    // ==================================================================
    //  诊断报告
    // ==================================================================

    /**
     * 学生诊断报告（K 值 / 体质类型 / 运动处方 / 健康风险）
     * 待 {@code DiagnosisQueryService#getLatestReport(Operator)} 实现后替换 buildMockDiagnosis()
     */
    @GetMapping("/diagnosis")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<DiagnosisReportResponse> getDiagnosis() {
        return ApiResult.success(buildMockDiagnosis());
    }

    // ==================================================================
    //  Mock 数据（与前端 student.ts 对齐，真实实现后整体删除）
    // ==================================================================

    private StudentDashboardResponse buildMockDashboard() {
        return StudentDashboardResponse.builder()
            .student(StudentDashboardResponse.Student.builder()
                .userId(1L)
                .studentNo("2023420115")
                .name("陈小宇")
                .gender(1)
                .className("计算机科学与技术2301班")
                .college("计算机学院")
                .enrollYear(2023)
                .build())
            .latestTest(StudentDashboardResponse.LatestTest.builder()
                .recordId(3L)
                .testTime("2025-05-18 09:30:00")
                .testRound(3)
                .totalScore(new BigDecimal("86.5"))
                .level("良好")
                .build())
            .indicators(StudentDashboardResponse.Indicators.builder()
                .height(new BigDecimal("175"))
                .weight(new BigDecimal("68.2"))
                .bmi(new BigDecimal("22.3"))
                .vitalCapacity(new BigDecimal("4800"))
                .build())
            .build();
    }

    private DiagnosisReportResponse buildMockDiagnosis() {
        return DiagnosisReportResponse.builder()
            .recordId(3L)
            .generateTime("2025-05-18 10:20:00")
            .totalScore(new BigDecimal("86.5"))
            .level("良好")
            .kValue(2)
            .physiqueType("匀称耐力型")
            .sportPrescription(List.of(
                "每周进行 3 次、每次 30 分钟的中高强度有氧运动（如慢跑、游泳），提升心肺耐力。",
                "加入每周 2 次的核心与上肢力量训练（平板支撑、俯卧撑、弹力带划船）。",
                "体测前 1 周减少训练量，保证充足睡眠，避免过度疲劳影响成绩。"
            ))
            .healthRisks(List.of(
                DiagnosisReportResponse.HealthRisk.builder()
                    .title("心肺耐力")
                    .severity("medium")
                    .risk("1000米成绩处于中等区间，心肺耐力仍有提升空间。")
                    .suggestion("每周安排 2 次持续 20 分钟以上的中等强度跑步。")
                    .build(),
                DiagnosisReportResponse.HealthRisk.builder()
                    .title("上肢肌力")
                    .severity("medium")
                    .risk("引体向上处于良好边缘，上肢力量储备一般。")
                    .suggestion("以弹力带辅助引体或负重悬垂渐进提升。")
                    .build(),
                DiagnosisReportResponse.HealthRisk.builder()
                    .title("体重管理")
                    .severity("low")
                    .risk("BMI 处于正常区间，暂无明显风险。")
                    .suggestion("保持均衡饮食与每周 3 次运动即可。")
                    .build()
            ))
            .build();
    }

}
