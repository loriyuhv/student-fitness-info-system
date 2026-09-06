// utils/permission.ts
import { Role, type UserInfo } from '@/types'

/**
 * 安全的角色判断（默认拒绝）
 *
 * @param userInfo 用户信息
 * @param roles 路由要求的角色列表（undefined=公开，[]=无角色可访问，['ADMIN']=需要特定角色）
 * @returns boolean true=有权限，false=无权限
 */
export function hasRole(userInfo: UserInfo | null, roles: Role[] | undefined): boolean {
  // 1. 未配置角色（undefined），视为公开页面，放行
  if (roles === undefined || roles === null) return true

  // 2. 配置了空数组（[]），表示“任何角色都不允许”，拦截（安全底线）
  if (roles.length === 0) return false

  // 3. 用户没有角色信息，拦截
  if (!userInfo?.roles || userInfo.roles.length === 0) return false

  // 4. 检查用户角色是否命中任一要求
  return roles.some(r => userInfo.roles.includes(r))
}

/**
 * 安全的权限判断（默认拒绝）
 *
 * @param userInfo 用户信息
 * @param permissions 路由要求的权限列表（undefined=公开，[]=无权限访问，['xx']=需要权限）
 * @param mode OR：命中任一；AND：必须全部命中
 */
export function hasPermission(
  userInfo: UserInfo | null,
  permissions: string[] | undefined,
  mode: 'AND' | 'OR' = 'OR',
): boolean {
  // 1. 如果未配置权限字段（undefined），视为公开页面，直接放行
  if (permissions === undefined || permissions === null) return true

  // 2. 如果配置了空数组（[]），表示“无权限可见”，直接拦截（安全底线）
  if (permissions.length === 0) return false

  // 3. 检查用户是否有权限
  if (!userInfo?.permissions) return false

  return mode === 'AND'
    ? permissions.every((p) => userInfo.permissions.includes(p))
    : permissions.some((p) => userInfo.permissions.includes(p))
}
