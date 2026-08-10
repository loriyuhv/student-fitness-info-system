<template>
  <div class="error-container">
    <div class="error-content">
      <!-- 错误图标 -->
      <el-icon class="error-icon error">
        <CircleClose />
      </el-icon>

      <h1 class="error-code">404</h1>
      <p class="error-title">页面不存在</p>
      <p class="error-description">
        抱歉，您访问的页面不存在或已被移除。
        <br />
        请检查网址是否正确。
      </p>

      <div class="error-action">
        <el-button type="primary" @click="goBack" :icon="ArrowLeft"> 返回上一页 </el-button>
        <el-button @click="goHome" :icon="House"> 返回首页 </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { ElButton, ElIcon } from 'element-plus'
import { getHomePath } from '@/utils/router-helper.ts'
import { CircleClose, ArrowLeft, House } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

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
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;
@use '@/assets/styles/abstracts/mixins' as *;

.error-container {
  @include flex-center;
  min-height: 100vh;
  background: $gradient-primary;
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

  &.error {
    color: $color-danger;
  }

  &.warning {
    color: $color-warning;
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
