<template>
  <div class="history-list">
    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">历史体测记录</span>
          <el-button link type="primary" @click="router.push('/student/dashboard')">
            返回仪表盘
          </el-button>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="6" animated />

      <el-empty v-else-if="!records.length" description="暂无体测记录" />

      <el-table v-else :data="records" stripe>
        <el-table-column label="序号" type="index" width="70" align="center" />
        <el-table-column label="体测时间" min-width="170">
          <template #default="{ row }">
            <span>{{ row.testTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="第几次" width="90" align="center">
          <template #default="{ row }">第 {{ row.testRound }} 次</template>
        </el-table-column>
        <el-table-column label="总分" width="110" align="center">
          <template #default="{ row }">
            <span :style="{ color: scoreColor(row.totalScore), fontWeight: 600 }">
              {{ row.totalScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.level)" size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row.recordId)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import type { TestRecord } from '@/types/student'
import { levelTagType, scoreColor } from '@/utils/student'
import { getHistoryList } from '@/api/student'

const router = useRouter()

const loading = ref(true)
const records = ref<TestRecord[]>([])

function viewDetail(recordId: number): void {
  router.push(`/student/history/${recordId}`)
}

onMounted(async () => {
  try {
    records.value = await getHistoryList()
  } catch (error) {
    ElMessage.error((error as Error).message || '历史记录加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;

.history-list {
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
</style>
