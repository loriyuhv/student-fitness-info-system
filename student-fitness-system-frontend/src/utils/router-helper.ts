// utils/router-helper.ts
import type { UserInfo } from '@/types'
import { UserType } from '@/types'

/**
 * 根据用户类型返回首页路径
 * 学生 → 个人体测中心；管理员 / 教师 → 体测信息管理页
 */
export function getHomePath(userInfo: UserInfo | null): string {
  if (!userInfo) return '/auth/login'
  return userInfo.userType === UserType.STUDENT ? '/student/dashboard' : '/fitness-record/dashboard'
}
