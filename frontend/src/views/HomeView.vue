<template>
  <el-container>
    <el-header class="header">
      <div class="header-content">
        <h1 class="logo">AI 小说转剧本工具</h1>
        <p class="subtitle">将你的小说一键转化为结构化剧本</p>
      </div>
    </el-header>

    <el-main class="main">
      <el-row :gutter="24" class="feature-cards">
        <el-col :span="8">
          <el-card shadow="hover" class="feature-card" @click="$router.push('/novel/input')">
            <el-icon class="card-icon" :size="48"><Upload /></el-icon>
            <h3>上传小说</h3>
            <p>粘贴或上传小说文本内容<br>支持3个章节以上</p>
            <el-button type="primary" round>开始上传</el-button>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card shadow="hover" class="feature-card" @click="$router.push('/scripts')">
            <el-icon class="card-icon" :size="48"><Document /></el-icon>
            <h3>AI 转换</h3>
            <p>AI 自动分析角色、场景和情节<br>生成结构化剧本 YAML</p>
            <el-button type="success" round>查看剧本</el-button>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card shadow="hover" class="feature-card">
            <el-icon class="card-icon" :size="48"><Download /></el-icon>
            <h3>导出 YAML</h3>
            <p>下载标准 YAML 格式剧本<br>可编辑、可进一步打磨</p>
            <el-button type="warning" round @click="$router.push('/scripts')">立即导出</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-main>

    <el-footer class="footer">
      <p>XEngineer 新工科计划 · 72小时极限开发 · 2026</p>
    </el-footer>
  </el-container>
</template>

<script setup>
import { Upload, Document, Download } from '@element-plus/icons-vue'
</script>

<style scoped>
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 20px;
  text-align: center;
  height: auto !important;
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
}

.logo {
  font-size: 2.5em;
  margin: 0 0 10px 0;
  font-weight: 700;
}

.subtitle {
  font-size: 1.1em;
  opacity: 0.9;
  margin: 0;
}

.main {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
  min-height: 400px;
  overflow-x: hidden;
}

.feature-cards {
  margin-top: 20px;
}

.feature-card {
  text-align: center;
  padding: 30px 20px;
  cursor: pointer;
  transition: transform 0.2s;
  height: 280px;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.card-icon {
  margin-bottom: 15px;
  color: #667eea;
}

.feature-card h3 {
  margin: 10px 0;
  font-size: 1.3em;
  color: #333;
}

.feature-card p {
  color: #666;
  margin-bottom: 20px;
  line-height: 1.6;
}

.footer {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 0.9em;
}
</style>
