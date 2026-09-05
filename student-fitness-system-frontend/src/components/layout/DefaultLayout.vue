<template>
  <div id="app" class="default-layout">
    <!-- 固定头部导航栏 -->
    <el-header v-if="showHeader" class="main-header fixed-header">
      <div class="header-content">
        <!-- 左侧 Logo -->
        <div class="header-left">
          <h1 class="logo">
            <el-icon class="logo-icon"><Monitor /></el-icon>
            <span>学生专业体质诊断信息管理系统</span>
          </h1>
        </div>

        <!-- 中间面包屑 -->
        <div class="header-center">
          <Breadcrumb :breadcrumb="breadcrumb" />
        </div>

        <!-- 右侧用户信息 -->
        <div class="header-right" v-if="userInfo">
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-info">
              <el-avatar :size="36" :src="userInfo.avatar" class="user-avatar">
                {{ userInfo.nickname?.charAt(0) || '某' }}
              </el-avatar>
              <span class="user-name">{{ userInfo.nickname }}</span>
              <el-icon class="arrow-down"><ArrowDown /></el-icon>
            </div>

            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 主要内容包装器 -->
    <div class="main-content-wrapper">
      <el-main :class="{ 'with-header': showHeader, 'without-header': !showHeader }">
        <router-view />
      </el-main>

      <!-- 页脚 -->
      <el-footer v-if="showHeader" class="main-footer">
        <div class="footer-content">
          <p>© 2025 学生专业体质诊断信息管理系统 版权所有</p>
          <p>技术支持：教育技术中心 | 服务热线：010-12345678</p>
        </div>
      </el-footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import type { BreadcrumbItem } from '@/types'
import Breadcrumb from '@/components/common/Breadcrumb.vue'
import { useUserStore } from '@/store'
import { logout } from '@/api/auth'

const route = useRoute()
const router = useRouter()

// 用户信息从 Pinia 读取（持久化 + 响应式）
const userStore = useUserStore()
const { userInfo } = storeToRefs(userStore)

// 是否显示头部（登录页不显示）
const showHeader = computed(() => route.name !== 'Login')

// 面包屑数据
const breadcrumb = ref<BreadcrumbItem[]>([])

/**
 * 更新面包屑
 */
function updateBreadcrumb(): void {
  const matched = route.matched.filter((r) => r.meta && r.meta.title)
  breadcrumb.value = matched.slice(1).map((item) => ({
    path: item.path,
    meta: {
      title: (item.meta?.title as string) || '默认标题',
    },
  }))
}

/**
 * 处理下拉菜单命令
 */
async function handleCommand(command: string): Promise<void> {
  switch (command) {
    case 'profile':
      ElMessage.info('个人中心功能开发中...')
      break
    case 'password':
      ElMessage.info('修改密码功能开发中...')
      break
    case 'logout':
      await handleLogout()
      break
  }
}

/**
 * 处理退出登录：调后端 + 清除本地凭证 + 跳登录页
 */
async function handleLogout(): Promise<void> {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      center: true,
    })
  } catch {
    return // 用户取消
  }

  await logout()
  ElMessage.success('退出登录成功')
  await router.replace('/auth/login')
}

// 监听路由变化，刷新面包屑
watch(() => route.path, () => updateBreadcrumb(), { immediate: true })
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;
@use '@/assets/styles/abstracts/mixins' as *;

// 固定头部样式
.main-header {
  background: $gradient-primary;
  height: $header-height !important;
  padding: 0 !important;
  box-shadow: $shadow-light;
  z-index: $header-z-index;
}

.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  width: 100%;
}

// 头部内容布局
.header-content {
  @include flex-between;
  height: 100%;
  padding: 0 20px;
  max-width: $content-max-width;
  margin: 0 auto;
  width: 100%;

  // 移动端适配
  @include respond-to(sm) {
    padding: 0 10px;
  }
}

.header-center {
  flex: 1;
  padding: 0 20px;

  @include respond-to(sm) {
    display: none; // 移动端隐藏面包屑
  }
}

// Logo 样式
.logo {
  @include flex-start;
  gap: 10px;
  color: $color-white;
  font-size: 20px;
  margin: 0;

  .logo-icon {
    font-size: 24px;
  }

  @include respond-to(sm) {
    font-size: 16px;

    span {
      @include text-ellipsis;
      max-width: 150px; // 防止移动端文字溢出
    }
  }
}

// 用户信息区域
.user-info {
  @include flex-start;
  gap: 10px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: $border-radius-base;
  transition: all $transition-duration $transition-timing;
  color: $color-white;

  &:hover {
    background-color: rgba($color-white, 0.1);
  }
}

.user-avatar {
  background-color: $color-primary;
}

.user-name {
  font-size: $font-size-base;
  color: $color-white;

  @include respond-to(sm) {
    display: none; // 移动端隐藏用户名
  }
}

.arrow-down {
  color: rgba($color-white, 0.7);
  font-size: 12px;

  @include respond-to(sm) {
    display: none; // 移动端隐藏箭头
  }
}

// 页脚样式
.main-footer {
  background: $color-bg-primary;
  border-top: 1px solid $color-border-light;
  height: $footer-height !important;
  padding: $footer-padding !important;
  width: 100%;
  flex-shrink: 0;

  .footer-content {
    text-align: center;
    color: $color-text-secondary;
    font-size: 12px;
    line-height: 1.5;
    max-width: $content-max-width;
    margin: 0 auto;

    p {
      margin: 5px 0;
    }
  }
}
</style>
