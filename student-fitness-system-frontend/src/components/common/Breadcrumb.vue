<template>
  <el-breadcrumb separator="/" class="breadcrumb">
    <el-breadcrumb-item :to="{ path: '/' }">
      <el-icon><HomeFilled /></el-icon>
      首页
    </el-breadcrumb-item>
    <el-breadcrumb-item v-for="item in breadcrumb" :key="item.path" :to="{ path: item.path }">
      {{ item.meta.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts" name="Breadcrumb">
import type { BreadcrumbItem } from '@/types'

defineProps<{
  breadcrumb: BreadcrumbItem[]
}>()
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;

.breadcrumb {
  color: rgba($color-white, 0.8);
  font-size: $font-size-base;

  // 覆盖 Element Plus 默认样式
  :deep(.el-breadcrumb__inner) {
    color: rgba($color-white, 0.8) !important;

    &:hover {
      color: $color-white !important;
    }
  }

  :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
    color: $color-white !important;
    font-weight: bold;
  }

  :deep(.el-breadcrumb__separator) {
    color: rgba($color-white, 0.6);
  }
}
</style>
