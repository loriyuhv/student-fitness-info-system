// ============================================
// 全局类型定义
// ============================================

// 用户角色
export enum Role {
  ADMIN = 'ADMIN',
  STUDENT = 'STUDENT',
  TEACHER = 'TEACHER',
}

// 用户身份
export enum UserType {
  ADMIN = 0,
  TEACHER = 1,
  STUDENT = 2,
}

// 性别
export enum Gender {
  UNKNOWN = 0,
  MALE = 1,
  FEMALE = 2,
}

// 评分等级
export type ScoreLevel = '优秀' | '良好' | '及格' | '不及格'

// 体重等级
export type WeightLevel = '偏瘦' | '正常' | '超重' | '肥胖'

// 体质测评记录接口
export interface AssessmentRecord {
  id: string
  userAccount: string
  testTime: string | number // 时间戳或日期字符串
  height: number // 身高(cm)
  weight: number // 体重(kg)
  weightLevel: WeightLevel

  // 各项测试分数
  vitalCapacityScore: number // 肺活量
  run50mScore: number // 50米跑
  standLongJumpScore: number // 立定跳远
  sitReachScore: number // 坐位体前屈
  run1000mScore?: number // 1000米跑（男）
  pullUpScore?: number // 引体向上（男）
  run800mScore?: number // 800米跑（女）
  sitUpScore?: number // 仰卧起坐（女）

  // 总分和结果
  totalScore: number
  physicalFitnessResult?: string
  sportPrescription?: string // 运动处方ID
}

// 面包屑项接口
export interface BreadcrumbItem {
  path: string
  meta: {
    title: string
  }
}

// 路由元信息接口
export interface RouteMeta {
  title?: string
  requiresAuth?: boolean
  permissions?: string[]
  [key: PropertyKey]: unknown
}

/* ============导出========= */

// API相关
export type { ApiResponse, PageParams, PageResult } from './api'

// 用户相关
export type {
  LoginForm,
  UserInfo,
  LoginResponse,
} from './user'

// 状态码相关
export type { ResultCode, TOKEN_ERROR_CODES } from './result-code'
