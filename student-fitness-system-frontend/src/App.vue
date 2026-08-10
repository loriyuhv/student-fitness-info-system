<template>
  <div id="app">
    <!-- 路由出口：根据当前路由动态渲染对应的组件 -->
    <router-view />
  </div>
</template>

<script setup lang="ts">
// ============================================
// 根组件 App.vue
// ============================================
// 该组件是 Vue 应用的入口，仅包含路由视图容器
// 具体页面内容由 Vue Router 根据路由配置动态加载
// 布局逻辑由 Layout 组件（DefaultLayout/AuthLayout）管理
// 全局样式已在 main.ts 中引入：@/assets/styles/main.scss
</script>

<style lang="scss">
// 全局样式已在 main.ts 中引入，此处无需重复
// 如需添加全局样式，建议写在以下文件中：
// - src/assets/styles/base/_common.scss （通用样式）
// - src/assets/styles/main.scss （主入口文件）

// 可选：添加全局过渡动画
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.3s ease;
}

.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}
</style>
