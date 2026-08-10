// utils/auth.ts
// 负责：认证业务流程

import authApi from '@/api/auth'
import type { LoginForm } from '@/types/user'
import { useUserStore, useTokenStore } from '@/store'

// 正在执行的用户信息请求
let userInfoPromise: null | Promise<any> = null

export function loadUserInfo(force = false) {
  const userStore = useUserStore()

  // 1. 已经存在用户信息，不需要重复请求
  if (!force && userStore.hasUserInfo) {
    return Promise.resolve(userStore.userInfo)
  }

  // 2. 如果已经有人正在请求
  if (userInfoPromise) {
    return userInfoPromise
  }

  // 3. 第一次请求
  userInfoPromise = authApi
    .getUserInfo()
    .then((res) => {
      // 保存用户信息
      userStore.setUserInfo(res)
      return res
    })
    .finally(() => {
      // 请求结束以后释放
      userInfoPromise = null
    })
  return userInfoPromise
}

/**
 * 判断是否存在登录凭证
 */
export function hasToken() {
  const tokenStore = useTokenStore()
  return !!tokenStore.accessToken
}

/**
 * 获取accessToken
 */
export function getAccessToken() {
  const tokenStore = useTokenStore()
  return tokenStore.accessToken
}

/**
 * 清除登录信息
 */
export function clearAuth() {
  const tokenStore = useTokenStore()
  const userStore = useUserStore()

  // 清token
  tokenStore.clearTokens()
  // 清用户信息
  userStore.clearUserInfo()
}

/**
 * 判断用户是否认证
 */
export async function checkAuth() {
  const tokenStore = useTokenStore()
  if (!tokenStore.accessToken) {
    return false
  }
  try {
    await loadUserInfo()
    return true
  } catch (e) {
    console.error('认证失败！！！', e)
    clearAuth()
    return false
  }
}

/**
 * 用户登录
 * @param loginForm
 */
export async function login(loginForm: LoginForm) {
  const tokenStore = useTokenStore()
  userInfoPromise = null // 强制丢弃可能进行中的旧请求
  const res = await authApi.login(loginForm)

  if (!res.accessToken || !res.refreshToken) {
    throw new Error('登录凭证不存在')
  }

  // 保存token
  try {
    tokenStore.setTokens(res.accessToken, res.refreshToken)
    await loadUserInfo()
    return res
  } catch (e) {
    // 加载用户信息失败时回滚，避免半登录状态
    clearAuth()
    userInfoPromise = null // 同时重置 userInfoPromise，避免残留
    const msg = e instanceof Error ? e.message : String(e)
    throw new Error(`登录成功但获取用户信息失败，请重试: ${msg}`)
  }
}

/**
 * 获取 Authorization 请求头完整值 Bearer token
 */
export function getAuthHeader() {
  const token = getAccessToken()
  if (!token) return null
  return `Bearer ${token}`
}
