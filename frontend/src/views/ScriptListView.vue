<template>
  <el-container>
    <el-header class="sub-header">
      <el-button text @click="$router.push('/')" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回首页
      </el-button>
      <h2>剧本列表</h2>
      <el-button type="primary" @click="$router.push('/novel/input')" class="new-btn">
        新建剧本
      </el-button>
    </el-header>

    <el-main class="main">
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
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { listScripts } from '../api/script'

const loading = ref(false)
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
  const url = `/api/scripts/${id}/export`
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
}

.main {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
