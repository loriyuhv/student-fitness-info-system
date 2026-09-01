// router/index.ts
/*
 * 路由职责：
 * 1.页面是否需要登录?
 * 2.当前用户是否有权限?
 * 3.放行 / 跳转
 */
import type { App } from 'vue'
import type { RouteMeta } from '@/types'
import { useUserStore } from '@/store'
import AuthLayout from '@/components/layout/AuthLayout.vue'
import DefaultLayout from '@/components/layout/DefaultLayout.vue'
import { checkAuth } from '@/utils/auth.ts'
import { hasPermission } from '@/utils/permission.ts'
import { getHomePath } from '@/utils/router-helper.ts'
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

// 常量统一权限编码，杜绝字符写错
const PERM = {
  STUDENT_CENTER: 'fitness:record:self:view',
  MANAGE_PAGE: 'fitness:record:view',
}

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: () => {
      return '/auth/login'
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
  /* 学生端路由 */
  {
    path: '/student',
    component: DefaultLayout,
    meta: {
      requiresAuth: true,
      permissions: [PERM.STUDENT_CENTER],
    } as RouteMeta,
    children: [
      {
        path: 'center',
        name: 'StudentCenter',
        component: () => import('@/views/student/Dashboard.vue'),
        meta: {
          title: '学生体测信息个人详情',
        } as RouteMeta,
      },
    ],
  },
  // 体测信息管理路由
  {
    path: '/fitness-record',
    component: DefaultLayout,
    meta: {
      requiresAuth: true,
      permissions: [PERM.MANAGE_PAGE],
    } as RouteMeta,
    children: [
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('@/views/teacher/Dashboard.vue'),
        meta: {
          title: '学生体测信息管理',
        } as RouteMeta,
      },
    ],
  },
  // 其他路由...
  {
    path: '/401',
    name: 'Unauthorized',
    component: () => import('@/views/error/401.vue'),
    meta: {
      title: '无权访问',
    } as RouteMeta,
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
    } as RouteMeta,
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 路由守卫
router.beforeEach(async (to) => {
  // 设置标题
  if (to.meta.title) {
    document.title = `${to.meta.title}-体质诊断系统`
  }

  // 是否需要认证
  const requiresAuth = to.matched.some((record) => record.meta?.requiresAuth)
  // 是否游客页面
  const requiresGuest = to.matched.some((record) => record.meta?.requiresGuest)

  /**
   * 1. 需要认证
   */
  if (requiresAuth) {
    const isAuthenticated = await checkAuth()

    // 没认证
    if (!isAuthenticated) {
      return {
        path: '/auth/login',
        query: { redirect: to.fullPath },
      }
    }

    const userStore = useUserStore()
    const permissions = to.meta.permissions as string[]

    if (permissions && !hasPermission(userStore.userInfo, permissions)) {
      return '/401'
    }

    return true
  }

  /**
   * 2. 已登录用户禁止进入登录页
   */
  if (requiresGuest) {
    const isAuthenticated = await checkAuth()
    if (isAuthenticated) {
      const userStore = useUserStore()
      return getHomePath(userStore.userInfo)
    }
  }

  return true
})

// 全局注册 router
export function setupRouter(app: App<Element>) {
  app.use(router)
}

export default router
