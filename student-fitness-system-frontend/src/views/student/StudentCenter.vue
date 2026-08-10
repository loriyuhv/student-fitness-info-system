<template>
  <div class="student-center-container">
    <!-- 学生基本信息卡片 -->
    <el-card shadow="never" class="student-info-card">
      <div class="student-info-header">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <el-avatar :size="100" :src="studentInfo.avatar" class="student-avatar">
            {{ studentInfo.userName?.charAt(0) || '学' }}
          </el-avatar>
          <div class="avatar-actions">
            <el-button type="text" @click="changeAvatar">
              <el-icon><Camera /></el-icon>
              更换头像
            </el-button>
          </div>
        </div>

        <!-- 基本信息区域 -->
        <div class="basic-info-section">
          <div class="name-section">
            <h2>{{ studentInfo.userName }}</h2>
            <el-tag type="success" size="small">学生</el-tag>
          </div>

          <div class="info-grid">
            <div class="info-row">
              <div class="info-item">
                <span class="info-label"
                  ><el-icon><User /></el-icon> 学号：</span
                >
                <span class="info-value">{{ studentInfo.userAccount }}</span>
              </div>
              <div class="info-item">
                <span class="info-label"
                  ><el-icon
                    ><Male v-if="studentInfo.gender === Gender.MALE" /><Female v-else
                  /></el-icon>
                  性别：</span
                >
                <span class="info-value">{{
                  studentInfo.gender === Gender.MALE ? '男' : '女'
                }}</span>
              </div>
            </div>

            <div class="info-row">
              <div class="info-item">
                <span class="info-label"
                  ><el-icon><OfficeBuilding /></el-icon> 班级：</span
                >
                <span class="info-value">{{ studentInfo.className || '--' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label"
                  ><el-icon><Phone /></el-icon> 联系电话：</span
                >
                <span class="info-value">{{ studentInfo.phone || '未填写' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 评分区域 -->
        <!-- <div class="score-section">
          <div class="total-score-display">
            <div class="score-label">最新测试总分</div>
            <div class="score-value" :class="getScoreClass(latestAssessment.totalScore)">
              {{ latestAssessment.totalScore || '--' }}
            </div>
          </div>

          <div class="fitness-result">
            <el-tag :type="getResultTagType(latestAssessment.weightLevel)" size="medium">
              {{ latestAssessment.weightLevel || '暂无测试' }}
            </el-tag>
          </div>

          <div class="assessment-date">测试时间：{{ formatDate(latestAssessment.testTime) }}</div>
        </div> -->
      </div>

      <!-- 身体指标概览 -->
      <div class="body-indicators">
        <el-row :gutter="20">
          <el-col :span="6" :xs="24" :sm="12" :md="6">
            <div class="indicator-card">
              <div class="indicator-icon height">
                <el-icon><Sort /></el-icon>
              </div>
              <div class="indicator-content">
                <div class="indicator-value">{{ latestAssessment.height || '--' }} cm</div>
                <div class="indicator-label">身高</div>
              </div>
            </div>
          </el-col>

          <el-col :span="6" :xs="24" :sm="12" :md="6">
            <div class="indicator-card">
              <div class="indicator-icon weight">
                <el-icon><ScaleToOriginal /></el-icon>
              </div>
              <div class="indicator-content">
                <div class="indicator-value">{{ latestAssessment.weight || '--' }} kg</div>
                <div class="indicator-label">体重</div>
              </div>
            </div>
          </el-col>

          <el-col :span="6" :xs="24" :sm="12" :md="6">
            <div class="indicator-card">
              <div class="indicator-icon bmi">
                <el-icon><DataAnalysis /></el-icon>
              </div>
              <div class="indicator-content">
                <div class="indicator-value">
                  {{ calculateBMI(latestAssessment.height, latestAssessment.weight) }}
                </div>
                <div class="indicator-label">BMI指数</div>
              </div>
            </div>
          </el-col>

          <!-- <el-col :span="6" :xs="24" :sm="12" :md="6">
            <div class="indicator-card">
              <div class="indicator-icon level">
                <el-icon><Medal /></el-icon>
              </div>
              <div class="indicator-content">
                <div class="indicator-value">
                  <el-tag :type="getWeightLevelTagType(latestAssessment.weightLevel)" size="small">
                    {{ latestAssessment.weightLevel || '--' }}
                  </el-tag>
                </div>
                <div class="indicator-label">体重等级</div>
              </div>
            </div>
          </el-col> -->
        </el-row>
      </div>
    </el-card>

    <!-- 导航标签页 -->
    <!-- <div class="content-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="体质健康评价详情" name="latest">
          <div class="tab-content">
            <LatestAssessment
              :assessment="latestAssessment"
              :gender="studentInfo.gender"
              @view-detail="viewAssessmentDetail"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="历史测试记录" name="history">
          <div class="tab-content" v-if="activeTab === 'history'">
            <StudentHistory
              :user-account="studentInfo.userAccount"
              :gender="studentInfo.gender"
              @view-detail="viewAssessmentDetail"
              @refresh="refreshData"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="体质趋势分析" name="trend">
          <div class="tab-content">
            <TrendAnalysis
              v-if="historyData.length && activeTab === 'trend'"
              :history-data="historyData"
              :student-info="studentInfo"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="健康建议与改进" name="suggestion">
          <div class="tab-content">
            <HealthSuggestion
              :assessment="latestAssessment"
              :student-info="studentInfo"
              @view-suggestion="viewSuggestionPDF"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div> -->

    <!-- 快速操作浮动按钮 -->
    <!-- <div class="floating-actions">
      <el-tooltip content="查看运动处方" placement="left">
        <el-button type="primary" circle size="medium" @click="viewSuggestionPDF">
          <el-icon><Reading /></el-icon>
        </el-button>
      </el-tooltip>

      <el-tooltip content="打印测试报告" placement="left">
        <el-button type="success" circle size="medium" @click="printReport">
          <el-icon><Printer /></el-icon>
        </el-button>
      </el-tooltip>

      <el-tooltip content="刷新数据" placement="left">
        <el-button type="info" circle size="medium" @click="refreshData" :loading="refreshing">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </el-tooltip>
    </div> -->

    <!-- 测试详情弹窗 -->
    <!-- <el-dialog
      :title="`测试详情 - ${formatDate(currentDetail.testTime)}`"
      v-model="detailDialogVisible"
      width="800px"
      :before-close="handleCloseDetailDialog"
    >
      <AssessmentDetail :assessment="currentDetail" :gender="studentInfo.gender" />

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            type="primary"
            @click="viewSuggestionPDF(currentDetail)"
            v-if="currentDetail.physicalFitnessResult"
          >
            查看建议
          </el-button>
        </span>
      </template>
    </el-dialog> -->
  </div>
</template>

<script setup lang="ts">
// import { ref, reactive, onMounted } from 'vue'
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Camera,
  User,
  Male,
  Female,
  OfficeBuilding,
  Phone,
  Sort,
  ScaleToOriginal,
  DataAnalysis,
  // Medal,
  // Reading,
  // Printer,
  // Refresh,
} from '@element-plus/icons-vue'
// import student from '@/api/student'
import type { AssessmentRecord, UserInfo } from '@/types'
import { UserType, Gender } from '@/types'
// import LatestAssessment from './components/LatestAssessment.vue'
// import TrendAnalysis from './components/TrendAnalysis.vue'
// import HealthSuggestion from './components/HealthSuggestion.vue'
// import AssessmentDetail from './components/AssessmentDetail.vue'
// import StudentHistory from './components/StudentHistory.vue'

// 用户信息
const studentInfo = reactive<UserInfo>({
  username: '',
  userType: UserType.STUDENT,
  //avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
})

// 最新测评数据
const latestAssessment = reactive<Partial<AssessmentRecord>>({})

// 历史数据
// const historyData = ref<AssessmentRecord[]>([])

// 当前查看的详情
// const currentDetail = reactive<Partial<AssessmentRecord>>({})

// 状态控制
// const activeTab = ref('latest')
// const refreshing = ref(false)
// const detailDialogVisible = ref(false)

/**
 * 加载学生信息
 */
function loadStudentInfo() {
  const userInfoStr = localStorage.getItem('fitness-user-info')
  if (userInfoStr) {
    const info = JSON.parse(userInfoStr)
    Object.assign(studentInfo, info)
  }
}

/**
 * 加载最新测评数据
 */
// async function loadLatestAssessment() {
//   try {
//     const res = await student.getLatestAssessment(studentInfo.userAccount)
//     Object.assign(latestAssessment, res)
//   } catch (error) {
//     console.error('加载最新测试数据失败:', error)
//     ElMessage.error('获取测试数据失败')
//   }
// }

/**
 * 加载历史数据
 */
// async function loadHistoryData() {
//   try {
//     const res = await student.getHistoryData(studentInfo.userAccount)
//     historyData.value = res || []
//   } catch (error) {
//     console.error('加载历史数据失败:', error)
//     ElMessage.error('获取历史测试数据失败')
//   }
// }

/**
 * 查看测试详情
 */
// function viewAssessmentDetail(assessment: AssessmentRecord) {
//   Object.assign(currentDetail, assessment || latestAssessment)
//   detailDialogVisible.value = true
// }

/**
 * 查看运动处方PDF
 */
// function viewSuggestionPDF(record?: AssessmentRecord) {
//   const data = record || latestAssessment
//   if (!data.sportPrescription) {
//     ElMessage.warning('暂无运动处方')
//     return
//   }

//   const pdfUrl = `/suggestions/advice${data.sportPrescription}.pdf`
//   window.open(pdfUrl, '_blank')
// }

/**
 * 刷新数据
 */
// async function refreshData() {
//   refreshing.value = true

//   try {
//     await Promise.all([loadLatestAssessment(), loadHistoryData()])
//     ElMessage.success('数据已刷新')
//   } catch (error) {
//     console.error('刷新数据失败:', error)
//   } finally {
//     refreshing.value = false
//   }
// }

/**
 * 打印报告
 */
// function printReport() {
//   window.print()
// }

/**
 * 更换头像
 */
function changeAvatar() {
  ElMessage.info('头像更换功能开发中...')
}

/**
 * 标签页切换
 */
// function handleTabClick(tab: any) {
//   console.log('切换到标签页:', tab.name)
// }

/**
 * 关闭详情弹窗
 */
// function handleCloseDetailDialog() {
//   detailDialogVisible.value = false
//   Object.assign(currentDetail, {})
// }

/**
 * 计算 BMI
 */
function calculateBMI(height?: number, weight?: number): string {
  if (!height || !weight) return '--'
  const heightInMeter = height / 100
  const bmi = weight / (heightInMeter * heightInMeter)
  return bmi.toFixed(1)
}

/**
 * 格式化日期
 */
// function formatDate(timestamp?: string | number): string {
//   if (!timestamp) return '--'
//   const date = new Date(timestamp)
//   return date.toLocaleDateString('zh-CN', {
//     year: 'numeric',
//     month: 'long',
//     day: 'numeric',
//   })
// }

/**
 * 获取分数样式类
 */
// function getScoreClass(score?: number): string {
//   if (!score && score !== 0) return ''
//   if (score >= 90) return 'score-excellent'
//   if (score >= 80) return 'score-good'
//   if (score >= 60) return 'score-pass'
//   return 'score-fail'
// }

/**
 * 获取结果标签类型
 */
// function getResultTagType(result?: string): string {
//   if (!result) return 'info'
//   const map: Record<string, string> = {
//     优秀: 'success',
//     良好: '',
//     及格: 'warning',
//     不及格: 'danger',
//     偏瘦: 'info',
//     正常: 'success',
//     超重: 'warning',
//     肥胖: 'danger',
//   }
//   return map[result] || 'info'
// }

/**
 * 获取体重等级标签类型
 */
// function getWeightLevelTagType(level?: string): string {
//   if (!level) return 'info'
//   const map: Record<string, string> = {
//     偏瘦: 'info',
//     正常: 'success',
//     超重: 'warning',
//     肥胖: 'danger',
//   }
//   return map[level] || 'info'
// }

// 初始化
onMounted(() => {
  loadStudentInfo()
  // loadLatestAssessment()
  // loadHistoryData()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/abstracts/variables' as *;
@use '@/assets/styles/abstracts/mixins' as *;

.student-center-container {
  padding: 20px;
  background-color: $color-bg-primary;
  min-height: calc(100vh - #{$header-height});

  @include respond-to(sm) {
    padding: 10px;
  }
}

.name-section {
  @include flex-start;
  gap: 15px;
  margin-bottom: 25px;

  h2 {
    margin: 0;
    font-size: 28px;
    font-weight: bold;
    color: $color-white;

    @include respond-to(sm) {
      font-size: 24px;
    }
  }
}

.info-grid {
  background: rgba($color-white, 0.1);
  padding: 20px;
  border-radius: $border-radius-medium;
  backdrop-filter: blur(10px);
}

.info-row {
  @include flex-start;
  margin-bottom: 15px;

  &:last-child {
    margin-bottom: 0;
  }

  @include respond-to(sm) {
    flex-direction: column;
    gap: 15px;
  }
}

.info-item {
  flex: 1;
  @include flex-start;

  .info-label {
    color: rgba($color-white, 0.8);
    margin-right: 10px;
    min-width: 100px;
    font-size: $font-size-base;

    @include flex-start;
    gap: 5px;
  }

  .info-value {
    color: $color-white;
    font-weight: 500;
    font-size: 16px;
  }
}

.total-score-display {
  text-align: center;
  margin-bottom: 15px;

  .score-value {
    font-size: 48px;
    font-weight: bold;
    line-height: 1;

    @include respond-to(sm) {
      font-size: 36px;
    }
  }
}

.assessment-date {
  color: rgba($color-white, 0.7);
  font-size: 12px;
  margin-top: 10px;
}

.body-indicators {
  padding: 30px;
  background: $color-white;
}

.content-tabs {
  background: $color-white;
  border-radius: $border-radius-large;
  overflow: hidden;

  // 覆盖 Element Plus 样式
  :deep(.el-tabs__header) {
    margin: 0;
    padding: 0 30px;
    background: $color-white;
    border-bottom: 1px solid $color-border-light;
  }

  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }

  :deep(.el-tabs__item) {
    height: 60px;
    line-height: 60px;
    font-size: $font-size-extra-large;
    font-weight: 500;

    @include respond-to(sm) {
      padding: 0 10px;
      font-size: $font-size-base;
    }
  }

  :deep(.el-tabs__item.is-active) {
    color: $color-primary;
  }

  :deep(.el-tabs__active-bar) {
    background-color: $color-primary;
    height: 3px;
  }
}

.tab-content {
  padding: 30px;
  min-height: 400px;

  @include respond-to(sm) {
    padding: 15px;
  }
}
</style>
