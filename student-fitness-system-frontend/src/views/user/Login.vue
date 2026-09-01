<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-header">
        <h2>{{ systemName }}</h2>
        <p>欢迎登录，请使用您的账号和密码登录</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <!-- 账号输入 -->
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户账号"
            clearable
            size="large"
          >
            <template #prepend>
              <el-icon><User /></el-icon>
              <span>账号</span>
            </template>
          </el-input>
        </el-form-item>

        <!-- 密码输入 -->
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入用户密码"
            show-password
            clearable
            size="large"
          >
            <template #prepend>
              <el-icon><Lock /></el-icon>
              <span>密码</span>
            </template>
          </el-input>
        </el-form-item>

        <!-- 记住我和忘记密码 -->
        <el-form-item class="remember-me">
          <div class="remember-me-content">
            <el-checkbox v-model="loginForm.rememberMe"> 记住我 </el-checkbox>
            <el-button type="text" @click="showForgetDialog" class="forget-btn">
              忘记密码？
            </el-button>
          </div>
        </el-form-item>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            class="login-button"
            size="large"
          >
            登录
          </el-button>
        </el-form-item>

        <!-- 提示信息 -->
        <div class="login-footer">
          <p>提示：初始密码为123456</p>
          <p>如有问题，请联系管理员：010-12345678</p>
        </div>
      </el-form>
    </el-card>

    <!-- 忘记密码对话框 -->
    <el-dialog title="忘记密码" v-model="forgetDialogVisible" width="400px">
      <div class="forget-password-dialog">
        <p>密码默认账号后六位；如果出错，请联系系统管理员重置密码！！！</p>
        <p></p>
        <p><strong>管理员电话：</strong>010-12345678</p>
        <p><strong>管理员邮箱：</strong>admin@school.edu.cn</p>
        <p><strong>办公地址：</strong>行政楼301室</p>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="forgetDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="copyContactInfo"> 复制联系方式 </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import type { LoginForm } from '@/types'
import { getDeviceId, getDeviceType } from '@/utils/device.ts'
import { login } from '@/utils/auth'
import { getHomePath } from '@/utils/router-helper'
import { useUserStore } from '@/store'

const router = useRouter()
const route = useRoute()
const systemName = ref('学生专业体质诊断信息管理系统')
const loading = ref(false)
const forgetDialogVisible = ref(false)
const loginFormRef = ref()

// 登录表单初始数据
const loginForm = reactive<LoginForm>({
  username: '',
  password: '',
  deviceType: getDeviceType(),
  deviceId: getDeviceId(),
  rememberMe: false,
})

// 表单验证规则
const loginRules = reactive<FormRules>({
  username: [
    {
      required: true,
      trigger: 'blur',
      validator: (_rule: unknown, value: string, callback: (error?: string | Error) => void) => {
        if (!value) {
          callback(new Error('请输入用户账号'))
        } else if (!/^\d{8,12}$/.test(value)) {
          callback(new Error('账号应为8-12位数字'))
        } else {
          callback()
        }
      },
    },
  ],
  password: [
    {
      required: true,
      trigger: 'blur',
      validator: (_rule: unknown, value: string, callback: (error?: string | Error) => void) => {
        if (!value) {
          callback(new Error('请输入用户密码'))
        } else if (value.length < 6) {
          callback(new Error('密码长度不能少于6位'))
        } else {
          callback()
        }
      },
    },
  ],
})

/**
 * 处理登录
 */
async function handleLogin(): Promise<void> {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    loading.value = true

    try {
      await login(loginForm)

      ElMessage.success('登录成功')

      const redirect = route.query.redirect as string

      if (redirect && redirect.startsWith('/')) {
        await router.replace(redirect) // 重定向到原始目标页面
      } else {
        const userStore = useUserStore()
        const homePath = getHomePath(userStore.userInfo)
        await router.replace(homePath)
      }
    } catch (error) {
      ElMessage.error('登录失败，' + (error as Error).message)
    } finally {
      loading.value = false
    }
  })
}

/**
 * 显示忘记密码对话框
 */
function showForgetDialog(): void {
  forgetDialogVisible.value = true
}

/**
 * 复制联系方式
 */
async function copyContactInfo(): Promise<void> {
  const contactInfo = `管理员电话：010-12345678\n管理员邮箱：admin@school.edu.cn\n办公地址：行政楼301室`

  try {
    // 现代剪贴板API（优先走这里）
    await navigator.clipboard.writeText(contactInfo)
    ElMessage.success('联系方式已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制文本')
    console.error('剪贴板复制失败：', error)
  }

  forgetDialogVisible.value = false
}

// 初始化
onMounted(() => {
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;
@use '@/assets/styles/abstracts/mixins' as *;

.login-container {
  @include flex-center;
  min-height: 95vh;
  position: relative;
  z-index: 1;
}

.login-card {
  width: 400px;
  border-radius: $border-radius-large;
  box-shadow: $shadow-dark;

  @include respond-to(xs) {
    width: 90%;
    max-width: 400px;
  }
}

.login-header {
  text-align: center;
  margin-bottom: 30px;

  h2 {
    color: $color-primary;
    margin-bottom: 10px;
    font-size: 24px;

    @include respond-to(xs) {
      font-size: 20px;
    }
  }

  p {
    color: $color-text-secondary;
    font-size: $font-size-base;
  }
}

.login-form {
  margin-top: 20px;
}

.remember-me-content {
  @include flex-between;
  width: 100%;
}

.forget-btn {
  padding-left: 10px;
}

.login-footer {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid $color-border-light;
  text-align: center;
  color: $color-text-secondary;
  font-size: 12px;

  p {
    margin: 5px 0;
  }
}

.forget-password-dialog {
  padding: 10px;

  p {
    margin: 10px 0;
    color: $color-text-regular;
    line-height: 1.6;
  }

  strong {
    color: $color-text-primary;
  }
}
</style>
