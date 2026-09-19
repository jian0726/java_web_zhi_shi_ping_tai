<template>
  <div class="job-page">
    <!-- 顶部横幅 -->
    <section class="hero-wrap">
      <div class="container">
        <div class="crumb">
          <router-link to="/">首页</router-link>
          <span class="crumb-sep">&gt;</span>
          <span class="crumb-cur">岗位认知详情</span>
        </div>

        <div class="banner" v-loading="loading">
          <template v-if="job">
            <div class="banner-tags">
              <span>岗位职责</span>
              <span>任职要求</span>
              <span>能力模型</span>
              <span>工作场景</span>
            </div>
            <h1 class="banner-title">{{ job.jobName }}</h1>
            <p class="banner-sub">一份来自一线从业者的完整岗位认知拆解</p>
          </template>
        </div>

        <h2 v-if="job" class="job-heading">{{ job.jobName }} · 岗位认知全解</h2>
      </div>
    </section>

    <!-- 正文两栏 -->
    <section class="body-wrap">
      <div class="container body-inner">
        <!-- 左：详细介绍 -->
        <div class="main-card" v-loading="loading">
          <template v-if="job">
            <div class="section-block">
              <h3 class="sec-title">岗位职责</h3>
              <p class="sec-text">{{ job.jobDuty || '暂无数据' }}</p>
            </div>
            <div class="section-block">
              <h3 class="sec-title">任职要求</h3>
              <p class="sec-text">{{ job.jobRequire || '暂无数据' }}</p>
            </div>
            <div class="section-block">
              <h3 class="sec-title">核心能力模型</h3>
              <p class="sec-text">{{ job.abilityModel || '暂无数据' }}</p>
            </div>
            <div class="section-block">
              <h3 class="sec-title">典型工作场景</h3>
              <p class="sec-text">{{ job.workScene || '暂无数据' }}</p>
            </div>
          </template>
          <el-empty v-else-if="!loading" description="岗位不存在或已下架" />
        </div>

        <!-- 右：知识库推荐 -->
        <aside class="side-card">
          <h3 class="side-title">知识库</h3>
          <div v-for="rec in recommends" :key="rec.id" class="rec-item" @click="goDetail(rec.id)">
            <div class="rec-cover">
              <img v-if="rec.coverUrl" :src="rec.coverUrl" :alt="rec.title" />
              <div v-else class="rec-fallback"><span>{{ rec.title.slice(0, 2) }}</span></div>
            </div>
            <div class="rec-info">
              <p class="rec-title">{{ rec.title }}</p>
              <p class="rec-meta">作者：{{ rec.authorName }}</p>
              <p class="rec-meta">{{ rec.subscribeCount || 0 }}人已订阅</p>
              <button class="rec-btn">详情</button>
            </div>
          </div>
          <el-empty v-if="recommends.length === 0 && !loading" description="暂无推荐" :image-size="60" />
        </aside>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getJobDetailApi, type JobCognition } from '@/api/job'
import { getKbRecommendApi, type KnowledgeCard } from '@/api/kb'

const route = useRoute()
const router = useRouter()

const jobId = computed(() => Number(route.params.id))
const loading = ref(false)
const job = ref<JobCognition | null>(null)
const recommends = ref<KnowledgeCard[]>([])

async function load() {
  loading.value = true
  try {
    const res = await getJobDetailApi(jobId.value)
    job.value = res.data
    const recRes = await getKbRecommendApi({ limit: 6 })
    recommends.value = recRes.data || []
  } finally {
    loading.value = false
  }
}

function goDetail(id: number) {
  router.push(`/article/${id}`)
}

watch(jobId, load, { immediate: true })
</script>

<style scoped>
/* ---------- 横幅 ---------- */
.hero-wrap {
  background: linear-gradient(180deg, #7fe0a8 0%, #d9f5e5 100%);
  padding: 18px 0 30px;
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
.banner {
  background: linear-gradient(135deg, #00b159 0%, #009e4d 60%, #008a43 100%);
  border-radius: 18px;
  padding: 52px 48px;
  color: #fff;
  box-shadow: 0 10px 30px rgba(0, 158, 77, 0.25);
  min-height: 180px;
}
.banner-tags {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.banner-tags span {
  height: 26px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  font-size: 12px;
  display: inline-flex;
  align-items: center;
}
.banner-title {
  margin: 0 0 10px;
  font-size: 34px;
  letter-spacing: 2px;
}
.banner-sub {
  margin: 0;
  font-size: 14px;
  opacity: 0.85;
}
.job-heading {
  margin: 22px 4px 0;
  font-size: 20px;
}

/* ---------- 正文两栏 ---------- */
.body-wrap {
  padding: 26px 0 50px;
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
  padding: 30px 34px;
  min-height: 320px;
}
.section-block {
  padding: 18px 0;
  border-bottom: 1px dashed rgba(31, 43, 37, 0.1);
}
.section-block:last-child {
  border-bottom: none;
}
.sec-title {
  margin: 0 0 10px;
  font-size: 16px;
  color: var(--brand-dark);
  padding-left: 12px;
  border-left: 4px solid var(--brand);
  line-height: 1.3;
}
.sec-text {
  margin: 0;
  font-size: 14px;
  color: var(--ink-2);
  line-height: 1.9;
  white-space: pre-line;
}

/* ---------- 右栏推荐（与详情页一致） ---------- */
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
  .banner-title {
    font-size: 24px;
  }
}
</style>
