// ============================================
// 认证管理 API 模块
// ============================================

import { httpRequest } from '@/utils/request' // 使用类型安全方法
import { clearAuth } from '@/utils/auth.ts' // 使用枚举
import type { LoginForm, UserInfo, LoginResponse } from '@/types/user'

/* ==================== API 方法 ==================== */

/**
 * 用户登录
 * @returns 登录响应（包含 accessToken 和用户信息）
 */
export async function login(loginForm: LoginForm): Promise<LoginResponse> {
  return httpRequest<LoginResponse>({
    method: 'POST',
    url: '/auth/login',
    data: loginForm,
  })
}

/**
 * 用户退出登录
 */
export async function logout(): Promise<void> {
  try {
    await httpRequest<void>({ method: 'POST', url: '/auth/logout' })
  } catch (error) {
    console.error('退出登录 API 调用失败:', error)
  }

  // 无论 API 是否成功，都清除本地认证信息
  clearAuth()
}

/**
 * 获取当前用户信息
 */
export async function getUserInfo(): Promise<UserInfo> {
  return httpRequest<UserInfo>({
    method: 'GET',
    url: '/user/info',
  })
}

/**
 * 刷新访问令牌
 */
export async function refreshToken(refreshToken: string): Promise<{ accessToken: string }> {
  return httpRequest<{ accessToken: string }>({
    method: 'POST',
    url: '/auth/refresh',
    data: { refreshToken },
  })
}

/**
 * 修改密码
 */
export async function changePassword(oldPassword: string, newPassword: string): Promise<void> {
  return httpRequest<void>({
    method: 'POST',
    url: '/auth/change-password',
    data: { oldPassword, newPassword },
  })
}

/* ==================== 导出 ==================== */
export default {
  login,
  logout,
  refreshToken,
  getUserInfo,
  changePassword,
}
