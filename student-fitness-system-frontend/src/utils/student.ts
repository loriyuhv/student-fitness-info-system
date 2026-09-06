// utils/student.ts
// 学生端通用展示辅助：等级 → Element Plus Tag 类型 / 分值颜色

import type { TestLevel } from '@/types/student'

/** 等级 → el-tag type */
export function levelTagType(level?: TestLevel): 'success' | 'primary' | 'warning' | 'danger' | 'info' {
  switch (level) {
    case '优秀':
      return 'success'
    case '良好':
      return 'primary'
    case '及格':
      return 'warning'
    case '不及格':
      return 'danger'
    default:
      return 'info'
  }
}

/** 分数 → 展示色（红/绿语义与后端一致：分数越高越健康） */
export function scoreColor(score?: number): string {
  if (score === undefined || score === null) return 'var(--el-text-color-placeholder)'
  if (score >= 90) return 'var(--el-color-success)'
  if (score >= 80) return 'var(--el-color-primary)'
  if (score >= 60) return 'var(--el-color-warning)'
  return 'var(--el-color-danger)'
}

/** 性别码 → 文案 */
export function genderText(gender?: 1 | 2): string {
  return gender === 1 ? '男' : gender === 2 ? '女' : '未知'
}
