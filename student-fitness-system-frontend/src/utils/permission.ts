// utils/permission.ts
import type { UserInfo } from '@/types'
import { Role, UserType } from '@/types'

/**
 * 由用户类型（后端 userType：0-管理员 1-教师 2-学生）推导角色集合。
 * 后端当前只返回 userType，不返回角色码，因此这里做一次映射。
 */
export function getRoles(userInfo: UserInfo | null): Role[] {
  if (!userInfo) return []
  switch (userInfo.userType) {
    case UserType.ADMIN:
      return [Role.ADMIN]
    case UserType.TEACHER:
      return [Role.TEACHER]
    case UserType.STUDENT:
      return [Role.STUDENT]
    default:
      return []
  }
}

/**
 * 角色判断：roles 为空视为不限制；命中任一角色即通过。
 */
export function hasRole(userInfo: UserInfo | null, roles: Role[]): boolean {
  if (!roles || roles.length === 0) return true
  const userRoles = getRoles(userInfo)
  return roles.some((r) => userRoles.includes(r))
}

/**
 * 权限判断：permissions 为空视为不限制。
 * @param mode OR：命中任一；AND：必须全部命中
 */
export function hasPermission(
  userInfo: UserInfo | null,
  permissions: string[],
  mode: 'AND' | 'OR' = 'OR',
): boolean {
  if (!permissions || permissions.length === 0) return true
  if (!userInfo?.permissions) return false

  return mode === 'AND'
    ? permissions.every((p) => userInfo.permissions.includes(p))
    : permissions.some((p) => userInfo.permissions.includes(p))
}
