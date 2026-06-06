<template>
  <div class="page-wrapper">
    <!-- 导航栏 -->
    <el-header class="nav-header">
      <div class="nav-content">
        <div class="nav-left">
          <el-icon class="logo-icon" :size="32"><Reading /></el-icon>
          <span class="logo-text">AI 小说转剧本工具</span>
        </div>
        <div class="nav-center">
          <span class="nav-link" @click="$router.push('/')">首页</span>
          <span class="nav-link" @click="$router.push('/scripts')">我的剧本</span>
          <span class="nav-link">使用指南</span>
        </div>
        <div class="nav-right">
          <el-button type="primary" round @click="$router.push('/novel/input')">
            <el-icon><Upload /></el-icon>
            上传小说
          </el-button>
        </div>
      </div>
    </el-header>

    <el-main class="main">
      <div class="page-header">
        <el-button text @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <h2>选择章节</h2>
        <span class="novel-title">{{ novelTitle }}</span>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-wrapper">
        <el-skeleton :rows="6" animated />
      </div>

      <!-- Chapter List -->
      <template v-else-if="chapters.length > 0">
        <el-card class="summary-card" shadow="never">
          <div class="summary-row">
            <span>共检测到 <strong>{{ chapters.length }}</strong> 章</span>
            <span>已选择 <strong>{{ selectedIndices.length }}</strong> 章</span>
            <span v-if="selectedChars > 0">{{ selectedChars }} 字符</span>
          </div>
          <div class="action-row">
            <el-button size="small" @click="selectAll">全选</el-button>
            <el-button size="small" @click="deselectAll">取消全选</el-button>
            <el-button
              type="primary"
              size="default"
              :disabled="selectedIndices.length === 0"
              :loading="converting"
              @click="handleConvert"
            >
              {{ converting ? '转换中...' : `转换所选章节（${selectedIndices.length} 章）` }}
            </el-button>
          </div>
        </el-card>

        <el-checkbox-group v-model="selectedIndices" class="chapter-list">
          <el-card
            v-for="ch in chapters"
            :key="ch.index"
            :class="['chapter-card', { selected: selectedIndices.includes(ch.index) }]"
            shadow="never"
            @click="toggleChapter(ch.index)"
          >
            <el-checkbox :label="ch.index" class="chapter-checkbox" @click.stop>
              <template #default>
                <div class="chapter-info">
                  <div class="chapter-title">{{ ch.title }}</div>
                  <div class="chapter-preview">{{ ch.preview }}</div>
                  <div class="chapter-meta">{{ ch.charCount }} 字符</div>
                </div>
              </template>
            </el-checkbox>
          </el-card>
        </el-checkbox-group>
      </template>

      <!-- Empty -->
      <el-empty v-else description="无法解析章节内容" />

      <!-- Conversion Progress Dialog -->
      <el-dialog v-model="showProgress" title="AI 剧本生成中" :close-on-click-modal="false" width="400px">
        <div class="progress-content">
          <el-progress type="circle" :percentage="progressPercent" :status="progressStatus" />
          <p class="progress-text">{{ progressText }}</p>
        </div>
      </el-dialog>
    </el-main>

    <!-- 页脚 -->
    <footer class="page-footer">
      <p>© 2024 AI 小说转剧本工具 · 基于 Spring Boot & Vue 3 构建</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading, Upload, ArrowLeft } from '@element-plus/icons-vue'
import { getChapters, getNovel, convertNovel } from '../api/novel'

const router = useRouter()
const route = useRoute()

const novelId = computed(() => Number(route.params.id))
const novelTitle = ref('')
const chapters = ref([])
const selectedIndices = ref([])
const loading = ref(true)
const converting = ref(false)
const showProgress = ref(false)
const progressPercent = ref(0)
const progressStatus = ref('')
const progressText = ref('')

const selectedChars = computed(() => {
  let total = 0
  for (const idx of selectedIndices.value) {
    const ch = chapters.value.find(c => c.index === idx)
    if (ch) total += ch.charCount
  }
  return total
})

onMounted(async () => {
  try {
    const novelRes = await getNovel(novelId.value)
    novelTitle.value = novelRes.title || `小说 #${novelId.value}`

    const chapterRes = await getChapters(novelId.value)
    chapters.value = chapterRes || []

    selectedIndices.value = chapters.value.map(c => c.index)
  } catch (err) {
    ElMessage.error('加载章节失败: ' + (err.message || '未知错误'))
  } finally {
    loading.value = false
  }
})

function toggleChapter(index) {
  const idx = selectedIndices.value.indexOf(index)
  if (idx >= 0) {
    selectedIndices.value.splice(idx, 1)
  } else {
    selectedIndices.value.push(index)
  }
}

function selectAll() {
  selectedIndices.value = chapters.value.map(c => c.index)
}

function deselectAll() {
  selectedIndices.value = []
}

async function handleConvert() {
  if (selectedIndices.value.length === 0) {
    ElMessage.warning('请至少选择一个章节')
    return
  }

  if (selectedIndices.value.length < 3) {
    ElMessage.warning('建议至少选择 3 个章节以获得更好的剧本效果')
  }

  converting.value = true

  try {
    showProgress.value = true
    progressPercent.value = 10
    progressStatus.value = ''
    progressText.value = '正在分析小说结构和角色...'

    const script = await convertNovel(novelId.value, selectedIndices.value)

    progressPercent.value = 100
    progressStatus.value = 'success'
    progressText.value = '剧本生成完成！'

    setTimeout(() => {
      showProgress.value = false
      router.push(`/scripts/${script.id}`)
    }, 1000)

  } catch (err) {
    showProgress.value = false
    ElMessage.error('转换失败: ' + (err.message || '未知错误'))
  } finally {
    converting.value = false
  }
}

function goBack() {
  if (showProgress.value) return
  router.push('/novel/input')
}
</script>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background: #f8f9fa;
}

/* 导航栏 — 与首页一致 */
.nav-header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: 64px !important;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon { color: #409eff; }

.logo-text {
  font-size: 1.2em;
  font-weight: 600;
  color: #303133;
}

.nav-center {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-link {
  cursor: pointer;
  color: #606266;
  font-size: 0.95em;
  transition: color 0.2s;
}

.nav-link:hover { color: #409eff; }

.main {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 1.5em;
  color: #303133;
}

.novel-title {
  color: #999;
  font-size: 0.9em;
  margin-left: auto;
}

.back-btn {
  font-size: 14px;
  color: #606266;
}

.loading-wrapper {
  padding: 40px;
}

.summary-card {
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.summary-row {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
  color: #666;
  font-size: 0.95em;
}

.action-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-row .el-button:last-child {
  margin-left: auto;
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.chapter-card {
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.chapter-card.selected {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.chapter-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.chapter-checkbox {
  display: flex;
  align-items: flex-start;
  width: 100%;
}

.chapter-info {
  margin-left: 8px;
  flex: 1;
  min-width: 0;
}

.chapter-title {
  font-weight: 600;
  font-size: 1em;
  margin-bottom: 4px;
}

.chapter-preview {
  color: #999;
  font-size: 0.85em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.chapter-meta {
  color: #bbb;
  font-size: 0.8em;
}

.progress-content {
  text-align: center;
  padding: 30px;
}

.progress-text {
  margin-top: 20px;
  color: #666;
}

/* 页脚 */
.page-footer {
  background: #303133;
  color: #909399;
  text-align: center;
  padding: 24px 20px;
  font-size: 0.9em;
}

.page-footer p { margin: 0; }
</style>
