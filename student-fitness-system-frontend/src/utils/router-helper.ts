import type { UserInfo } from '@/types'

export function getHomePath(userInfo: UserInfo | null) {
  if (!userInfo) {
    return '/auth/login'
  }

  if (userInfo.permissions.includes('fitness:record:view')) {
    return '/fitness-record/dashboard'
  }

  if (userInfo.permissions.includes('fitness:record:self:view')) {
    return '/student/center'
  }

  return '/401'
}
