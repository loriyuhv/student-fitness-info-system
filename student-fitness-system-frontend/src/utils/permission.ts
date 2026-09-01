// utils/permission.ts
import type { UserInfo } from '@/types'

export function hasPermission(userInfo: UserInfo | null, permissions: string[]) {
  if (!userInfo) {
    return false
  }

  return permissions.some((permission) => {
    return userInfo.permissions.includes(permission)
  })
}
