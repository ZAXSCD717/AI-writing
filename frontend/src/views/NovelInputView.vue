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
          <el-button type="primary" round disabled>
            <el-icon><Upload /></el-icon>
            上传小说
          </el-button>
        </div>
      </div>
    </el-header>

    <el-main class="main">
      <div class="page-header">
        <h2>上传小说</h2>
      </div>

      <el-card class="upload-card" shadow="never">
        <template #header>
          <span>小说信息</span>
        </template>

        <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
          <el-form-item label="小说标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入小说标题" />
          </el-form-item>

          <el-form-item label="原著作者" prop="author">
            <el-input v-model="form.author" placeholder="请输入原著作者（选填）" />
          </el-form-item>

          <el-form-item label="上传文件">
            <div class="upload-row">
              <el-upload
                ref="uploadRef"
                :auto-upload="false"
                :show-file-list="true"
                :limit="1"
                accept=".txt,.md"
                @change="handleFileSelect"
              >
                <el-button type="primary" plain>
                  <el-icon><Upload /></el-icon> 选择 .txt 文件
                </el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 .txt / .md 格式
                  </div>
                </template>
              </el-upload>
              <div class="encoding-group" v-if="fileSelected">
                <el-select v-model="selectedEncoding" class="encoding-select" size="default">
                  <el-option label="UTF-8" value="UTF-8" />
                  <el-option label="GBK" value="GBK" />
                  <el-option label="GB2312" value="GB2312" />
                  <el-option label="GB18030" value="GB18030" />
                  <el-option label="Big5 (繁体)" value="Big5" />
                  <el-option label="Shift-JIS (日文)" value="Shift-JIS" />
                  <el-option label="EUC-KR (韩文)" value="EUC-KR" />
                  <el-option label="Latin-1" value="ISO-8859-1" />
                </el-select>
                <el-button type="success" size="default" @click="readFile">
                  <el-icon><Download /></el-icon> 读取
                </el-button>
              </div>
            </div>
          </el-form-item>

          <el-divider>或手动粘贴</el-divider>

          <el-form-item label="小说内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="15"
              placeholder="请粘贴小说正文内容（至少3个章节）&#10;支持格式：第X章 / Chapter X 等"
            />
          </el-form-item>

          <el-form-item>
            <div class="char-count">
              已输入 {{ form.content.length }} 字符
              <span v-if="chapterCount > 0"> · 检测到约 {{ chapterCount }} 章</span>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
              {{ submitting ? '上传中...' : '上传并选择章节' }}
            </el-button>
            <el-button @click="handleClear" size="large">清空</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-main>

    <!-- 页脚 -->
    <footer class="page-footer">
      <p>© 2024 AI 小说转剧本工具 · 基于 Spring Boot & Vue 3 构建</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading, Upload, Download } from '@element-plus/icons-vue'
import { uploadNovel } from '../api/novel'

const router = useRouter()
const formRef = ref(null)
const uploadRef = ref(null)
const submitting = ref(false)
const selectedEncoding = ref('UTF-8')
const fileSelected = ref(false)
const pendingFile = ref(null)

const form = ref({
  title: '',
  author: '',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入小说标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入小说正文内容或上传文件', trigger: 'blur' }]
}

const chapterCount = computed(() => {
  const content = form.value.content
  if (!content) return 0
  const lines = content.split('\n')
  let count = 0
  for (const line of lines) {
    const trimmed = line.trim()
    if (/^第[一二三四五六七八九十百千零\d]+[章节部]/.test(trimmed) ||
        /^[Cc]hapter\s+\d+/.test(trimmed)) {
      count++
    }
  }
  return count || Math.ceil(content.length / 2000)
})

function handleFileSelect(file) {
  pendingFile.value = file
  fileSelected.value = true
  const name = file.name.replace(/\.[^.]+$/, '')
  if (!form.value.title) {
    form.value.title = name
  }
  ElMessage.info(`已选择文件：${file.name}，请选择编码后点击「读取」`)
}

function readFile() {
  if (!pendingFile.value) return
  const reader = new FileReader()
  const encoding = selectedEncoding.value || 'UTF-8'
  reader.onload = (e) => {
    const text = e.target.result
    form.value.content = text
    ElMessage.success(`已读取文件：${pendingFile.value.name}（${text.length} 字符，${encoding}）`)
  }
  reader.onerror = () => {
    ElMessage.error('文件读取失败，请检查编码设置')
  }
  reader.readAsText(pendingFile.value.raw, encoding)
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!form.value.content || form.value.content.trim().length === 0) {
    ElMessage.warning('请粘贴小说内容或上传 .txt 文件')
    return
  }

  if (chapterCount.value < 3) {
    ElMessage.warning('至少需要 3 个章节的内容才能生成剧本')
    return
  }

  submitting.value = true

  try {
    const novel = await uploadNovel({
      title: form.value.title,
      author: form.value.author || '未知',
      content: form.value.content
    })

    ElMessage.success('小说上传成功！请选择要转换的章节')
    router.push(`/novels/${novel.id}/chapters`)

  } catch (err) {
    ElMessage.error('上传失败: ' + (err.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

function handleClear() {
  form.value = { title: '', author: '', content: '' }
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

.page-header h2 {
  margin: 0 0 20px 0;
  font-size: 1.5em;
  color: #303133;
}

.upload-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.char-count {
  color: #999;
  font-size: 0.9em;
}

.upload-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.encoding-select {
  width: 150px;
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
