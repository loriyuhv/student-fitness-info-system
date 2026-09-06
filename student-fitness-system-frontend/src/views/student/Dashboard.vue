<template>
  <div class="student-dashboard">
    <el-row :gutter="16">
      <!-- 个人信息卡片 -->
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="panel-card profile-card">
          <template #header>
            <span class="card-title">个人信息</span>
          </template>

          <el-skeleton v-if="loading" :rows="4" animated />
          <div v-else class="profile-body">
            <div class="avatar">{{ nickname.charAt(0) || '学' }}</div>
            <div class="profile-detail">
              <div class="name-row">
                <span class="name">{{ nickname }}</span>
                <el-tag size="small" type="primary">学生</el-tag>
                <el-tag v-if="gender" size="small" type="info">{{ genderText(gender) }}</el-tag>
              </div>
              <div class="row"><span class="label">学号</span>{{ studentNo }}</div>
              <div class="row"><span class="label">班级</span>{{ className }}</div>
              <div class="row"><span class="label">学院</span>{{ college }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 最近一次体测 -->
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="panel-card latest-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">最近一次体测</span>
              <div class="header-actions">
                <el-button link type="primary" @click="router.push('/student/history')">
                  历史记录
                </el-button>
                <el-button link type="primary" @click="router.push('/student/diagnosis')">
                  诊断报告
                </el-button>
              </div>
            </div>
          </template>

          <el-skeleton v-if="loading" :rows="3" animated />
          <el-empty v-else-if="!latestTest" description="暂无体测记录，请等待老师录入" />
          <div v-else class="latest-body">
            <div class="score-block">
              <div class="score-label">总分</div>
              <div class="score-value" :style="{ color: scoreColor(latestTest.totalScore) }">
                {{ latestTest.totalScore }}
              </div>
            </div>
            <el-divider direction="vertical" class="score-divider" />
            <div class="latest-meta">
              <div class="meta-row">
                等级
                <el-tag :type="levelTagType(latestTest.level)" size="small">
                  {{ latestTest.level }}
                </el-tag>
              </div>
              <div class="meta-row">
                第 {{ latestTest.testRound }} 次
                <el-tag size="small" type="info">{{ latestTest.testTime }}</el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 关键指标摘要 -->
    <el-card shadow="never" class="panel-card indicators-card">
      <template #header>
        <span class="card-title">关键指标摘要</span>
      </template>

      <el-skeleton v-if="loading" :rows="2" animated />
      <el-row v-else :gutter="16">
        <el-col v-for="item in indicators" :key="item.label" :xs="12" :md="6">
          <div class="indicator-tile">
            <div class="indicator-value">
              {{ item.value }}
              <span class="indicator-unit">{{ item.unit }}</span>
            </div>
            <div class="indicator-label">{{ item.label }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store'
import type { StudentDashboard } from '@/types/student'
import { genderText, levelTagType, scoreColor } from '@/utils/student'
import { getDashboard } from '@/api/student'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const data = ref<StudentDashboard | null>(null)

// 身份信息优先取登录态真实数据（用户名即学号），班级/学院等由档案数据兜底
const nickname = computed(() => userStore.userInfo?.nickname || data.value?.student.name || '--')
const studentNo = computed(() => userStore.userInfo?.username || data.value?.student.studentNo || '--')
const className = computed(() => data.value?.student.className || '--')
const college = computed(() => data.value?.student.college || '--')
const gender = computed(() => data.value?.student.gender)

const latestTest = computed(() => data.value?.latestTest ?? null)

const indicators = computed(() => {
  const ind = data.value?.indicators
  if (!ind) return []
  return [
    { label: '身高', value: ind.height, unit: 'cm' },
    { label: '体重', value: ind.weight, unit: 'kg' },
    { label: 'BMI', value: ind.bmi, unit: 'kg/m²' },
    { label: '肺活量', value: ind.vitalCapacity, unit: 'ml' },
  ]
})

onMounted(async () => {
  try {
    data.value = await getDashboard()
  } catch (error) {
    ElMessage.error((error as Error).message || '仪表盘数据加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;

.student-dashboard {
  min-height: calc(100vh - 140px);
  background-color: $color-bg-primary;
}

.panel-card {
  margin-bottom: 16px;
}

.card-title {
  font-weight: 600;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-actions {
  display: flex;
  gap: 4px;
}

// 个人信息
.profile-body {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: #fff;
  background: linear-gradient(135deg, $color-primary 0%, #6a5acd 100%);
}

.profile-detail {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;

  .name {
    font-size: 20px;
    font-weight: 600;
  }
}

.row {
  display: flex;
  gap: 8px;
  margin-top: 4px;
  font-size: 14px;

  .label {
    color: $color-text-secondary;
    flex-shrink: 0;
  }
}

// 最近一次体测
.latest-body {
  display: flex;
  align-items: center;
  padding: 8px 4px;
}

.score-block {
  text-align: center;
  min-width: 120px;
}

.score-label {
  font-size: 13px;
  color: $color-text-secondary;
  margin-bottom: 4px;
}

.score-value {
  font-size: 52px;
  font-weight: 700;
  line-height: 1.1;
}

.score-divider {
  height: 64px;
  margin: 0 24px;
}

.latest-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: $color-text-regular;
}

// 关键指标
.indicator-tile {
  padding: 18px;
  text-align: center;
  background: $color-bg-secondary;
  border-radius: 8px;
}

.indicator-value {
  font-size: 28px;
  font-weight: 700;
  color: $color-primary;

  .indicator-unit {
    font-size: 13px;
    font-weight: 400;
    color: $color-text-secondary;
    margin-left: 2px;
  }
}

.indicator-label {
  margin-top: 6px;
  font-size: 13px;
  color: $color-text-secondary;
}
</style>
