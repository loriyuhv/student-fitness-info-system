// router/index.ts
/*
 * 路由职责：
 * 1. 页面是否需要登录？
 * 2. 当前用户是否有角色/权限？
 * 3. 放行 / 跳转
 */
import type { App } from 'vue'
import type { RouteMeta } from '@/types'
import { Role } from '@/types'
import { useUserStore } from '@/store'
import AuthLayout from '@/components/layout/AuthLayout.vue'
import DefaultLayout from '@/components/layout/DefaultLayout.vue'
import { checkAuth } from '@/utils/auth'
import { hasPermission, hasRole } from '@/utils/permission'
import { getHomePath } from '@/utils/router-helper'
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

// 常量统一权限编码，杜绝字符写错
// 注意：学生、教师、管理员共用 'fitness:record:view' 权限，
// 但通过角色绑定的 data_scope 字段控制数据可见范围
const PERM = {
  STUDENT_CENTER: 'fitness:record:view',
  MANAGE_PAGE: 'fitness:record:view',
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    redirect: () => {
      // 按登录态 + 角色跳首页
      const userStore = useUserStore()
      return userStore.hasUserInfo ? getHomePath(userStore.userInfo) : '/auth/login'
    },
  },
  // 认证路由（登录）
  {
    path: '/auth',
    component: AuthLayout,
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/views/user/Login.vue'),
        meta: {
          title: '登录',
          requiresGuest: true, // 防止已登录用户重复访问登录页
        } as RouteMeta,
      },
    ],
  },
  // 学生端路由（只读 C 端）
  {
    path: '/student',
    component: DefaultLayout,
    // 学生端整组：需登录 + 仅 STUDENT 角色 + 本人数据查看权限
    meta: {
      requiresAuth: true,
      roles: [Role.STUDENT],
      permissions: [PERM.STUDENT_CENTER],
    } as RouteMeta,
    children: [
      // /student → 默认跳仪表盘
      { path: '', redirect: { name: 'StudentDashboard' } },
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('@/views/student/Dashboard.vue'),
        meta: { title: '我的体测中心' } as RouteMeta,
      },
      {
        path: 'history',
        name: 'StudentHistory',
        component: () => import('@/views/student/HistoryList.vue'),
        meta: { title: '体测历史记录' } as RouteMeta,
      },
      {
        path: 'history/:id',
        name: 'StudentHistoryDetail',
        component: () => import('@/views/student/HistoryDetail.vue'),
        meta: { title: '体测详情' } as RouteMeta,
      },
      {
        path: 'diagnosis',
        name: 'StudentDiagnosis',
        component: () => import('@/views/student/DiagnosisReport.vue'),
        meta: { title: '诊断报告与运动处方' } as RouteMeta,
      },
    ],
  },
  // 体测信息管理路由（教师 / 管理员）
  {
    path: '/fitness-record',
    component: DefaultLayout,
    meta: { requiresAuth: true } as RouteMeta,
    children: [
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('@/views/teacher/Dashboard.vue'),
        meta: {
          title: '学生体测信息管理',
          roles: [Role.ADMIN, Role.TEACHER],
          permissions: [PERM.MANAGE_PAGE],
        } as RouteMeta,
      },
    ],
  },
  // 无权限访问
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限访问' } as RouteMeta,
  },
  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' } as RouteMeta,
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 全局路由守卫
router.beforeEach(async (to) => {
  // 设置标题
  if (to.meta.title) {
    document.title = `${to.meta.title}-体质诊断系统`
  }

  const requiresAuth = to.matched.some((record) => record.meta?.requiresAuth)
  const requiresGuest = to.matched.some((record) => record.meta?.requiresGuest)

  // 1. 需要认证
  if (requiresAuth) {
    const isAuthenticated = await checkAuth()
    if (!isAuthenticated) {
      return { path: '/auth/login', query: { redirect: to.fullPath } }
    }

    const userStore = useUserStore()

    // 角色校验（优先）
    const roles = to.meta.roles as Role[] | undefined
    if (roles && !hasRole(userStore.userInfo, roles)) {
      return '/403'
    }

    // 权限校验
    const permissions = to.meta.permissions as string[] | undefined
    if (permissions && !hasPermission(userStore.userInfo, permissions)) {
      return '/403'
    }

    return true
  }

  // 2. 已登录用户禁止进入登录页
  if (requiresGuest) {
    const isAuthenticated = await checkAuth()
    if (isAuthenticated) {
      return getHomePath(useUserStore().userInfo)
    }
  }

  return true
})

// 全局注册 router
export function setupRouter(app: App<Element>) {
  app.use(router)
}

export default router
