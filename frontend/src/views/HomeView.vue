<template>
  <div class="home-page">
    <!-- 导航栏 -->
    <el-header class="nav-header">
      <div class="nav-content">
        <div class="nav-left">
          <el-icon class="logo-icon" :size="32"><Reading /></el-icon>
          <span class="logo-text">AI 小说转剧本工具</span>
        </div>
        <div class="nav-center">
          <span class="nav-link active">首页</span>
          <span class="nav-link" @click="$router.push('/scripts')">我的剧本</span>
          <span class="nav-link">使用指南</span>
          <el-icon class="github-icon" :size="20"><Monitor /></el-icon>
        </div>
        <div class="nav-right">
          <el-button type="primary" round @click="$router.push('/novel/input')">
            <el-icon><Upload /></el-icon>
            上传小说
          </el-button>
        </div>
      </div>
    </el-header>

    <!-- 主内容 -->
    <div class="main-content">
    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-left">
          <h1 class="hero-title">
            AI 助力 · <span class="highlight">小说秒变剧本</span>
          </h1>
          <p class="hero-subtitle">
            上传小说，AI 自动分析角色、场景、情节，<br>
            生成结构化 YAML 剧本，支持在线编辑与导出
          </p>
          <div class="hero-buttons">
            <el-button type="primary" size="large" round @click="$router.push('/novel/input')">
              <el-icon><Upload /></el-icon>
              上传小说开始转换
            </el-button>
            <el-button size="large" round>
              <el-icon><VideoCamera /></el-icon>
              观看演示
            </el-button>
          </div>
        </div>
        <div class="hero-right">
          <div class="conversion-demo">
            <div class="novel-card">
              <div class="card-header novel">小说</div>
              <div class="card-content">
                <p>第一章 清晨的雾气还未散尽，林羽站在山顶，望着远处的城市。他不知道，命运的齿轮即将开始转动……</p>
              </div>
            </div>
            <div class="arrow-icon">
              <el-icon :size="40"><Right /></el-icon>
            </div>
            <div class="script-card">
              <div class="card-header script">剧本 (YAML)</div>
              <div class="card-content">
                <pre>script:
  metadata:
    title: 命运之轮
    source_novel: 命运之轮
    version: 1.0.0
  characters:
    - id: char_001
      name: 林羽
      gender: 男
      description: 主人公...</pre>
              </div>
            </div>
            <div class="code-badge">
              <el-icon :size="24"><Notebook /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="quick-actions">
      <div class="quick-grid">
        <div class="quick-card" @click="$router.push('/novel/input')">
          <el-icon class="quick-icon" :size="40"><Upload /></el-icon>
          <div class="quick-info">
            <h3>上传小说</h3>
            <p>支持 .txt 文件上传或手动粘贴，选择章节生成剧本</p>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="quick-card" @click="scrollToNovels">
          <el-icon class="quick-icon" :size="40"><Reading /></el-icon>
          <div class="quick-info">
            <h3>小说管理</h3>
            <p>查看已上传的小说，选择章节生成剧本</p>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="quick-card" @click="$router.push('/scripts')">
          <el-icon class="quick-icon" :size="40"><Folder /></el-icon>
          <div class="quick-info">
            <h3>剧本管理</h3>
            <p>查看、编辑、导出、删除已生成的剧本</p>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </section>

    <!-- 小说管理 -->
    <section class="novels-section" ref="novelsSectionRef">
      <div class="section-header-row">
        <h2 class="section-title">小说管理</h2>
        <el-button type="primary" @click="$router.push('/novel/input')">
          <el-icon><Upload /></el-icon> 上传新小说
        </el-button>
      </div>
      <el-card class="section-card" shadow="never">
        <el-table :data="novels" v-loading="loadingNovels" stripe style="width: 100%">
          <el-table-column prop="title" label="小说标题" min-width="160" />
          <el-table-column prop="author" label="作者" width="120" />
          <el-table-column prop="chapters" label="章节数" width="80" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 'converted' ? 'success' : 'info'" size="small">
                {{ row.status === 'converted' ? '已转换' : '待转换' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="上传时间" width="170">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="$router.push(`/novels/${row.id}/chapters`)">
                选择章节生成
              </el-button>
              <el-button type="danger" link size="small" @click="handleDeleteNovel(row.id, row.title)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loadingNovels && novels.length === 0" description="还没有上传小说">
          <el-button type="primary" @click="$router.push('/novel/input')">上传小说</el-button>
        </el-empty>
      </el-card>
    </section>

    <!-- 剧本管理 -->
    <section class="scripts-section">
      <div class="section-header-row">
        <h2 class="section-title">最近的剧本</h2>
        <el-link type="primary" @click="$router.push('/scripts')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-link>
      </div>
      <el-card class="section-card" shadow="never">
        <el-table :data="recentScripts" v-loading="loadingScripts" stripe style="width: 100%">
          <el-table-column prop="title" label="剧本标题" min-width="160" />
          <el-table-column prop="novelTitle" label="来源小说" min-width="140" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 'published' ? 'success' : 'info'" size="small">
                {{ row.status === 'published' ? '已发布' : '草稿' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="更新时间" width="170">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="$router.push(`/scripts/${row.id}`)">查看</el-button>
              <el-button link type="primary" size="small" @click="handleExport(row.id)">导出</el-button>
              <el-button link type="danger" size="small" @click="handleDeleteScript(row.id, row.title)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loadingScripts && recentScripts.length === 0" description="还没有剧本，快去上传小说吧！">
          <el-button type="primary" @click="$router.push('/novel/input')">上传小说</el-button>
        </el-empty>
      </el-card>
    </section>

    </div>

    <!-- 页脚 -->
    <footer class="page-footer">
      <p>© 2024 AI 小说转剧本工具 · 基于 Spring Boot & Vue 3 构建</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Reading, Upload, VideoCamera, Right, Notebook, 
  ArrowRight, Monitor 
} from '@element-plus/icons-vue'
import { listNovels, deleteNovel } from '../api/novel'
import { listScripts, deleteScript } from '../api/script'

const novelsSectionRef = ref(null)

const loadingNovels = ref(true)
const novels = ref([])

const loadingScripts = ref(true)
const recentScripts = ref([])

onMounted(async () => {
  await Promise.all([loadNovels(), loadScripts()])
})

async function loadNovels() {
  loadingNovels.value = true
  try {
    const data = await listNovels(1, 100)
    novels.value = data.records || []
  } catch (err) {
    // Silently fail
  } finally {
    loadingNovels.value = false
  }
}

async function loadScripts() {
  loadingScripts.value = true
  try {
    const data = await listScripts(1, 5)
    recentScripts.value = (data.records || []).slice(0, 5)
  } catch (err) {
    // Silently fail
  } finally {
    loadingScripts.value = false
  }
}

async function handleDeleteNovel(id, title) {
  try {
    await ElMessageBox.confirm(`确定要删除小说《${title}》吗？此操作不可恢复。`, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteNovel(id)
    ElMessage.success('删除成功')
    await loadNovels()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败: ' + (err.message || '未知错误'))
    }
  }
}

async function handleDeleteScript(id, title) {
  try {
    await ElMessageBox.confirm(`确定要删除剧本《${title}》吗？此操作不可恢复。`, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteScript(id)
    ElMessage.success('删除成功')
    await loadScripts()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败: ' + (err.message || '未知错误'))
    }
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.substring(0, 19).replace('T', ' ')
}

function handleExport(id) {
  window.open(`/api/scripts/${id}/export`, '_blank')
}

function scrollToNovels() {
  novelsSectionRef.value?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

/* 导航栏 */
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

.logo-icon {
  color: #409eff;
}

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

.nav-link:hover {
  color: #409eff;
}

.nav-link.active {
  color: #409eff;
  font-weight: 500;
  position: relative;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 2px;
  background: #409eff;
}

.github-icon {
  cursor: pointer;
  color: #606266;
}

.github-icon:hover {
  color: #409eff;
}

/* Hero 区域 */
.hero-section {
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding: 80px 20px;
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 60px;
}

.hero-left {
  flex: 1;
}

.hero-title {
  font-size: 3em;
  font-weight: 700;
  color: #303133;
  margin: 0 0 20px 0;
}

.hero-title .highlight {
  color: #409eff;
}

.hero-subtitle {
  font-size: 1.1em;
  color: #606266;
  line-height: 1.8;
  margin: 0 0 32px 0;
}

.hero-buttons {
  display: flex;
  gap: 16px;
}

.hero-right {
  flex: 1;
}

.conversion-demo {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
}

.novel-card, .script-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  width: 240px;
  overflow: hidden;
}

.card-header {
  padding: 12px 16px;
  color: white;
  font-weight: 600;
  font-size: 0.9em;
}

.card-header.novel {
  background: #67c23a;
}

.card-header.script {
  background: #409eff;
}

.card-content {
  padding: 16px;
  font-size: 0.85em;
  color: #606266;
}

.card-content pre {
  margin: 0;
  font-size: 0.8em;
  line-height: 1.6;
  white-space: pre-wrap;
}

.arrow-icon {
  color: #409eff;
}

.code-badge {
  position: absolute;
  bottom: -20px;
  right: 60px;
  background: #409eff;
  color: white;
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

/* 快捷入口 */
.quick-actions {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 0;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.quick-card {
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.25s;
}

.quick-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.12);
  transform: translateY(-2px);
}

.quick-icon {
  color: #409eff;
  flex-shrink: 0;
}

.quick-info {
  flex: 1;
  min-width: 0;
}

.quick-info h3 {
  margin: 0 0 4px 0;
  font-size: 1.05em;
  color: #303133;
}

.quick-info p {
  margin: 0;
  font-size: 0.85em;
  color: #909399;
}

.quick-arrow {
  color: #dcdfe6;
  flex-shrink: 0;
  transition: color 0.2s;
}

.quick-card:hover .quick-arrow {
  color: #409eff;
}

/* 小说管理 + 剧本管理 */
.novels-section,
.scripts-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 1.3em;
  color: #303133;
  margin: 0;
}

.section-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

/* 页脚 */
.page-footer {
  background: #303133;
  color: #909399;
  text-align: center;
  padding: 24px 20px;
  font-size: 0.9em;
}

.page-footer p {
  margin: 0;
}
</style>
