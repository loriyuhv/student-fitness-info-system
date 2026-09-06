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
  requiresGuest?: boolean
  roles?: Role[]
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

// 状态码相关（ResultCode 是 enum、TOKEN_ERROR_CODES 是数组，必须按值导出）
export { ResultCode, TOKEN_ERROR_CODES } from './result-code'

// 学生端（C 端）相关
export type {
  StudentInfo,
  TestRecord,
  TestDetail,
  TestLevel,
  StudentDashboard,
  DashboardIndicators,
  FitnessItemScore,
  DiagnosisSummary,
  HealthRisk,
  DiagnosisReport,
} from './student'
