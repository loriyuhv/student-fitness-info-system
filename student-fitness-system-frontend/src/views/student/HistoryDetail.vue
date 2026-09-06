<template>
  <div class="history-detail">
    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">体测详情</span>
          <div class="header-actions">
            <el-button link @click="router.push('/student/history')">返回列表</el-button>
            <el-button link type="primary" @click="router.push('/student/diagnosis')">
              查看诊断报告
            </el-button>
          </div>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="8" animated />

      <el-empty
        v-else-if="loadFailed"
        description="未找到该次体测记录，可能已被删除"
      >
        <el-button type="primary" @click="router.replace('/student/history')">
          返回历史列表
        </el-button>
      </el-empty>

      <template v-else-if="detail">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border class="section">
          <el-descriptions-item label="姓名">{{ detail.student.name }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ detail.student.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ detail.student.className }}</el-descriptions-item>
          <el-descriptions-item label="体测时间">{{ detail.testTime }}</el-descriptions-item>
          <el-descriptions-item label="第几次体测">第 {{ detail.testRound }} 次</el-descriptions-item>
          <el-descriptions-item label="综合评定">
            <el-tag :type="levelTagType(detail.level)" size="small">{{ detail.level }}</el-tag>
            <span class="total-score" :style="{ color: scoreColor(detail.totalScore) }">
              {{ detail.totalScore }} 分
            </span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 项目细分得分 -->
        <div class="section-title">各项目细分得分</div>
        <el-table :data="detail.items" stripe>
          <el-table-column label="项目" min-width="150">
            <template #default="{ row }">{{ row.itemName }}</template>
          </el-table-column>
          <el-table-column label="原始成绩" width="140" align="center">
            <template #default="{ row }">{{ row.itemValue }} {{ row.unit }}</template>
          </el-table-column>
          <el-table-column label="得分" width="100" align="center">
            <template #default="{ row }">
              <span :style="{ color: scoreColor(row.score), fontWeight: 600 }">{{ row.score }}</span>
            </template>
          </el-table-column>
          <el-table-column label="附加分" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.bonus > 0" size="small" type="success">+{{ row.bonus }}</el-tag>
              <span v-else>--</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 综合诊断结论 -->
        <div class="section-title">综合诊断结论</div>
        <el-card shadow="never" class="diagnosis-card">
          <div class="diagnosis-line">
            <span class="label">体质类型</span>
            <el-tag type="warning" size="large">{{ detail.summary.physiqueType }}</el-tag>
            <span class="label k-value">K 值</span>
            <el-tag type="info" size="large">{{ detail.summary.kValue }}</el-tag>
          </div>
          <div class="prescription">
            <div class="prescription-title">运动处方</div>
            <ul class="prescription-list">
              <li v-for="(item, index) in detail.summary.sportPrescription" :key="index">
                {{ item }}
              </li>
            </ul>
          </div>
        </el-card>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import type { TestDetail } from '@/types/student'
import { levelTagType, scoreColor } from '@/utils/student'
import { getHistoryDetail } from '@/api/student'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const loadFailed = ref(false)
const detail = ref<TestDetail | null>(null)

async function loadDetail(recordId: number): Promise<void> {
  loading.value = true
  loadFailed.value = false
  try {
    detail.value = await getHistoryDetail(recordId)
  } catch (error) {
    console.error('加载体测详情失败：', error)
    loadFailed.value = true
    ElMessage.error((error as Error).message || '详情加载失败')
  } finally {
    loading.value = false
  }
}

// 初始加载 + 同页切换记录时重新加载
watch(
  () => route.params.id,
  (id) => {
    const recordId = Number(id)
    if (Number.isNaN(recordId)) {
      loadFailed.value = true
      loading.value = false
      return
    }
    loadDetail(recordId)
  },
  { immediate: true },
)
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;

.history-detail {
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

.total-score {
  margin-left: 8px;
  font-weight: 600;
}

.section {
  margin-bottom: 8px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  margin: 20px 0 12px;
  padding-left: 10px;
  border-left: 3px solid $color-primary;
}

.diagnosis-card {
  background-color: $color-bg-secondary;
  border: none;
}

.diagnosis-line {
  display: flex;
  align-items: center;
  gap: 12px;

  .label {
    font-size: 14px;
    color: $color-text-secondary;
  }

  .k-value {
    margin-left: 24px;
  }
}

.prescription {
  margin-top: 16px;

  .prescription-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 8px;
  }

  .prescription-list {
    margin: 0;
    padding-left: 18px;
    line-height: 1.9;
    color: $color-text-regular;
  }
}
</style>
