// ============================================
// 学生端（C 端）API 模块
// ============================================
// 说明：后端接口未就绪，以下函数当前返回 Mock 数据，保证页面可立即渲染。
// 待后端就绪后：删除 Mock 数据，将函数体替换为 httpRequest 调用即可。
//
// 后端统一响应：{ httpCode, bizCode, message, data, timestamp }
// httpRequest<T>() 内部已校验 bizCode === SUCCESS 并剥壳返回 data。
//
// ============ 真实后端接口约定（实现时替换） ============
// 建议后端路径（context 前缀 /api 由 Vite 代理自动处理）：
//  GET /health/student/dashboard         → data: StudentDashboard
//  GET /health/student/records           → data: TestRecord[]
//  GET /health/student/records/{id}      → data: TestDetail
//  GET /health/student/diagnosis         → data: DiagnosisReport
// ============================================

import type {
  DiagnosisReport,
  FitnessItemScore,
  StudentDashboard,
  StudentInfo,
  TestDetail,
  TestRecord,
} from '@/types/student'

const mockDelay = () => new Promise<void>((resolve) => setTimeout(resolve, 350))

/* ==================== Mock 数据 ==================== */

const MOCK_STUDENT: StudentInfo = {
  userId: 1,
  studentNo: '2023420115',
  name: '陈小宇',
  gender: 1,
  className: '计算机科学与技术2301班',
  college: '计算机学院',
  enrollYear: 2023,
}

// 按时间倒序：最新在前
const MOCK_RECORDS: TestRecord[] = [
  { recordId: 3, testTime: '2025-05-18 09:30:00', testRound: 3, totalScore: 86.5, level: '良好' },
  { recordId: 2, testTime: '2024-11-09 10:20:00', testRound: 2, totalScore: 74.2, level: '及格' },
  { recordId: 1, testTime: '2024-05-22 14:00:00', testRound: 1, totalScore: 91.8, level: '优秀' },
]

// 项目元信息（顺序即明细展示顺序）
const ITEM_META: Array<{ itemCode: string; itemName: string; unit: string }> = [
  { itemCode: 'BMI', itemName: '体重指数（BMI）', unit: 'kg/m²' },
  { itemCode: 'VITAL_CAPACITY', itemName: '肺活量', unit: 'ml' },
  { itemCode: '50M', itemName: '50米跑', unit: 's' },
  { itemCode: 'SIT_AND_REACH', itemName: '坐位体前屈', unit: 'cm' },
  { itemCode: 'STANDING_LONG_JUMP', itemName: '立定跳远', unit: 'cm' },
  { itemCode: 'PULL_UP', itemName: '引体向上（男）', unit: '次' },
  { itemCode: 'RUN_1000_800', itemName: '1000米跑（男）', unit: 's' },
]

/** 由成绩/原始值批量构造明细（Mock 用，后端由评分引擎产出） */
function buildItems(
  scores: number[],
  values: number[],
  bonus: number[] = scores.map(() => 0),
): FitnessItemScore[] {
  return ITEM_META.map((meta, i) => ({
    ...meta,
    itemValue: values[i] ?? 0,
    score: scores[i] ?? 0,
    bonus: bonus[i] ?? 0,
  }))
}

const MOCK_DETAILS: Record<number, TestDetail> = {
  3: {
    recordId: 3,
    student: { studentNo: MOCK_STUDENT.studentNo, name: MOCK_STUDENT.name, className: MOCK_STUDENT.className },
    testTime: '2025-05-18 09:30:00',
    testRound: 3,
    totalScore: 86.5,
    level: '良好',
    items: buildItems(
      [100, 90, 90, 84, 76, 80, 84],
      [22.3, 4800, 6.9, 17.5, 235, 12, 232],
      [0, 0, 0, 0, 0, 1, 0],
    ),
    summary: {
      physiqueType: '匀称耐力型',
      kValue: 2,
      sportPrescription: ['每周 3 次、每次 30 分钟中高强度有氧跑', '加强核心力量训练，每周 2 次平板支撑'],
    },
  },
  2: {
    recordId: 2,
    student: { studentNo: MOCK_STUDENT.studentNo, name: MOCK_STUDENT.name, className: MOCK_STUDENT.className },
    testTime: '2024-11-09 10:20:00',
    testRound: 2,
    totalScore: 74.2,
    level: '及格',
    items: buildItems(
      [60, 72, 64, 68, 62, 60, 74],
      [24.6, 3520, 8.9, 9.2, 208, 7, 243],
      [0, 0, 0, 0, 0, 0, 0],
    ),
    summary: {
      physiqueType: '偏弱肌力型',
      kValue: 4,
      sportPrescription: ['增加抗阻训练：每周 2 次俯卧撑/哑铃', '耐力跑前先做 5 分钟动态热身'],
    },
  },
  1: {
    recordId: 1,
    student: { studentNo: MOCK_STUDENT.studentNo, name: MOCK_STUDENT.name, className: MOCK_STUDENT.className },
    testTime: '2024-05-22 14:00:00',
    testRound: 1,
    totalScore: 91.8,
    level: '优秀',
    items: buildItems(
      [100, 100, 95, 92, 90, 95, 88],
      [21.4, 5060, 6.5, 20.6, 262, 18, 226],
      [0, 0, 0, 0, 0, 1, 1],
    ),
    summary: {
      physiqueType: '强健均衡型',
      kValue: 1,
      sportPrescription: ['保持当前运动习惯，每周 2 次高强度间歇训练', '注意运动后的拉伸与恢复'],
    },
  },
}

const MOCK_DIAGNOSIS: DiagnosisReport = {
  recordId: 3,
  generateTime: '2025-05-18 10:20:00',
  totalScore: 86.5,
  level: '良好',
  kValue: 2,
  physiqueType: '匀称耐力型',
  sportPrescription: [
    '每周进行 3 次、每次 30 分钟的中高强度有氧运动（如慢跑、游泳），提升心肺耐力。',
    '加入每周 2 次的核心与上肢力量训练（平板支撑、俯卧撑、弹力带划船）。',
    '体测前 1 周减少训练量，保证充足睡眠，避免过度疲劳影响成绩。',
  ],
  healthRisks: [
    {
      title: '心肺耐力',
      severity: 'medium',
      risk: '1000米成绩处于中等区间，心肺耐力仍有提升空间。',
      suggestion: '每周安排 2 次持续 20 分钟以上的中等强度跑步。',
    },
    {
      title: '上肢肌力',
      severity: 'medium',
      risk: '引体向上处于良好边缘，上肢力量储备一般。',
      suggestion: '以弹力带辅助引体或负重悬垂渐进提升。',
    },
    {
      title: '体重管理',
      severity: 'low',
      risk: 'BMI 处于正常区间，暂无明显风险。',
      suggestion: '保持均衡饮食与每周 3 次运动即可。',
    },
  ],
}

/* ==================== API 方法 ==================== */

/**
 * 学生首页仪表盘数据
 *
 * 【真实接口】GET /health/student/dashboard
 * @returns StudentDashboard
 */
export async function getDashboard(): Promise<StudentDashboard> {
  // 后端就绪后替换为：
  // return httpRequest<StudentDashboard>({ method: 'GET', url: '/health/student/dashboard' })
  await mockDelay()
  return {
    student: MOCK_STUDENT,
    latestTest: MOCK_RECORDS[0] ?? null,
    indicators: { height: 175, weight: 68.2, bmi: 22.3, vitalCapacity: 4800 },
  }
}

/**
 * 学生历史体测列表（按时间倒序）
 *
 * 【真实接口】GET /health/student/records
 * @returns TestRecord[]
 */
export async function getHistoryList(): Promise<TestRecord[]> {
  // 后端就绪后替换为：
  // return httpRequest<TestRecord[]>({ method: 'GET', url: '/health/student/records' })
  await mockDelay()
  return MOCK_RECORDS
}

/**
 * 某次体测的完整详情（项目细分得分 + 综合诊断结论）
 *
 * 【真实接口】GET /health/student/records/{recordId}
 * @param recordId 体测记录 ID
 * @returns TestDetail
 */
export async function getHistoryDetail(recordId: number): Promise<TestDetail> {
  // 后端就绪后替换为：
  // return httpRequest<TestDetail>({ method: 'GET', url: `/health/student/records/${recordId}` })
  await mockDelay()
  const detail = MOCK_DETAILS[recordId]
  if (!detail) {
    throw new Error('未找到该次体测记录，可能已被删除')
  }
  return detail
}

/**
 * 学生诊断报告（K 值 / 体质类型 / 运动处方 / 健康风险）
 *
 * 【真实接口】GET /health/student/diagnosis
 * @returns DiagnosisReport
 */
export async function getDiagnosis(): Promise<DiagnosisReport> {
  // 后端就绪后替换为：
  // return httpRequest<DiagnosisReport>({ method: 'GET', url: '/health/student/diagnosis' })
  await mockDelay()
  return MOCK_DIAGNOSIS
}
