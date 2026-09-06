<template>
  <div class="diagnosis-report">
    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">诊断报告与运动处方</span>
          <div class="header-actions">
            <el-button link type="primary" @click="router.push('/student/dashboard')">
              返回仪表盘
            </el-button>
            <el-button link @click="router.push('/student/history')">查看历史记录</el-button>
          </div>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="8" animated />

      <el-empty v-else-if="!report" description="暂无诊断报告，请先完成一次体测" />

      <template v-else>
        <!-- 报告概览 -->
        <el-descriptions title="报告概览" :column="3" border class="section">
          <el-descriptions-item label="总分">{{ report.totalScore }} 分</el-descriptions-item>
          <el-descriptions-item label="综合等级">
            <el-tag :type="levelTagType(report.level)" size="small">{{ report.level }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="生成时间">{{ report.generateTime }}</el-descriptions-item>
        </el-descriptions>

        <!-- 核心结论：K 值 + 体质类型 -->
        <div class="core-row">
          <div class="core-card highlight">
            <div class="core-label">K 值（聚类号）</div>
            <div class="core-value">{{ report.kValue }}</div>
            <div class="core-tip">基于历史体测数据的体质聚类标识</div>
          </div>
          <div class="core-card highlight">
            <div class="core-label">体质类型</div>
            <div class="core-value type">{{ report.physiqueType }}</div>
            <div class="core-tip">当前体质画像归类结果</div>
          </div>
        </div>

        <!-- 运动处方 -->
        <div class="section-title">运动处方建议</div>
        <el-card shadow="never" class="prescription-card">
          <ul class="prescription-list">
            <li v-for="(item, index) in report.sportPrescription" :key="index">
              <span class="prescription-index">{{ index + 1 }}</span>
              {{ item }}
            </li>
          </ul>
        </el-card>

        <!-- 健康风险提示 -->
        <div class="section-title">健康风险提示</div>
        <el-row :gutter="16">
          <el-col v-for="risk in report.healthRisks" :key="risk.title" :xs="24" :md="8">
            <el-card shadow="never" class="risk-card" :class="`risk-${risk.severity}`">
              <div class="risk-header">
                <el-icon class="risk-icon"><WarningFilled /></el-icon>
                <span class="risk-title">{{ risk.title }}</span>
                <el-tag size="small" :type="riskSeverityTagType(risk.severity)" class="risk-tag">
                  {{ riskSeverityText(risk.severity) }}
                </el-tag>
              </div>
              <div class="risk-desc">{{ risk.risk }}</div>
              <div class="risk-suggestion">
                <span class="suggestion-label">建议</span>
                {{ risk.suggestion }}
              </div>
            </el-card>
          </el-col>
        </el-row>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import type { DiagnosisReport } from '@/types/student'
import { WarningFilled } from '@element-plus/icons-vue'
import { levelTagType } from '@/utils/student'
import { getDiagnosis } from '@/api/student'

const router = useRouter()

const loading = ref(true)
const report = ref<DiagnosisReport | null>(null)

function riskSeverityTagType(severity: DiagnosisReport['healthRisks'][number]['severity']) {
  switch (severity) {
    case 'high':
      return 'danger' as const
    case 'medium':
      return 'warning' as const
    default:
      return 'info' as const
  }
}

function riskSeverityText(severity: DiagnosisReport['healthRisks'][number]['severity']): string {
  switch (severity) {
    case 'high':
      return '高风险'
    case 'medium':
      return '中风险'
    default:
      return '低风险'
  }
}

onMounted(async () => {
  try {
    report.value = await getDiagnosis()
  } catch (error) {
    ElMessage.error((error as Error).message || '诊断报告加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;
@use '@/assets/styles/abstracts/mixins' as *;

.diagnosis-report {
  background-color: $color-bg-primary;
  min-height: calc(100vh - 140px);
}

.panel-card {
  background-color: $color-white;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.section {
  margin-bottom: 8px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  margin: 24px 0 12px;
  padding-left: 10px;
  border-left: 3px solid $color-primary;
}

// 核心结论高亮卡
.core-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 20px;

  @include respond-to(sm) {
    grid-template-columns: 1fr;
  }
}

.core-card {
  text-align: center;
  padding: 28px 16px;
  border-radius: 10px;

  &.highlight {
    background: linear-gradient(135deg, rgba($color-primary, 0.08) 0%, rgba(106, 90, 205, 0.1) 100%);
    border: 1px solid rgba($color-primary, 0.2);
  }

  .core-label {
    font-size: 13px;
    color: $color-text-secondary;
  }

  .core-value {
    font-size: 42px;
    font-weight: 700;
    line-height: 1.2;
    margin: 8px 0 6px;
    color: $color-primary;

    &.type {
      font-size: 26px;
    }
  }

  .core-tip {
    font-size: 12px;
    color: $color-text-secondary;
  }
}

// 运动处方
.prescription-card {
  background-color: $color-bg-secondary;
  border: none;
}

.prescription-list {
  margin: 0;
  padding: 0;
  list-style: none;

  li {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    line-height: 1.8;
    padding: 6px 0;
    color: $color-text-regular;
  }
}

.prescription-index {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  margin-top: 5px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
  background: $color-primary;
}

// 健康风险
.risk-card {
  margin-bottom: 16px;
  border-left: 4px solid $color-info;

  &.risk-high {
    border-left-color: $color-danger;
  }

  &.risk-medium {
    border-left-color: $color-warning;
  }

  &.risk-low {
    border-left-color: $color-info;
  }
}

.risk-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.risk-icon {
  font-size: 18px;
  color: $color-warning;
}

.risk-title {
  font-weight: 600;
}

.risk-tag {
  margin-left: auto;
}

.risk-desc {
  margin-top: 10px;
  font-size: 13px;
  color: $color-text-regular;
  line-height: 1.7;
}

.risk-suggestion {
  margin-top: 10px;
  padding: 8px 10px;
  background: $color-bg-secondary;
  border-radius: 6px;
  font-size: 13px;
  color: $color-text-regular;
  line-height: 1.7;

  .suggestion-label {
    color: $color-primary;
    font-weight: 600;
    margin-right: 6px;
  }
}
</style>
