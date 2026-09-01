// utils/permission.ts
import type { UserInfo } from '@/types'

export function hasPermission(
  userInfo: UserInfo | null,
  permissions: string[],
  mode='OR'
) {
  if (mode === 'AND') {
    return permissions.every( p => userInfo?.permissions.includes(p))
  }

  return permissions.some(
    p => userInfo?.permissions.includes(p)
  )
}
