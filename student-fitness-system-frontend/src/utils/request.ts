import router from '@/router'
import type { ApiResponse } from '@/types'
import { clearAuth, getAccessToken, getRefreshToken, setTokens } from '@/utils/auth'
import { TOKEN_ERROR_CODES, ResultCode } from '@/types/result-code'
import axios, { AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'

/* ==================== 配置 ==================== */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

/** 业务错误（非全局拦截错误），由调用方决定是否提示 */
export class ApiError extends Error {
  readonly bizCode: number
  constructor(bizCode: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.bizCode = bizCode
  }
}

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
  },
})

/** 标记重放请求，避免无限刷新循环 */
interface RetryableConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

/* ==================== Token 静默刷新（single-flight） ==================== */
let refreshPromise: Promise<string | null> | null = null

function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return Promise.resolve(null)

  if (!refreshPromise) {
    refreshPromise = (async () => {
      try {
        // 用裸 axios 绕过本实例拦截器，避免刷新请求再次触发 401 造成死循环
        const { data } = await axios.post<
          ApiResponse<{ access_token: string; refresh_token: string; expires_in: number }>
        >(`${API_BASE_URL}/auth/refresh`, { refreshToken }, { timeout: 10000 })

        const payload = data?.data
        if (data?.bizCode === ResultCode.SUCCESS && payload?.access_token) {
          setTokens(payload.access_token, payload.refresh_token)
          return payload.access_token
        }
        return null
      } catch {
        return null
      } finally {
        refreshPromise = null
      }
    })()
  }
  return refreshPromise
}

function isTokenError(code: number): boolean {
  return TOKEN_ERROR_CODES.includes(code)
}

/** 凭证彻底失效：清空本地状态并跳登录页 */
async function redirectToLogin(): Promise<never> {
  clearAuth()
  const redirect = encodeURIComponent(window.location.pathname + window.location.search)
  await router.replace(`/auth/login?redirect=${redirect}`)
  return Promise.reject(new Error('登录状态已失效'))
}

/* ==================== 请求拦截器：注入 Token ==================== */
axiosInstance.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/* ==================== 响应拦截器 ==================== */
axiosInstance.interceptors.response.use(
  // 成功分支：处理 bizCode 层面的 token 失效 / 无权限
  async (response) => {
    const res = response.data as ApiResponse<unknown>
    const config = response.config as RetryableConfig

    // 1. Token 失效：尝试静默刷新后重放原请求（仅重试一次）
    if (isTokenError(res.bizCode)) {
      if (!config._retry) {
        const newToken = await refreshAccessToken()
        if (newToken) {
          config._retry = true
          config.headers.Authorization = `Bearer ${newToken}`
          return axiosInstance(config)
        }
      }
      ElMessage.error('登录状态已失效，请重新登录')
      return redirectToLogin()
    }

    // 2. 无权限：跳转 403 页
    if (res.bizCode === ResultCode.PERMISSION_DENIED) {
      await router.replace('/403')
      return Promise.reject(new ApiError(res.bizCode, res.message || '无访问权限'))
    }

    // 其他业务错误交由 httpRequest 统一抛 ApiError
    return response
  },
  // 失败分支：HTTP 层错误
  async (error: AxiosError) => {
    const config = error.config as RetryableConfig | undefined

    // 1. HTTP 401：同样尝试刷新重放
    if (error.response?.status === 401) {
      if (config && !config._retry) {
        const newToken = await refreshAccessToken()
        if (newToken) {
          config._retry = true
          config.headers.Authorization = `Bearer ${newToken}`
          return axiosInstance(config)
        }
      }
      ElMessage.error('认证失败，请重新登录')
      return redirectToLogin()
    }

    // 2. HTTP 403
    if (error.response?.status === 403) {
      ElMessage.error('访问被拒绝')
      await router.replace('/403')
      return Promise.reject(error)
    }

    // 3. 其他网络 / 服务端错误
    if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else if (!error.response) {
      ElMessage.error('网络连接异常，请检查网络设置')
    } else {
      const status = error.response.status
      switch (status) {
        case 400:
          ElMessage.error('参数错误')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
        case 502:
        case 503:
        case 504:
          ElMessage.error('服务器异常，请稍后重试')
          break
        default:
          ElMessage.error('请求失败')
      }
    }

    return Promise.reject(error)
  },
)

/* ==================== 类型安全泛型方法 ==================== */
export async function httpRequest<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await axiosInstance<ApiResponse<T>>(config)
  const res = response.data

  if (res.bizCode !== ResultCode.SUCCESS) {
    // 业务错误：不在此处弹提示，抛给调用方处理（避免双重弹窗）
    throw new ApiError(res.bizCode, res.message || '请求失败')
  }

  return res.data
}

/* ==================== 保持向后兼容 ==================== */
export default axiosInstance
