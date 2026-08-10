// types/user.ts
import type { UserType } from '@/types/index'

/* 表单请求类型设计 */
// 登录表单类型
export interface LoginForm {
  username: string // 用户账户
  password: string // 用户密码
  deviceType: string // 设备类型
  rememberMe?: boolean // 记住密码（可选）
  captcha?: string // 验证码（可选）
  imgCode?: string // 图形验证码（可选）
}

/* 用户信息类型设计 */
// 基础用户信息（登录后返回的最小集合）
export interface UserInfo {
  userId: number // 用户ID
  campusId: number // 学校ID
  username: number | string // 用户账户
  nickname: string // 用户昵称
  userType: UserType // 用户类型
  phoneNumber?: string // 手机号
  email?: string // 邮箱
  remark?: string // 备注
  permissions: string[] // 权限列表
}

/* 用户登录响应类型设计 */
// 登录响应数据
export interface LoginResponse {
  accessToken: string // JWT令牌
  refreshToken: string // 刷新令牌
  expiresIn: number // 过期时间（秒）
}
