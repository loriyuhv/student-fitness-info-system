<template>
  <div class="error-container">
    <div class="error-content">
      <el-icon class="error-icon warning">
        <Warning />
      </el-icon>

      <h1 class="error-code">403</h1>
      <p class="error-title">无权限访问</p>
      <p class="error-description">
        抱歉，您没有权限访问此页面。
        <br />
        请联系管理员获取相应权限。
      </p>

      <div class="error-action">
        <el-button type="primary" @click="goBack" :icon="ArrowLeft"> 返回上一页 </el-button>
        <el-button @click="goHome" :icon="House"> 返回首页 </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Warning, ArrowLeft, House } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'
import { getHomePath } from '@/utils/router-helper'

const router = useRouter()
const userStore = useUserStore()

function goBack(): void {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    goHome()
  }
}

function goHome(): void {
  router.push(getHomePath(userStore.userInfo))
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;
@use '@/assets/styles/abstracts/mixins' as *;

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

.error-icon {
  font-size: 80px;
  margin-bottom: 20px;

  &.warning {
    color: $color-warning;
  }
}

.error-code {
  font-size: 120px;
  color: $color-text-primary;
  margin: 0;
  line-height: 1;
  font-weight: bold;
  @include gradient-text($gradient-primary);
}

.error-title {
  font-size: 24px;
  color: $color-text-regular;
  margin: 20px 0 10px;
  font-weight: 500;
}

.error-description {
  color: $color-text-secondary;
  font-size: $font-size-base;
  margin-bottom: 30px;
  line-height: 1.6;
}

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
