<template>
  <div class="detail-page">
    <!-- 绿色渐变 Hero 卡片 -->
    <section class="hero-wrap">
      <div class="container">
        <div class="crumb">
          <router-link to="/">首页</router-link>
          <span class="crumb-sep">&gt;</span>
          <router-link :to="currentType ? { path: '/category', query: { type: currentType } } : '/category'">
            {{ detail?.kbType || '知识分类' }}
          </router-link>
          <span class="crumb-sep">&gt;</span>
          <span class="crumb-cur">知识详情库</span>
        </div>

        <div class="hero-card" v-loading="loading">
          <template v-if="detail">
            <div class="hero-cover">
              <img v-if="detail.coverUrl" :src="detail.coverUrl" :alt="detail.title" />
              <div v-else class="cover-fallback"><span>{{ detail.title.slice(0, 2) }}</span></div>
            </div>
            <div class="hero-info">
              <h1 class="hero-title">{{ detail.title }}</h1>
              <p class="hero-summary">{{ detail.summary || '暂无简介' }}</p>
              <p class="hero-meta">
                <span>作者：{{ detail.authorName }}</span>
                <span>发布时间：{{ formatDate(detail.publishedAt) }}</span>
                <span>{{ detail.subscribeCount || 0 }}人已订阅</span>
              </p>
              <button
                class="subscribe-btn"
                :class="{ done: detail.subscribed }"
                @click="onSubscribe"
              >
                {{ detail.subscribed ? '已订阅' : '我要订阅' }}
              </button>
            </div>
          </template>
        </div>
      </div>
    </section>

    <!-- 正文两栏 -->
    <section class="body-wrap">
      <div class="container body-inner">
        <!-- 左：介绍 / 大纲 -->
        <div class="main-card" v-loading="loading">
          <el-tabs v-model="activeTab" class="detail-tabs">
            <el-tab-pane label="课程介绍" name="intro">
              <div class="rich" v-html="detail?.intro || '<p>暂无介绍</p>'"></div>
            </el-tab-pane>
            <el-tab-pane :label="'课程大纲'" name="outline">
              <div class="outline">
                <p class="outline-tip">
                  本知识库共 {{ detail?.chapters?.length || 0 }} 个模块、{{ totalArticles }} 篇内容
                </p>
                <div v-for="mod in detail?.chapters || []" :key="mod.moduleId" class="outline-module">
                  <p class="outline-mod-name">{{ mod.moduleName }}</p>
                  <div
                    v-for="a in mod.articles"
                    :key="a.id"
                    class="outline-item"
                    :class="{ current: false }"
                    @click="goRead(a.id)"
                  >
                    <span class="outline-no">文</span>
                    <span class="outline-title">{{ a.title }}</span>
                  </div>
                  <p v-if="mod.articles.length === 0" class="outline-empty">该模块暂无文章</p>
                </div>
                <el-empty v-if="!detail?.chapters?.length" description="暂无章节" :image-size="60" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 右：知识库推荐 -->
        <aside class="side-card">
          <h3 class="side-title">知识库</h3>
          <div
            v-for="rec in recommends"
            :key="rec.id"
            class="rec-item"
            @click="rec.id === currentId ? null : goDetail(rec.id)"
          >
            <div class="rec-cover">
              <img v-if="rec.coverUrl" :src="rec.coverUrl" :alt="rec.title" />
              <div v-else class="rec-fallback"><span>{{ rec.title.slice(0, 2) }}</span></div>
            </div>
            <div class="rec-info">
              <p class="rec-title">{{ rec.title }}</p>
              <p class="rec-meta">作者：{{ rec.authorName }}</p>
              <p class="rec-meta">发布时间：{{ formatDate(rec.publishedAt) }}</p>
              <button class="rec-btn">详情</button>
            </div>
          </div>
          <el-empty v-if="recommends.length === 0" description="暂无推荐" :image-size="60" />
        </aside>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getKbDetailApi,
  getKbRecommendApi,
  subscribeApi,
  unsubscribeApi,
  type KnowledgeDetail,
  type KnowledgeCard
} from '@/api/kb'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentId = computed(() => Number(route.params.id))
const loading = ref(false)
const detail = ref<KnowledgeDetail | null>(null)
const recommends = ref<KnowledgeCard[]>([])
const activeTab = ref('intro')

const currentType = computed(() => detail.value?.kbType || '')
const totalArticles = computed(() =>
  (detail.value?.chapters || []).reduce((sum, m) => sum + m.articles.length, 0)
)

async function load() {
  loading.value = true
  try {
    const res = await getKbDetailApi(currentId.value)
    detail.value = res.data
    if (res.data) {
      const recRes = await getKbRecommendApi({
        excludeId: currentId.value,
        kbType: res.data.kbType || undefined,
        limit: 6
      })
      recommends.value = recRes.data || []
    }
  } finally {
    loading.value = false
  }
}

async function onSubscribe() {
  if (!userStore.token) {
    ElMessage.warning('请先登录后再订阅')
    return
  }
  if (!detail.value) return
  try {
    if (detail.value.subscribed) {
      await unsubscribeApi(detail.value.id)
      detail.value.subscribed = false
      detail.value.subscribeCount = Math.max((detail.value.subscribeCount || 1) - 1, 0)
      ElMessage.success('已取消订阅')
    } else {
      await subscribeApi(detail.value.id)
      detail.value.subscribed = true
      detail.value.subscribeCount = (detail.value.subscribeCount || 0) + 1
      ElMessage.success('订阅成功')
    }
  } catch {
    /* 错误提示由拦截器统一处理 */
  }
}

function goRead(id: number) {
  router.push(`/read/${id}`)
}

function goDetail(id: number) {
  router.push(`/article/${id}`)
}

function formatDate(d?: string) {
  if (!d) return '—'
  return String(d).slice(0, 10)
}

watch(currentId, load, { immediate: true })
</script>

<style scoped>
/* ---------- Hero ---------- */
.hero-wrap {
  background: linear-gradient(180deg, #7fe0a8 0%, #d9f5e5 100%);
  padding: 18px 0 34px;
}
.crumb {
  font-size: 13px;
  color: rgba(31, 43, 37, 0.65);
  margin-bottom: 16px;
}
.crumb a:hover {
  color: var(--brand-dark);
}
.crumb-sep {
  margin: 0 8px;
  color: rgba(31, 43, 37, 0.4);
}
.crumb-cur {
  color: var(--ink-2);
}
.hero-card {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(0, 158, 77, 0.12);
  padding: 32px;
  display: flex;
  gap: 36px;
  min-height: 200px;
}
.hero-cover {
  width: 200px;
  height: 150px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
}
.hero-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(0, 199, 95, 0.9), rgba(0, 158, 77, 0.85));
}
.cover-fallback span {
  color: #fff;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 4px;
}
.hero-info {
  flex: 1;
  min-width: 0;
}
.hero-title {
  margin: 4px 0 12px;
  font-size: 26px;
}
.hero-summary {
  margin: 0 0 14px;
  font-size: 14px;
  color: var(--ink-2);
  line-height: 1.8;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.hero-meta {
  margin: 0 0 20px;
  display: flex;
  gap: 28px;
  font-size: 13px;
  color: var(--ink-3);
}
.subscribe-btn {
  height: 40px;
  padding: 0 32px;
  border-radius: 999px;
  background: var(--brand);
  color: #fff;
  font-size: 14px;
  transition: background 0.2s;
}
.subscribe-btn:hover {
  background: var(--brand-deep);
}
.subscribe-btn.done {
  background: #eef4f0;
  color: var(--ink-3);
  cursor: default;
}

/* ---------- 正文两栏 ---------- */
.body-wrap {
  padding: 30px 0 50px;
}
.body-inner {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
  align-items: start;
}
.main-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(31, 43, 37, 0.07);
  padding: 8px 32px 32px;
  min-height: 320px;
}
.detail-tabs :deep(.el-tabs__item.is-active) {
  color: var(--brand);
}
.detail-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--brand);
}
.rich {
  font-size: 14px;
  line-height: 1.9;
  color: var(--ink-2);
}
.rich :deep(h3) {
  color: var(--ink);
  font-size: 16px;
  margin: 22px 0 10px;
}
.rich :deep(p) {
  margin: 8px 0;
}

/* 大纲 */
.outline-tip {
  font-size: 13px;
  color: var(--ink-3);
  margin: 6px 0 14px;
}
.outline-mod-name {
  margin: 14px 0 6px;
  font-size: 14px;
  font-weight: 700;
  color: var(--ink);
}
.outline-empty {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--ink-3);
}
.outline-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 11px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;
}
.outline-item:hover {
  background: var(--bg-soft);
}
.outline-item.current {
  background: rgba(0, 199, 95, 0.08);
  cursor: default;
}
.outline-no {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border-radius: 8px;
  background: rgba(0, 199, 95, 0.12);
  color: var(--brand);
  font-weight: 700;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.outline-item.current .outline-no {
  background: var(--brand);
  color: #fff;
}
.outline-title {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.outline-meta {
  font-size: 12px;
  color: var(--ink-3);
  flex-shrink: 0;
}

/* ---------- 右栏推荐 ---------- */
.side-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(31, 43, 37, 0.07);
  padding: 20px 18px;
}
.side-title {
  margin: 0 0 14px;
  font-size: 17px;
}
.rec-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-top: 1px solid rgba(31, 43, 37, 0.06);
  cursor: pointer;
}
.rec-item:first-of-type {
  border-top: none;
}
.rec-cover {
  width: 84px;
  height: 62px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
}
.rec-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.rec-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(0, 199, 95, 0.85), rgba(0, 158, 77, 0.8));
}
.rec-fallback span {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
}
.rec-info {
  flex: 1;
  min-width: 0;
}
.rec-title {
  margin: 0 0 4px;
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rec-meta {
  margin: 2px 0;
  font-size: 11px;
  color: var(--ink-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rec-btn {
  margin-top: 6px;
  height: 22px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid var(--brand);
  background: #fff;
  color: var(--brand);
  font-size: 11px;
}
.rec-btn:hover {
  background: var(--brand);
  color: #fff;
}

@media (max-width: 992px) {
  .body-inner {
    grid-template-columns: 1fr;
  }
  .hero-card {
    flex-direction: column;
  }
  .hero-cover {
    width: 100%;
    height: 180px;
  }
}
</style>
