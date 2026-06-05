<template>
  <el-container>
    <el-header class="sub-header">
      <el-button text @click="$router.push('/scripts')" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
      <h2>{{ script?.title || '剧本详情' }}</h2>
      <div class="header-actions">
        <el-button type="success" @click="handleExport" :disabled="!script">
          导出 YAML
        </el-button>
      </div>
    </el-header>

    <el-main class="main">
      <el-card v-loading="loading" class="detail-card">
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
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
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
.sub-header {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-bottom: 1px solid #eee;
  padding: 0 20px;
  height: 60px;
}

.sub-header h2 {
  margin: 0;
  font-size: 1.2em;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 20px;
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
</style>
