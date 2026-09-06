// ============================================
// 学生端（C 端）类型定义
// 与后端 health 模块「学生端接口」对齐
// ============================================

/** 体测等级（与后端 total_level 文案一致） */
export type TestLevel = '优秀' | '良好' | '及格' | '不及格'

/** 学生基础档案信息 */
export interface StudentInfo {
  userId: number
  /** 学号 */
  studentNo: string
  name: string
  /** 性别：1-男 2-女 */
  gender: 1 | 2
  className: string
  college: string
  enrollYear: number
}

/** 历史列表中的一条体测记录（摘要） */
export interface TestRecord {
  recordId: number
  /** 体测时间，格式 yyyy-MM-dd HH:mm:ss */
  testTime: string
  /** 第几次体测 */
  testRound: number
  totalScore: number
  level: TestLevel
}

/** 仪表盘关键指标摘要 */
export interface DashboardIndicators {
  /** 身高 cm */
  height: number
  /** 体重 kg */
  weight: number
  /** BMI 指数 */
  bmi: number
  /** 肺活量 ml */
  vitalCapacity: number
}

/** 仪表盘数据 */
export interface StudentDashboard {
  student: StudentInfo
  /** 最近一次体测（从未体测时为 null） */
  latestTest: TestRecord | null
  indicators: DashboardIndicators
}

/** 某次体测中的单个项目细分得分 */
export interface FitnessItemScore {
  /** 项目编码（如 BMI / VITAL_CAPACITY / RUN_1000_800） */
  itemCode: string
  itemName: string
  /** 项目原始成绩 */
  itemValue: number
  unit: string
  /** 该项目得分 */
  score: number
  /** 附加分（无附加为 0） */
  bonus: number
}

/** 综合诊断简版（详情页使用） */
export interface DiagnosisSummary {
  /** 体质类型 */
  physiqueType: string
  /** K-means 聚类值 */
  kValue: number
  /** 运动处方（多条建议） */
  sportPrescription: string[]
}

/** 某次体测完整详情 */
export interface TestDetail {
  recordId: number
  student: Pick<StudentInfo, 'studentNo' | 'name' | 'className'>
  testTime: string
  testRound: number
  totalScore: number
  level: TestLevel
  /** 各项目细分得分 */
  items: FitnessItemScore[]
  /** 当次综合诊断结论 */
  summary: DiagnosisSummary
}

/** 健康风险提示 */
export interface HealthRisk {
  title: string
  /** 风险级别：high-高 / medium-中 / low-低 */
  severity: 'high' | 'medium' | 'low'
  risk: string
  suggestion: string
}

/** 诊断报告（运动处方） */
export interface DiagnosisReport {
  /** 关联的体测记录 ID */
  recordId: number
  /** 报告生成时间 */
  generateTime: string
  totalScore: number
  level: TestLevel
  /** K-means 聚类值 */
  kValue: number
  /** 体质类型 */
  physiqueType: string
  /** 运动处方（多条建议） */
  sportPrescription: string[]
  /** 健康风险提示 */
  healthRisks: HealthRisk[]
}
