// types/user.ts
import { type RoleString, type UserType } from '@/types/index'

/* ==================== 表单请求类型 ==================== */

export interface LoginForm {
  username: string
  password: string
  deviceType: string
  deviceId?: string
  rememberMe?: boolean
  captcha?: string
  imgCode?: string
}

/* ==================== 前端内部类型（统一 camelCase） ==================== */

/** 用户信息（前端内部统一 camelCase） */
export interface UserInfo {
  userId: number
  campusId: number
  username: string
  nickname: string
  userType: UserType
  phoneNumber?: string
  email?: string
  remark?: string
  avatar?: string
  roles: RoleString[]
  permissions: string[]
}

/** 登录响应（前端内部统一 camelCase） */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

/* ==================== 后端蛇形原始响应（仅供 API 适配层使用） ==================== */

/** 后端 /user/info 原始返回（snake_case） */
export interface UserInfoRaw {
  user_id: number
  campus_id: number
  username: string
  nickname: string
  user_type: number
  phone_number?: string
  email?: string
  remark?: string
  roles: RoleString[]
  permissions: string[]
}

/** 后端 /auth/login 原始返回（snake_case） */
export interface LoginResponseRaw {
  access_token: string
  refresh_token: string
  expires_in: number
}
