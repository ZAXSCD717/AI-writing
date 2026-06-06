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
        <el-button text @click="$router.push('/scripts')" class="back-btn">
          <el-icon><ArrowLeft /></el-icon> 返回列表
        </el-button>
        <h2>{{ script?.title || '剧本详情' }}</h2>
        <div class="header-actions">
          <el-button type="success" @click="handleExport" :disabled="!script">
            导出 YAML
          </el-button>
        </div>
      </div>

      <el-card v-loading="loading" class="detail-card" shadow="never">
        <template v-if="script">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="剧本标题">{{ script.title }}</el-descriptions-item>
            <el-descriptions-item label="原著小说">{{ script.novelTitle }}</el-descriptions-item>
            <el-descriptions-item label="版本号">{{ script.version }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="script.status === 'published' ? 'success' : 'info'">
                {{ script.status === 'published' ? '已发布' : '草稿' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(script.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDate(script.updatedAt) }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ script.description }}</el-descriptions-item>
          </el-descriptions>

          <div class="yaml-section">
            <div class="section-header">
              <h3>YAML 剧本内容</h3>
              <div class="section-actions">
                <el-button @click="toggleEdit" :type="isEditing ? 'default' : 'primary'" size="small">
                  {{ isEditing ? '取消编辑' : '编辑' }}
                </el-button>
                <el-button v-if="isEditing" type="success" size="small" @click="handleSave" :loading="saving">
                  保存
                </el-button>
              </div>
            </div>

            <template v-if="isEditing">
              <el-input
                v-model="editableYaml"
                type="textarea"
                :rows="20"
                class="yaml-editor"
              />
            </template>

            <template v-else>
              <pre class="yaml-display"><code>{{ script.yamlContent }}</code></pre>
            </template>
          </div>
        </template>
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
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading, Upload, ArrowLeft } from '@element-plus/icons-vue'
import { getScript, updateScript } from '../api/script'

const route = useRoute()
const loading = ref(true)
const saving = ref(false)
const script = ref(null)
const isEditing = ref(false)
const editableYaml = ref('')

onMounted(async () => {
  try {
    script.value = await getScript(route.params.id)
  } catch (err) {
    ElMessage.error('加载失败: ' + err.message)
  } finally {
    loading.value = false
  }
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.substring(0, 19).replace('T', ' ')
}

function toggleEdit() {
  if (!isEditing.value) {
    editableYaml.value = script.value?.yamlContent || ''
    isEditing.value = true
  } else {
    isEditing.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    const updated = await updateScript(route.params.id, {
      yamlContent: editableYaml.value
    })
    script.value = updated
    isEditing.value = false
    ElMessage.success('保存成功')
  } catch (err) {
    ElMessage.error('保存失败: ' + err.message)
  } finally {
    saving.value = false
  }
}

function handleExport() {
  const url = `/api/scripts/${route.params.id}/export`
  window.open(url, '_blank')
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
  max-width: 1000px;
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
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.back-btn {
  font-size: 14px;
  color: #606266;
}

.detail-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.yaml-section {
  margin-top: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-header h3 {
  margin: 0;
  color: #333;
}

.yaml-display {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 13px;
  line-height: 1.5;
  max-height: 600px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

.yaml-editor :deep(.el-textarea__inner) {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 13px;
  line-height: 1.5;
  min-height: 500px;
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
