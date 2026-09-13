// ============================================
// 学生端（C 端）API 模块
// ============================================
// 说明：后端接口未就绪时使用 Mock；就绪后逐步替换为 httpRequest。
// 后端统一响应：{ httpCode, bizCode, message, data, timestamp }
// httpRequest<T>() 内部已校验 bizCode === SUCCESS 并剥壳返回 data。
//
// ============ 真实后端接口约定 ============
//  GET /health/dashboard     → data: StudentDashboardRaw
//  GET /fitness/records      → data: TestRecordRaw[]
//  GET /fitness/records/{id} → data: TestDetailRaw
//  GET /health/diagnosis     → data: DiagnosisReportRaw
// ============================================

import type {
  DiagnosisReport,
  DiagnosisReportRaw,
  FitnessItemScore,
  StudentDashboard,
  StudentDashboardRaw,
  StudentInfo,
  TestDetail,
  TestDetailRaw,
  TestRecord,
  TestRecordRaw,
} from '@/types/student'
import { httpRequest } from '@/utils/request.ts'

/* ==================== 蛇形 → 驼峰适配 ==================== */
// 说明：后端返回 snake_case，前端组件使用 camelCase。
// 映射只发生在 API 层，组件永远只见驼峰字段。

/** 体测记录：Raw → 前端模型 */
function mapRecord(raw: TestRecordRaw): TestRecord {
  return {
    recordId: raw.record_id,
    testTime: raw.test_time,
    testRound: raw.test_round,
    totalScore: raw.total_score,
    level: raw.level as TestRecord['level'],
  }
}

/** 体测详情：Raw → 前端模型 */
function mapDetail(raw: TestDetailRaw): TestDetail {
  return {
    recordId: raw.record_id,
    student: {
      studentNo: raw.student.student_no,
      name: raw.student.name,
      className: raw.student.class_name,
    },
    testTime: raw.test_time,
    testRound: raw.test_round,
    totalScore: raw.total_score,
    level: raw.level as TestDetail['level'],
    items: raw.items.map(
      (it): FitnessItemScore => ({
        itemCode: it.item_code,
        itemName: it.item_name,
        unit: it.unit,
        itemValue: it.item_value,
        score: it.score,
        bonus: it.bonus,
      }),
    ),
    summary: raw.summary
      ? {
        physiqueType: raw.summary.physique_type,
        kValue: raw.summary.k_value,
        sportPrescription: raw.summary.sport_prescription,
      }
      : { physiqueType: '', kValue: 0, sportPrescription: [] },
  }
}

/** 仪表盘：Raw → 前端模型 */
function mapDashboard(raw: StudentDashboardRaw): StudentDashboard {
  const student: StudentInfo = {
    userId: raw.student.user_id,
    studentNo: raw.student.student_no,
    name: raw.student.name,
    gender: raw.student.gender as 1 | 2,
    className: raw.student.class_name,
    college: raw.student.college,
    enrollYear: raw.student.enroll_year,
  }
  return {
    student,
    latestTest: raw.latest_test ? mapRecord(raw.latest_test) : null,
    indicators: {
      height: raw.indicators.height,
      weight: raw.indicators.weight,
      bmi: raw.indicators.bmi,
      vitalCapacity: raw.indicators.vital_capacity,
    },
  }
}

/** 诊断报告：Raw → 前端模型 */
function mapDiagnosis(raw: DiagnosisReportRaw): DiagnosisReport {
  return {
    recordId: raw.record_id,
    generateTime: raw.generate_time,
    totalScore: raw.total_score,
    level: raw.level as DiagnosisReport['level'],
    kValue: raw.k_value,
    physiqueType: raw.physique_type,
    sportPrescription: raw.sport_prescription,
    healthRisks: raw.health_risks.map((r) => ({
      title: r.title,
      severity: r.severity as 'high' | 'medium' | 'low',
      risk: r.risk,
      suggestion: r.suggestion,
    })),
  }
}

/* ==================== API 方法 ==================== */

/**
 * 学生首页仪表盘数据
 *
 * 【真实接口】GET /health/dashboard
 * @returns StudentDashboard
 */
export async function getDashboard(): Promise<StudentDashboard> {
  // 后端就绪后替换为：
  const raw = await httpRequest<StudentDashboardRaw>({ method: 'GET', url: '/health/dashboard' })
  return mapDashboard(raw)
}

/**
 * 学生历史体测列表（按时间倒序）
 *
 * 【真实接口】GET /fitness/records
 * @returns TestRecord[]
 */
export async function getHistoryList(): Promise<TestRecord[]> {
  // 后端就绪后替换为：
  const raw = await httpRequest<TestRecordRaw[]>({ method: 'GET', url: '/fitness/records' })
  return raw.map(mapRecord)
}

/**
 * 某次体测的完整详情（项目细分得分 + 综合诊断结论）
 *
 * 【真实接口】GET /fitness/records/{recordId}
 * @param recordId 体测记录 ID
 * @returns TestDetail
 */
export async function getHistoryDetail(recordId: number): Promise<TestDetail> {
  const raw = await httpRequest<TestDetailRaw>({ method: 'GET', url: `/fitness/records/${recordId}` })
  return mapDetail(raw)
}

/**
 * 学生诊断报告（K 值 / 体质类型 / 运动处方 / 健康风险）
 *
 * 【真实接口】GET /health/diagnosis
 * @returns DiagnosisReport
 */
export async function getDiagnosis(): Promise<DiagnosisReport> {
  const raw = await httpRequest<DiagnosisReportRaw>({ method: 'GET', url: '/health/diagnosis' })
  return mapDiagnosis(raw)
}
