// utils/router-helper.ts
import { Role, type UserInfo } from '@/types'

/**
 * 根据用户角色返回首页路径
 * 学生 → 个人体测中心；管理员 / 教师 → 体测信息管理页
 */
export function getHomePath(userInfo: UserInfo | null): string {
  if (!userInfo) return '/auth/login'

  // 学生
  if (userInfo.roles?.includes(Role.STUDENT)) {
    return '/student/dashboard'
  }

  // 管理员或教师 → 管理页
  if (userInfo.roles?.includes(Role.ADMIN) || userInfo.roles?.includes(Role.TEACHER)) {
    return '/fitness-record/dashboard'
  }

  // 兜底：无角色时返回登录页
  return '/auth/login'
}
