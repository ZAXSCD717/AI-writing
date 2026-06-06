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

    <!-- 功能特点 -->
    <section class="features-section">
      <div class="features-grid">
        <div class="feature-card">
          <el-icon class="feature-icon" :size="36"><Reading /></el-icon>
          <h3>小说上传</h3>
          <p>支持长篇小说粘贴，自动检测章节数量，至少 3 章即可转换</p>
        </div>
        <div class="feature-card">
          <el-icon class="feature-icon" :size="36"><MagicStick /></el-icon>
          <h3>AI 自动转换</h3>
          <p>DeepSeek AI 分析小说内容，识别角色、场景、情节，生成结构化 YAML 剧本</p>
        </div>
        <div class="feature-card">
          <el-icon class="feature-icon" :size="36"><Edit /></el-icon>
          <h3>在线编辑</h3>
          <p>在线预览和编辑 YAML 剧本，支持语法高亮、自动补全，编辑更高效</p>
        </div>
        <div class="feature-card">
          <el-icon class="feature-icon" :size="36"><Download /></el-icon>
          <h3>YAML 导出</h3>
          <p>一键导出标准 YAML 格式，兼容各类剧本工具，便于分享与使用</p>
        </div>
        <div class="feature-card">
          <el-icon class="feature-icon" :size="36"><Folder /></el-icon>
          <h3>剧本管理</h3>
          <p>多剧本列表管理，支持查看、编辑、导出、删除等操作</p>
        </div>
      </div>
    </section>

    <!-- 最近的剧本 -->
    <section class="recent-section">
      <div class="recent-header">
        <h2 class="recent-title">最近的剧本</h2>
        <el-link type="primary" @click="$router.push('/scripts')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-link>
      </div>
      <el-table :data="recentScripts" style="width: 100%" class="recent-table">
        <el-table-column prop="title" label="剧本标题" />
        <el-table-column prop="sourceNovel" label="来源小说" />
        <el-table-column prop="charCount" label="角色数量" width="120" />
        <el-table-column prop="sceneCount" label="场景数量" width="120" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" size="small">查看</el-button>
            <el-button link type="primary" size="small">编辑</el-button>
            <el-button link type="primary" size="small">导出</el-button>
            <el-button link type="danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <!-- 页脚 -->
    <footer class="page-footer">
      <p>© 2024 AI 小说转剧本工具 · 基于 Spring Boot & Vue 3 构建</p>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { 
  Reading, Upload, VideoCamera, Right, Notebook, 
  MagicStick, Edit, Download, Folder, ArrowRight, Monitor 
} from '@element-plus/icons-vue'

const recentScripts = ref([
  {
    title: '命运之轮',
    sourceNovel: '命运之轮.txt',
    charCount: 8,
    sceneCount: 12,
    updateTime: '2024-06-01 14:30'
  },
  {
    title: '逆天成神',
    sourceNovel: '逆天成神.txt',
    charCount: 12,
    sceneCount: 20,
    updateTime: '2024-05-31 18:45'
  },
  {
    title: '都市之最强高手',
    sourceNovel: '都市之最强高手.txt',
    charCount: 6,
    sceneCount: 15,
    updateTime: '2024-05-30 09:20'
  }
])
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f8f9fa;
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

/* 功能特点 */
.features-section {
  padding: 60px 20px;
  background: white;
}

.features-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 24px;
}

.feature-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.feature-icon {
  color: #409eff;
  margin-bottom: 16px;
}

.feature-card h3 {
  font-size: 1.1em;
  color: #303133;
  margin: 0 0 12px 0;
}

.feature-card p {
  font-size: 0.9em;
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

/* 最近的剧本 */
.recent-section {
  padding: 60px 20px;
  background: white;
  margin-top: 20px;
}

.recent-section > div {
  max-width: 1200px;
  margin: 0 auto;
}

.recent-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.recent-title {
  font-size: 1.5em;
  color: #303133;
  margin: 0;
}

.recent-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
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
