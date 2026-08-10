<template>
  <div class="auth-layout">
    <!-- 背景装饰 -->
    <div class="auth-background">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>

    <!-- 主内容区域 -->
    <main class="auth-main">
      <router-view />
    </main>

    <!-- 版权信息 -->
    <footer class="auth-footer">
      <div class="copyright">
        <p>© 2025 学生专业体质诊断信息管理系统 版权所有</p>
        <p>技术支持：教育技术中心</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
// 认证布局组件，用于登录、注册等页面
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;
@use '@/assets/styles/abstracts/mixins' as *;

.auth-layout {
  min-height: 95vh;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

// 背景装饰
.auth-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba($color-white, 0.1);
  animation: float 15s infinite ease-in-out;

  &.circle-1 {
    width: 300px;
    height: 300px;
    top: -150px;
    right: -150px;
  }

  &.circle-2 {
    width: 200px;
    height: 200px;
    bottom: -100px;
    left: -100px;
    animation-delay: -5s;
  }

  &.circle-3 {
    width: 150px;
    height: 150px;
    top: 50%;
    right: 20%;
    animation-delay: -10s;
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

// 主内容区域
.auth-main {
  @include flex-center;
  min-height: 100vh;
  padding: 20px;
  position: relative;
  z-index: 1;
}

// 页脚版权
.auth-footer {
  position: absolute;
  bottom: 20px;
  width: 100%;
  text-align: center;
  color: rgba($color-white, 0.7);
  font-size: 12px;
  z-index: 1;

  .copyright {
    p {
      margin: 5px 0;
    }
  }

  @include respond-to(sm) {
    position: relative;
    bottom: auto;
    margin-top: -60px;
    padding-bottom: 20px;
  }
}
</style>
