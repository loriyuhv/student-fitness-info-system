<template>
  <div class="error-container">
    <div class="error-content">
      <!-- 警告图标 -->
      <el-icon class="error-icon warning">
        <Warning />
      </el-icon>

      <h1 class="error-code">401</h1>
      <p class="error-title">无权限访问</p>
      <p class="error-description">
        抱歉，您没有权限访问此页面。
        <br />
        请联系管理员获取相应权限。
      </p>

      <div class="error-action">
        <el-button type="primary" @click="goBack" :icon="ArrowLeft"> 返回上一页 </el-button>
        <el-button @click="goHome" :icon="House"> 返回首页 </el-button>
        <el-button v-if="!isAuthenticated" @click="goLogin" :icon="User" class="login-btn">
          重新登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElButton, ElIcon } from 'element-plus'
import { Warning, ArrowLeft, House, User } from '@element-plus/icons-vue'
import { checkAuth, clearAuth } from '@/utils/auth.ts'
import { getHomePath } from '@/utils/router-helper.ts'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()
const isAuthenticated = ref(false)

// 检查登录状态
onMounted(async () => {
  // 校验token和用户信息是否有效
  isAuthenticated.value = await checkAuth()
})

// 返回上一页：容错处理，无历史记录则跳首页
function goBack(): void {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    goHome()
  }
}

// 根据用户权限自动跳转首页
function goHome(): void {
  const homePath = getHomePath(userStore.userInfo)
  router.push(homePath)
}

// 前往登录页，清除旧认证信息
function goLogin(): void {
  clearAuth()
  router.push('/auth/login')
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;
@use '@/assets/styles/abstracts/mixins' as *;
@use 'sass:color';

.error-container {
  @include flex-center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.error-content {
  text-align: center;
  max-width: 600px;
  padding: 40px;
  background: $color-white;
  border-radius: $border-radius-large;
  box-shadow: $shadow-dark;
}

// 错误图标样式
.error-icon {
  font-size: 80px;
  margin-bottom: 20px;

  &.warning {
    color: $color-warning;
  }

  &.error {
    color: $color-danger;
  }
}

// 错误代码（如 401, 404）
.error-code {
  font-size: 120px;
  color: $color-text-primary;
  margin: 0;
  line-height: 1;
  font-weight: bold;
  @include gradient-text($gradient-primary);
}

// 错误标题
.error-title {
  font-size: 24px;
  color: $color-text-regular;
  margin: 20px 0 10px;
  font-weight: 500;
}

// 错误描述
.error-description {
  color: $color-text-secondary;
  font-size: $font-size-base;
  margin-bottom: 30px;
  line-height: 1.6;
}

// 操作按钮区域
.error-action {
  margin-top: 30px;
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;

  .el-button {
    min-width: 120px;

    @include respond-to(xs) {
      width: 100%;
      margin: 0;
    }
  }
}

// 重新登录按钮特殊样式
.login-btn {
  background: linear-gradient(
    135deg,
    $color-success 0%,
    color.adjust($color-success, $lightness: -10%) 100%
  );
  border-color: $color-success;

  &:hover {
    background: linear-gradient(
      135deg,
      color.adjust($color-success, $lightness: -5%) 0%,
      color.adjust($color-success, $lightness: -15%) 100%
    );
  }
}

// 响应式适配
@include respond-to(sm) {
  .error-content {
    padding: 30px 20px;
  }

  .error-code {
    font-size: 80px;
  }

  .error-title {
    font-size: 20px;
  }
}

@include respond-to(xs) {
  .error-content {
    padding: 20px 15px;
  }

  .error-icon {
    font-size: 60px;
  }

  .error-code {
    font-size: 60px;
  }

  .error-title {
    font-size: 18px;
  }
}
</style>
