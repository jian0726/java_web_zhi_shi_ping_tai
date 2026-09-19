<template>
  <div class="read-page">
    <div class="container read-inner">
      <!-- 左：目录 -->
      <aside class="toc-card" v-loading="loading">
        <template v-if="article">
          <div class="toc-head">
            <h2 class="toc-title">{{ baseTitle }}</h2>
            <p class="toc-meta">作者：{{ article.authorName }}</p>
            <p class="toc-count">共 {{ modules.length }} 章 / {{ flatArticles.length }} 篇</p>
          </div>
          <div class="toc-list">
            <template v-for="mod in modules" :key="mod.moduleId">
              <p class="toc-mod-name">{{ mod.moduleName }}</p>
              <div
                v-for="a in mod.articles"
                :key="a.id"
                class="toc-item"
                :class="{ current: a.id === currentId }"
                @click="goChapter(a.id)"
              >
                <span class="toc-item-title">{{ a.title }}</span>
                <span v-if="a.id === currentId" class="toc-badge">当前</span>
              </div>
            </template>
          </div>
        </template>
      </aside>

      <!-- 右：正文 -->
      <main class="content-col">
        <div class="chapter-banner" v-if="article">
          <div class="banner-fallback"><span>{{ baseTitle }}</span></div>
        </div>

        <article class="chapter-card" v-loading="loading">
          <template v-if="article">
            <h1 class="chapter-title">{{ article.title }}</h1>
            <p class="chapter-meta">
              <span>作者：{{ article.authorName }}</span>
              <span>{{ formatDate(article.publishedAt) }}</span>
            </p>
            <div class="rich" v-html="article.content || '<p>暂无正文</p>'"></div>

            <div class="chapter-nav">
              <button class="nav-btn" :disabled="!prevId" @click="goChapter(prevId!)">← 上一篇</button>
              <button class="nav-btn primary" :disabled="!nextId" @click="goChapter(nextId!)">下一篇 →</button>
            </div>
          </template>
          <el-empty v-else-if="!loading" description="内容不存在或未发布" />
        </article>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticleApi, getKbChaptersApi, getKbDetailApi, type ArticleDetail, type ChapterNode } from '@/api/kb'

const route = useRoute()
const router = useRouter()

const currentId = computed(() => Number(route.params.id))
const loading = ref(false)
const article = ref<ArticleDetail | null>(null)
const modules = ref<ChapterNode[]>([])
const baseTitle = ref('')

/** 目录树展平，用于上一篇/下一篇 */
const flatArticles = computed(() => modules.value.flatMap((m) => m.articles))
const currentIndex = computed(() => flatArticles.value.findIndex((a) => a.id === currentId.value))
const prevId = computed(() => (currentIndex.value > 0 ? flatArticles.value[currentIndex.value - 1].id : null))
const nextId = computed(() =>
  currentIndex.value >= 0 && currentIndex.value < flatArticles.value.length - 1
    ? flatArticles.value[currentIndex.value + 1].id
    : null
)

async function load() {
  loading.value = true
  try {
    const res = await getArticleApi(currentId.value)
    article.value = res.data
    if (res.data) {
      const [chapRes, baseRes] = await Promise.all([
        getKbChaptersApi(res.data.baseId),
        getKbDetailApi(res.data.baseId)
      ])
      modules.value = chapRes.data || []
      baseTitle.value = baseRes.data?.title || ''
    }
    window.scrollTo({ top: 0 })
  } finally {
    loading.value = false
  }
}

function goChapter(id: number) {
  if (id && id !== currentId.value) {
    router.push(`/read/${id}`)
  }
}

function formatDate(d?: string) {
  if (!d) return '—'
  return String(d).slice(0, 10)
}

watch(currentId, load, { immediate: true })
</script>

<style scoped>
.read-page {
  padding: 30px 0 50px;
}
.read-inner {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 24px;
  align-items: start;
}

/* ---------- 左目录 ---------- */
.toc-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(31, 43, 37, 0.07);
  padding: 22px 18px;
  position: sticky;
  top: 92px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}
.toc-head {
  text-align: left;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(31, 43, 37, 0.08);
}
.toc-cover {
  width: 96px;
  height: 70px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
}
.toc-cover img {
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
  font-size: 20px;
  font-weight: 700;
}
.toc-title {
  margin: 0 0 8px;
  font-size: 16px;
  line-height: 1.5;
}
.toc-meta {
  margin: 3px 0;
  font-size: 12px;
  color: var(--ink-3);
}
.toc-count {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--brand);
  font-weight: 600;
}
.toc-list {
  padding-top: 8px;
}
.toc-mod-name {
  margin: 12px 0 4px;
  font-size: 12px;
  font-weight: 700;
  color: var(--brand-dark);
  padding-left: 4px;
}
.toc-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}
.toc-item:hover {
  background: var(--bg-soft);
}
.toc-item.current {
  background: rgba(0, 199, 95, 0.1);
}
.toc-no {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 700;
  color: var(--ink-3);
}
.toc-item.current .toc-no {
  color: var(--brand);
}
.toc-item-title {
  flex: 1;
  font-size: 13px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.toc-badge {
  flex-shrink: 0;
  font-size: 11px;
  color: var(--brand);
  border: 1px solid var(--brand);
  border-radius: 999px;
  padding: 1px 8px;
}

/* ---------- 右正文 ---------- */
.chapter-banner {
  border-radius: 14px;
  overflow: hidden;
  margin-bottom: 22px;
}
.banner-fallback {
  height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.16) 0, transparent 40%),
    radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.12) 0, transparent 40%),
    linear-gradient(135deg, #00b159 0%, #009e4d 100%);
}
.banner-fallback span {
  color: #fff;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 6px;
  opacity: 0.95;
}
.chapter-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(31, 43, 37, 0.07);
  padding: 34px 40px;
  min-height: 320px;
}
.chapter-title {
  margin: 0 0 10px;
  font-size: 24px;
  line-height: 1.5;
}
.chapter-meta {
  margin: 0 0 24px;
  display: flex;
  gap: 28px;
  font-size: 13px;
  color: var(--ink-3);
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(31, 43, 37, 0.08);
}
.rich {
  font-size: 15px;
  line-height: 2;
  color: var(--ink-2);
}
.rich :deep(h1),
.rich :deep(h2),
.rich :deep(h3) {
  color: var(--ink);
  margin: 24px 0 12px;
}
.rich :deep(p) {
  margin: 10px 0;
}
.chapter-nav {
  display: flex;
  justify-content: space-between;
  margin-top: 36px;
  padding-top: 22px;
  border-top: 1px solid rgba(31, 43, 37, 0.08);
}
.nav-btn {
  height: 40px;
  padding: 0 28px;
  border-radius: 999px;
  background: var(--bg-soft);
  color: var(--ink-2);
  font-size: 14px;
  transition: all 0.2s;
}
.nav-btn:hover:not(:disabled) {
  color: var(--brand);
}
.nav-btn.primary {
  background: var(--brand);
  color: #fff;
}
.nav-btn.primary:hover:not(:disabled) {
  background: var(--brand-deep);
  color: #fff;
}
.nav-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

@media (max-width: 992px) {
  .read-inner {
    grid-template-columns: 1fr;
  }
  .toc-card {
    position: static;
    max-height: none;
  }
}
</style>
