/** API 统一响应接口 */
export interface ApiResponse<T = unknown> {
  /* HTTP 状态码（如 200, 401, 500） */
  httpCode: number

  /** 业务状态码（如 200000, 101001） */
  bizCode: number

  /** 响应消息 */
  message: string

  /** 业务数据 */
  data: T

  /** 响应时间戳 */
  timestamp: number
}

/* 分页接口 */
export interface PageParams {
  page: number
  pageSize: number
}

/* 分页数据接口 */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}
