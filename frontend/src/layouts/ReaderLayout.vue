<template>
  <div class="reader-layout">
    <!-- 顶部导航 -->
    <header class="navbar">
      <div class="container navbar-inner">
        <router-link to="/" class="logo-link">
          <img :src="logoImg" alt="好YOU经验" class="logo-img" />
        </router-link>

        <div class="search-bar">
          <input
            v-model="keyword"
            type="text"
            class="search-input"
            placeholder="请输入你想学习的内容"
            @keyup.enter="onSearch"
          />
          <button class="search-btn" aria-label="搜索" @click="onSearch">
            <el-icon :size="16" color="#fff"><Search /></el-icon>
          </button>
        </div>

        <nav class="nav-actions">
          <el-dropdown @command="onWorkbench">
            <button class="btn-login ghost">工作台 ▾</button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="/creator">创作者平台</el-dropdown-item>
                <el-dropdown-item command="/admin/audit-knowledge">审核工作台</el-dropdown-item>
                <el-dropdown-item command="/admin">管理后台</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <template v-if="userStore.token">
            <router-link to="/profile" class="avatar-link" title="个人中心">
              <el-icon :size="18" color="#fff"><User /></el-icon>
            </router-link>
            <span class="welcome">Hi，{{ userStore.nickname }}</span>
            <button class="btn-login" @click="onLogout">退出</button>
          </template>
          <button v-else class="btn-login" @click="authDialog?.open('login')">登录</button>
        </nav>
      </div>
    </header>

    <!-- 页面内容 -->
    <main class="page">
      <router-view />
    </main>

    <!-- 登录 / 注册弹窗 -->
    <AuthDialog ref="authDialog" />

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container footer-inner">
        <div class="footer-brand">
          <div class="footer-logo-chip">
            <img :src="logoImg" alt="好YOU经验" class="footer-logo" />
          </div>
          <p class="footer-desc">
            好YOU经验 —— 职场经验与专业知识分享平台，<br />
            让每一份职场经验都有回响。
          </p>
        </div>

        <div class="footer-col">
          <h4>参考课程</h4>
          <a href="#">热门课程</a>
          <a href="#">行业大类</a>
          <a href="#">系统学习</a>
        </div>

        <div class="footer-col">
          <h4>企业服务</h4>
          <a href="#">关于我们</a>
          <a href="#">联系我们</a>
          <a href="#">加入我们</a>
        </div>

        <div class="footer-col">
          <h4>联系方式</h4>
          <a href="#">400-888-6666</a>
          <a href="#">service@haoyou.com</a>
          <a href="#">周一至周日 9:00 - 18:00</a>
        </div>

        <div class="footer-col footer-qr">
          <div class="qr-item">
            <svg viewBox="0 0 64 64" class="qr-svg" aria-hidden="true">
              <rect width="64" height="64" rx="6" fill="#fff" />
              <g fill="#161d19">
                <rect x="8" y="8" width="16" height="16" rx="2" />
                <rect x="40" y="8" width="16" height="16" rx="2" />
                <rect x="8" y="40" width="16" height="16" rx="2" />
                <rect x="30" y="30" width="6" height="6" />
                <rect x="40" y="40" width="6" height="6" />
                <rect x="50" y="44" width="6" height="6" />
                <rect x="40" y="52" width="8" height="4" />
                <rect x="30" y="44" width="4" height="10" />
                <rect x="30" y="12" width="4" height="8" />
                <rect x="12" y="30" width="10" height="4" />
              </g>
            </svg>
            <span>微信公众号</span>
          </div>
          <div class="qr-item">
            <svg viewBox="0 0 64 64" class="qr-svg" aria-hidden="true">
              <rect width="64" height="64" rx="6" fill="#fff" />
              <g fill="#161d19">
                <rect x="8" y="8" width="16" height="16" rx="2" />
                <rect x="40" y="8" width="16" height="16" rx="2" />
                <rect x="8" y="40" width="16" height="16" rx="2" />
                <rect x="30" y="8" width="6" height="6" />
                <rect x="30" y="18" width="4" height="8" />
                <rect x="40" y="30" width="6" height="6" />
                <rect x="50" y="36" width="6" height="6" />
                <rect x="36" y="44" width="8" height="8" />
                <rect x="50" y="50" width="6" height="6" />
                <rect x="12" y="30" width="4" height="6" />
                <rect x="20" y="32" width="4" height="8" />
              </g>
            </svg>
            <span>下载APP</span>
          </div>
        </div>
      </div>

      <div class="footer-bottom">
        <p>好YOU经验创作者知识共享平台 · Copyright © 2026 haoyou.com All Rights Reserved</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Search } from '@element-plus/icons-vue'
import AuthDialog from '@/components/AuthDialog.vue'
import { useUserStore } from '@/store/user'
import logoImg from '@/assets/reader/logo.png'

const router = useRouter()
const userStore = useUserStore()
const authDialog = ref<InstanceType<typeof AuthDialog>>()

const keyword = ref('')

function onSearch() {
  if (!keyword.value.trim()) return
  router.push({ path: '/search', query: { keyword: keyword.value.trim() } })
}

function onLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
}

function onWorkbench(path: string) {
  router.push(path)
}
</script>

<style scoped>
.reader-layout {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}
.page {
  flex: 1;
}

/* ---------- 导航 ---------- */
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 1px 0 rgba(31, 43, 37, 0.06);
}
.navbar-inner {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 32px;
}
.logo-img {
  height: 40px;
  width: auto;
}
.search-bar {
  flex: 1;
  max-width: 560px;
  height: 42px;
  display: flex;
  align-items: center;
  background: #f2f5f3;
  border-radius: 999px;
  padding: 0 5px 0 20px;
}
.search-input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: var(--ink);
}
.search-input::placeholder {
  color: var(--ink-3);
}
.search-btn {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--brand);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}
.search-btn:hover {
  background: var(--brand-deep);
}
.nav-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 14px;
}
.avatar-link {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: var(--brand);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}
.avatar-link:hover {
  background: var(--brand-deep);
}
.btn-login {
  height: 38px;
  padding: 0 28px;
  border-radius: 999px;
  background: var(--brand);
  color: #fff;
  font-size: 14px;
  transition: background 0.2s;
}
.btn-login:hover {
  background: var(--brand-deep);
}
.btn-login.ghost {
  background: #fff;
  color: var(--brand);
  border: 1px solid var(--brand);
  padding: 0 18px;
}
.btn-login.ghost:hover {
  background: var(--brand);
  color: #fff;
}

/* ---------- 页脚 ---------- */
.footer {
  background: var(--footer-bg);
  color: rgba(255, 255, 255, 0.82);
  margin-top: 80px;
}
.footer-inner {
  display: flex;
  gap: 56px;
  padding: 56px 0 40px;
}
.footer-brand {
  flex: 1.2;
}
.footer-logo-chip {
  display: inline-block;
  background: #fff;
  border-radius: 8px;
  padding: 6px 10px;
}
.footer-logo {
  height: 28px;
  width: auto;
}
.footer-desc {
  margin: 18px 0 0;
  font-size: 13px;
  line-height: 1.9;
  color: rgba(255, 255, 255, 0.55);
}
.footer-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 13px;
}
.footer-col h4 {
  margin: 0 0 4px;
  font-size: 15px;
  color: #fff;
  font-weight: 600;
}
.footer-col a {
  color: rgba(255, 255, 255, 0.6);
  transition: color 0.2s;
}
.footer-col a:hover {
  color: var(--brand);
}
.footer-qr {
  flex-direction: row;
  gap: 20px;
  align-items: flex-start;
}
.qr-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}
.qr-svg {
  width: 84px;
  height: 84px;
  border-radius: 8px;
}
.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 18px 0;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}
.footer-bottom p {
  margin: 0;
}
</style>
