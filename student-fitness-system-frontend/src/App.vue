<template>
  <!-- Element Plus 按需引入后不再 app.use(ElementPlus)，
       中文化通过 el-config-provider 全局提供 -->
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
import zhCn from 'element-plus/es/locale/lang/zh-cn'
</script>

<style lang="scss">
// 全局样式已在 main.ts 中引入：@/assets/styles/main.scss
// 如需添加全局样式，建议写在以下文件中：
// - src/assets/styles/base/_common.scss （通用样式）
// - src/assets/styles/main.scss （主入口文件）

// 页面过渡动画
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.3s ease;
}

.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}
</style>
