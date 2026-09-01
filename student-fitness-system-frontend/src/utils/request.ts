import router from '@/router'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types'
import { clearAuth, getAuthHeader } from '@/utils/auth.ts'
import { TOKEN_ERROR_CODES, ResultCode } from '@/types/result-code'
import axios, { type AxiosError, type AxiosResponse, type AxiosRequestConfig } from 'axios'

/* ==================== 配置 ==================== */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

/* 1. 创建 axios 实例 */
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
  },
})

const handleAuthFailure = async (redirect = true) => {
  clearAuth()
  const path = redirect
    ? `/auth/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
    : '/atuh/login'
  await router.push(path)
}

/* 2. 请求拦截器：添加 Token */
axiosInstance.interceptors.request.use(
  (config) => {
    const authValue = getAuthHeader()
    if (authValue) {
      config.headers.Authorization = authValue
    }
    return config
  },
  (error: AxiosError) => Promise.reject(error),
)

/* 3. 响应拦截器：只处理全局跳转类错误 */
axiosInstance.interceptors.response.use(
  async (response: AxiosResponse<ApiResponse<unknown>>) => {
    const res = response.data

    // Token 失效：强制跳转登录页
    if (TOKEN_ERROR_CODES.includes(res.bizCode)) {
      ElMessage.error('登录状态已失效，请重新登录')
      await handleAuthFailure()
      return Promise.reject(new Error(res.message || '登录已过期'))
    }

    // 权限不足：跳转 401 页面
    if (res.bizCode === ResultCode.PERMISSION_NO_ACCESS) {
      ElMessage.error('您没有权限访问此资源')
      await router.push('/401')
      return Promise.reject(new Error(res.message || '无访问权限'))
    }

    // 其他错误（包括业务失败）返回原始 response，由 httpRequest 处理
    return response
  },
  // HTTP 层错误（网络、超时、网关等）
  async (error: AxiosError): Promise<never> => {
    console.error('HTTP 响应错误:', error)

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
        case 401:
          ElMessage.error('认证失败，请重新登录')
          await handleAuthFailure()
          break
        case 403:
          ElMessage.error('访问被拒绝')
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

/* 4. 类型安全的泛型方法 */
export async function httpRequest<T>(config: AxiosRequestConfig): Promise<T> {
  return axiosInstance<ApiResponse<T>>(config).then((response) => {
    const apiRes = response.data // ApiResponse<T>

    // 检查业务状态码（拦截器已处理 Token/权限错误）
    if (apiRes.bizCode !== ResultCode.SUCCESS) {
      ElMessage.error(apiRes.message || '请求失败')
      return Promise.reject(new Error(apiRes.message || 'Error'))
    }

    // 成功，返回类型安全的业务数据
    return apiRes.data
  })
}

/* 5. 保持向后兼容 */
export default axiosInstance
