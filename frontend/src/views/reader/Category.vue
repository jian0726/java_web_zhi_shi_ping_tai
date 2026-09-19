<template>
  <div class="category-page">
    <!-- 全部分类筛选 -->
    <section class="filter-bar">
      <div class="container">
        <div class="filter-row">
          <span class="filter-label">所有行业分类：</span>
          <div class="chips">
            <button
              class="chip"
              :class="{ active: !currentType }"
              @click="selectType('')"
            >全部</button>
            <button
              v-for="c in categories"
              :key="c.type"
              class="chip"
              :class="{ active: currentType === c.type }"
              @click="selectType(c.type)"
            >{{ c.type }}</button>
          </div>
        </div>
      </div>
    </section>

    <!-- 列表头 -->
    <section class="list-head">
      <div class="container list-head-inner">
        <div class="head-left">
          <h1 class="head-title">{{ currentType || '全部分类' }}</h1>
          <div class="sort-tabs">
            <button class="sort-tab" :class="{ active: sort === 'new' }" @click="switchSort('new')">最新</button>
            <button class="sort-tab" :class="{ active: sort === 'hot' }" @click="switchSort('hot')">最热</button>
          </div>
        </div>
        <p class="head-count">为你找到 <b>{{ total }}</b> 个内容</p>
      </div>
    </section>

    <!-- 卡片网格 -->
    <section class="list-body">
      <div class="container">
        <el-empty v-if="!loading && cards.length === 0" description="暂无内容" />
        <div v-else class="card-grid" v-loading="loading">
          <article
            v-for="card in cards"
            :key="card.id"
            class="kb-card"
            @click="goDetail(card.id)"
          >
            <div class="kb-cover">
              <img v-if="card.coverUrl" :src="card.coverUrl" :alt="card.title" />
              <div v-else class="cover-fallback">
                <span>{{ card.title.slice(0, 2) }}</span>
              </div>
            </div>
            <div class="kb-info">
              <h3 class="kb-title">{{ card.title }}</h3>
              <p class="kb-summary">{{ card.summary || '点击查看详细内容' }}</p>
              <p class="kb-meta">
                <span>作者：{{ card.authorName }}</span>
                <span>{{ formatCount(card.subscribeCount) }}人已订阅</span>
              </p>
            </div>
          </article>
        </div>

        <div class="pager">
          <el-pagination
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="page"
            background
            @current-change="onPageChange"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategoriesApi, getKbPageApi, type KnowledgeCard } from '@/api/kb'

const route = useRoute()
const router = useRouter()

const categories = ref<{ type: string; count: number }[]>([])
const currentType = ref((route.query.type as string) || '')
const sort = ref<'new' | 'hot'>('new')
const cards = ref<KnowledgeCard[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 16
const loading = ref(false)

async function loadCategories() {
  const res = await getCategoriesApi()
  categories.value = res.data || []
}

async function loadList() {
  loading.value = true
  try {
    const res = await getKbPageApi({
      type: currentType.value || undefined,
      sort: sort.value,
      page: page.value,
      size: pageSize
    })
    cards.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function selectType(type: string) {
  currentType.value = type
  page.value = 1
  router.replace({ query: type ? { type } : {} })
}

function switchSort(s: 'new' | 'hot') {
  sort.value = s
  page.value = 1
  loadList()
}

function onPageChange(p: number) {
  page.value = p
  loadList()
}

function goDetail(id: number) {
  router.push(`/article/${id}`)
}

function formatCount(n?: number) {
  return n ?? 0
}

watch(currentType, loadList)

loadCategories()
loadList()
</script>

<style scoped>
/* ---------- 筛选条 ---------- */
.filter-bar {
  border-bottom: 1px solid rgba(31, 43, 37, 0.06);
  padding: 22px 0;
}
.filter-row {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}
.filter-label {
  font-size: 14px;
  color: var(--ink-2);
  line-height: 30px;
  flex-shrink: 0;
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 12px;
}
.chip {
  height: 30px;
  padding: 0 16px;
  border-radius: 999px;
  background: var(--bg-soft);
  color: var(--ink-2);
  font-size: 13px;
  border: 1px solid transparent;
  transition: all 0.2s;
}
.chip:hover {
  color: var(--brand);
}
.chip.active {
  background: rgba(0, 199, 95, 0.1);
  border-color: var(--brand);
  color: var(--brand);
  font-weight: 600;
}

/* ---------- 列表头 ---------- */
.list-head {
  padding: 28px 0 6px;
}
.list-head-inner {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}
.head-left {
  display: flex;
  align-items: flex-end;
  gap: 24px;
}
.head-title {
  margin: 0;
  font-size: 24px;
}
.sort-tabs {
  display: flex;
  gap: 4px;
}
.sort-tab {
  height: 28px;
  padding: 0 14px;
  background: transparent;
  color: var(--ink-2);
  font-size: 14px;
  border-radius: 999px;
}
.sort-tab.active {
  background: var(--brand);
  color: #fff;
}
.head-count {
  margin: 0;
  font-size: 13px;
  color: var(--ink-3);
}
.head-count b {
  color: var(--brand);
}

/* ---------- 卡片网格 ---------- */
.list-body {
  padding: 20px 0 40px;
}
.card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 22px;
  min-height: 200px;
}
.kb-card {
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(31, 43, 37, 0.07);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.kb-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 199, 95, 0.16);
}
.kb-cover {
  aspect-ratio: 16 / 9;
  overflow: hidden;
}
.kb-cover img {
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
  font-size: 34px;
  font-weight: 700;
  letter-spacing: 4px;
  opacity: 0.92;
}
.kb-info {
  padding: 14px 16px 16px;
}
.kb-title {
  margin: 0;
  font-size: 15px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.kb-summary {
  margin: 8px 0 10px;
  font-size: 12px;
  color: var(--ink-3);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.kb-meta {
  margin: 0;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--ink-3);
}
.kb-meta span:last-child {
  color: var(--brand);
}

/* ---------- 分页 ---------- */
.pager {
  display: flex;
  justify-content: center;
  margin-top: 36px;
}

@media (max-width: 992px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
