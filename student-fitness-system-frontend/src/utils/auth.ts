// utils/auth.ts
// 负责：认证业务流程（登录凭证、用户信息加载、登出）

import authApi from '@/api/auth'
import type { LoginForm, UserInfo } from '@/types/user'
import { useUserStore, useTokenStore } from '@/store'

// 正在执行的用户信息请求（single-flight）
let userInfoPromise: null | Promise<UserInfo> = null

/**
 * 加载用户信息（带缓存 + 单飞去重）
 */
export function loadUserInfo(force = false): Promise<UserInfo> {
  const userStore = useUserStore()

  // 1. 已有用户信息，直接复用
  if (!force && userStore.hasUserInfo) {
    return Promise.resolve(userStore.userInfo as UserInfo)
  }

  // 2. 已有在途请求，复用
  if (userInfoPromise) {
    return userInfoPromise
  }

  // 3. 首次请求
  userInfoPromise = authApi
    .getUserInfo()
    .then((res) => {
      userStore.setUserInfo(res)
      return res
    })
    .finally(() => {
      userInfoPromise = null
    })
  return userInfoPromise
}

/* ==================== 凭证读取 ==================== */

export function hasToken(): boolean {
  return !!useTokenStore().accessToken
}

export function getAccessToken(): string {
  return useTokenStore().accessToken
}

export function getRefreshToken(): string {
  return useTokenStore().refreshToken
}

export function setTokens(access: string, refresh: string): void {
  useTokenStore().setTokens(access, refresh)
}

/* ==================== 登出 / 清空 ==================== */

export function clearAuth(): void {
  useTokenStore().clearTokens()
  useUserStore().clearUserInfo()
  // 丢弃可能残留的在途请求，避免旧请求回填脏数据
  userInfoPromise = null
}

/* ==================== 认证判断 ==================== */

export async function checkAuth(): Promise<boolean> {
  if (!hasToken()) return false
  try {
    await loadUserInfo()
    return true
  } catch (e) {
    console.error('认证失败：', e)
    clearAuth()
    return false
  }
}

/* ==================== 登录 ==================== */

export async function login(loginForm: LoginForm): Promise<void> {
  userInfoPromise = null // 丢弃可能进行中的旧请求

  const res = await authApi.login(loginForm)

  if (!res.accessToken || !res.refreshToken) {
    throw new Error('登录凭证不存在')
  }

  try {
    setTokens(res.accessToken, res.refreshToken)
    await loadUserInfo()
  } catch (e) {
    // 加载用户信息失败时回滚，避免半登录状态
    clearAuth()
    const msg = e instanceof Error ? e.message : String(e)
    throw new Error(`登录成功但获取用户信息失败，请重试: ${msg}`)
  }
}

/* ==================== 请求头 ==================== */

export function getAuthHeader(): string | null {
  const token = getAccessToken()
  return token ? `Bearer ${token}` : null
}
