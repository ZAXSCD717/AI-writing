<template>
  <el-container>
    <el-header class="sub-header">
      <el-button text @click="$router.push('/')" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回首页
      </el-button>
      <h2>上传小说</h2>
    </el-header>

    <el-main class="main">
      <el-card class="upload-card">
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
              {{ submitting ? '上传中...' : '上传并转换' }}
            </el-button>
            <el-button @click="handleClear" size="large">清空</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- Conversion Progress Modal -->
      <el-dialog v-model="showProgress" title="AI 剧本生成中" :close-on-click-modal="false" width="400px">
        <div class="progress-content">
          <el-progress type="circle" :percentage="progressPercent" :status="progressStatus" />
          <p class="progress-text">{{ progressText }}</p>
        </div>
      </el-dialog>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { uploadNovel, convertNovel } from '../api/novel'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const showProgress = ref(false)
const progressPercent = ref(0)
const progressStatus = ref('')
const progressText = ref('')

const form = ref({
  title: '',
  author: '',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入小说标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入小说正文内容', trigger: 'blur' }]
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

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (chapterCount.value < 3) {
    ElMessage.warning('至少需要 3 个章节的内容才能生成剧本')
    return
  }

  submitting.value = true

  try {
    // Step 1: Upload novel
    const novel = await uploadNovel({
      title: form.value.title,
      author: form.value.author || '未知',
      content: form.value.content
    })

    ElMessage.success('小说上传成功，开始 AI 转换...')

    // Step 2: Show progress dialog
    showProgress.value = true
    progressPercent.value = 10
    progressText.value = '正在分析小说结构和角色...'

    // Step 3: Convert to script
    const script = await convertNovel(novel.id)

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
    submitting.value = false
  }
}

function handleClear() {
  form.value = { title: '', author: '', content: '' }
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
}

.back-btn {
  font-size: 14px;
}

.main {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.char-count {
  color: #999;
  font-size: 0.9em;
}

.progress-content {
  text-align: center;
  padding: 30px;
}

.progress-text {
  margin-top: 20px;
  color: #666;
}
</style>
