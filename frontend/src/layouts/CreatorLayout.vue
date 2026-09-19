<template>
  <div class="creator-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="side-menu">
        <router-link to="/creator" class="side-item" active-class="active">
          <el-icon :size="20"><Notebook /></el-icon>
          <span>创建知识库</span>
        </router-link>
        <div class="side-item disabled" title="协作功能规划中">
          <el-icon :size="20"><UserFilled /></el-icon>
          <span>管理协作者</span>
        </div>
        <router-link to="/creator/profile" class="side-item" active-class="active">
          <el-icon :size="20"><Postcard /></el-icon>
          <span>个人信息</span>
        </router-link>
      </div>
    </aside>

    <div class="main-area">
      <!-- 顶栏 -->
      <header class="topbar">
        <div class="brand">
          <span class="brand-name">好YOU</span>
          <span class="brand-badge">经验</span>
          <span class="brand-sub">创作者知识平台</span>
        </div>
        <div class="top-actions">
          <span v-if="userStore.token" class="hello">Hi，{{ userStore.nickname }}</span>
          <button v-else class="btn-ghost" @click="goReader">去登录</button>
          <button class="btn-ghost" @click="goReader">返回读者端</button>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Notebook, UserFilled, Postcard } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

function goReader() {
  router.push('/')
}
</script>

<style scoped>
.creator-layout {
  min-height: 100vh;
  display: flex;
}
.sidebar {
  width: 88px;
  background: linear-gradient(180deg, #00c75f, #009e4d);
  flex-shrink: 0;
}
.side-menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 24px 8px;
}
.side-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 4px;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.side-item:hover {
  background: rgba(255, 255, 255, 0.14);
}
.side-item.active {
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
  font-weight: 600;
}
.side-item.disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.main-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #f6f8f7;
}
.topbar {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 28px;
  box-shadow: 0 1px 0 rgba(31, 43, 37, 0.06);
}
.brand {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.brand-name {
  font-size: 22px;
  font-weight: 800;
  font-style: italic;
  color: var(--ink);
}
.brand-badge {
  background: var(--brand);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  font-style: italic;
  border-radius: 4px;
  padding: 1px 8px;
}
.brand-sub {
  font-size: 15px;
  color: var(--ink-2);
}
.top-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}
.hello {
  font-size: 13px;
  color: var(--ink-2);
}
.btn-ghost {
  height: 32px;
  padding: 0 16px;
  border-radius: 999px;
  border: 1px solid var(--brand);
  background: #fff;
  color: var(--brand);
  font-size: 13px;
}
.btn-ghost:hover {
  background: var(--brand);
  color: #fff;
}
.content {
  padding: 24px 28px;
  flex: 1;
}
</style>
