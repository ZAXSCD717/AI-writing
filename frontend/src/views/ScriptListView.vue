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
          <span class="nav-link active">我的剧本</span>
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
        <h2>我的剧本</h2>
      </div>

      <el-card class="list-card" shadow="never">
        <el-table :data="scripts" v-loading="loading" stripe style="width: 100%">
          <el-table-column prop="title" label="剧本标题" min-width="200" />
          <el-table-column prop="novelTitle" label="原著小说" min-width="150" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'published' ? 'success' : 'info'">
                {{ row.status === 'published' ? '已发布' : '草稿' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="version" label="版本" width="80" />
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="$router.push(`/scripts/${row.id}`)">
                查看
              </el-button>
              <el-button type="success" link @click="handleExport(row.id)">
                导出
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next, total"
            @current-change="loadScripts"
          />
        </div>

        <el-empty v-if="!loading && total === 0" description="还没有剧本，快去上传小说吧！">
          <el-button type="primary" @click="$router.push('/novel/input')">上传小说</el-button>
        </el-empty>
      </el-card>
    </el-main>

    <!-- 页脚 -->
    <footer class="page-footer">
      <p>© 2024 AI 小说转剧本工具 · 基于 Spring Boot & Vue 3 构建</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Reading, Upload } from '@element-plus/icons-vue'
import { listScripts } from '../api/script'

const loading = ref(true)
const scripts = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

onMounted(() => {
  loadScripts()
})

async function loadScripts() {
  loading.value = true
  try {
    const data = await listScripts(currentPage.value, pageSize.value)
    scripts.value = data.records || []
    total.value = data.total || 0
  } catch (err) {
    ElMessage.error('加载失败: ' + err.message)
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.substring(0, 19).replace('T', ' ')
}

function handleExport(id) {
  window.open(`/api/scripts/${id}/export`, '_blank')
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

/* 主内容 */
.main {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 20px;
}

.page-header h2 {
  margin: 0 0 20px 0;
  font-size: 1.5em;
  color: #303133;
}

.list-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
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
