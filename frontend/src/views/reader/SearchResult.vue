<template>
  <div class="container search-page">
    <h2 class="page-title">
      “<span class="kw">{{ keyword }}</span>” 的检索结果
      <span class="count">共 {{ total }} 条</span>
    </h2>

    <div v-if="results.length" class="result-list" v-loading="loading">
      <article v-for="r in results" :key="r.id" class="result-card" @click="goDetail(r.id)">
        <div class="result-body">
          <h3>{{ r.title }}</h3>
          <p class="summary">{{ r.summary || '点击查看详细内容' }}</p>
          <div class="meta">
            <span>{{ r.kbType || '未分类' }}</span>
            <span>作者：{{ r.authorName }}</span>
            <span>{{ r.subscribeCount || 0 }}人已订阅</span>
            <span>{{ fmtDate(r.publishedAt) }}</span>
          </div>
        </div>
      </article>
    </div>
    <el-empty v-else-if="!loading" description="没有找到相关内容，换个关键词试试" />
    <div v-else class="loading-box" v-loading="true"></div>

    <div class="pager" v-if="total > size">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        background
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

interface SearchRow {
  id: number
  title: string
  kbType?: string
  summary?: string
  publishedAt?: string
  subscribeCount?: number
  authorName: string
}

const route = useRoute()
const router = useRouter()

const keyword = computed(() => (route.query.keyword as string) || '')
const results = ref<SearchRow[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)

async function load() {
  if (!keyword.value.trim()) {
    results.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const res = await request.get<any, ApiResult<{ total: number; list: SearchRow[] }>>('/api/search', {
      params: { keyword: keyword.value.trim(), page: page.value, size }
    })
    results.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function onPage(p: number) {
  page.value = p
  load()
}

function goDetail(id: number) {
  // 检索结果为文章，进入阅读页
  router.push(`/read/${id}`)
}

function fmtDate(d?: string) {
  return d ? String(d).slice(0, 10) : '—'
}

watch(keyword, () => {
  page.value = 1
  load()
}, { immediate: true })
</script>

<style scoped>
.search-page {
  padding: 30px 0 40px;
  min-height: 320px;
}
.page-title {
  font-size: 20px;
  margin: 0 0 20px;
}
.kw {
  color: var(--brand);
}
.count {
  font-size: 13px;
  color: var(--ink-3);
  font-weight: 400;
  margin-left: 8px;
}
.result-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.result-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(31, 43, 37, 0.06);
  padding: 20px 24px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.result-card:hover {
  box-shadow: 0 6px 18px rgba(0, 199, 95, 0.14);
}
.result-card h3 {
  margin: 0 0 8px;
  font-size: 16px;
}
.summary {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--ink-2);
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.meta {
  display: flex;
  gap: 18px;
  font-size: 12px;
  color: var(--ink-3);
}
.meta span:first-child {
  background: rgba(0, 199, 95, 0.1);
  color: var(--brand);
  border-radius: 4px;
  padding: 1px 8px;
  font-weight: 600;
}
.loading-box {
  min-height: 160px;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
</style>
