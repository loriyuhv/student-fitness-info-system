// ============================================
// 认证管理 API 模块（含蛇形 → 驼峰适配）
// ============================================

import { httpRequest } from '@/utils/request'
import { clearAuth } from '@/utils/auth'
import type {
  LoginForm,
  UserInfo,
  LoginResponse,
  LoginResponseRaw,
  UserInfoRaw,
} from '@/types/user'

/* ==================== 蛇形 → 驼峰适配 ==================== */

function mapLoginResponse(raw: LoginResponseRaw): LoginResponse {
  return {
    accessToken: raw.access_token,
    refreshToken: raw.refresh_token,
    expiresIn: raw.expires_in,
  }
}

function mapUserInfo(raw: UserInfoRaw): UserInfo {
  return {
    userId: raw.user_id,
    campusId: raw.campus_id,
    username: raw.username,
    nickname: raw.nickname,
    userType: raw.user_type,
    phoneNumber: raw.phone_number,
    email: raw.email,
    remark: raw.remark,
    permissions: raw.permissions ?? [],
  }
}

/* ==================== API 方法 ==================== */

/** 用户登录 */
export async function login(loginForm: LoginForm): Promise<LoginResponse> {
  const raw = await httpRequest<LoginResponseRaw>({
    method: 'POST',
    url: '/auth/login',
    data: loginForm,
  })
  return mapLoginResponse(raw)
}

/** 用户退出登录 */
export async function logout(): Promise<void> {
  try {
    await httpRequest<void>({ method: 'POST', url: '/auth/logout' })
  } catch (error) {
    console.error('退出登录 API 调用失败:', error)
  }
  // 无论 API 是否成功，都清除本地认证信息
  clearAuth()
}

/** 获取当前用户信息 */
export async function getUserInfo(): Promise<UserInfo> {
  const raw = await httpRequest<UserInfoRaw>({ method: 'GET', url: '/user/info' })
  return mapUserInfo(raw)
}

/** 修改密码 */
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
  getUserInfo,
  changePassword,
}
