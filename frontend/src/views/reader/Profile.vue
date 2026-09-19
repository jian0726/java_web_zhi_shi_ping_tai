<template>
  <div class="profile-page">
    <!-- 绿色渐变顶部 -->
    <section class="hero-wrap">
      <div class="container">
        <div class="crumb">
          <router-link to="/">首页</router-link>
          <span class="crumb-sep">&gt;</span>
          <span class="crumb-cur">个人中心</span>
        </div>

        <!-- 用户信息卡 -->
        <div class="user-card" v-loading="loading">
          <div class="user-ava">
            <el-icon :size="30" color="#fff"><User /></el-icon>
          </div>
          <div class="user-name-block">
            <p class="user-name">{{ profile?.nickname || '—' }}</p>
            <p class="user-sub">注册时间：{{ formatDate(profile?.createdAt) }}</p>
          </div>
          <div class="user-field">
            <p class="field-label">联系电话</p>
            <p class="field-value">{{ profile?.phone || '—' }}</p>
          </div>
          <div class="user-field">
            <p class="field-label">身份角色</p>
            <p class="field-value">读者</p>
          </div>
          <div class="user-field">
            <p class="field-label">单位 / 学校</p>
            <p class="field-value">未设置</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 已订阅 -->
    <section class="sub-wrap">
      <div class="container">
        <h2 class="sub-title">已订阅（{{ total }}）</h2>

        <template v-if="userStore.token">
          <el-empty v-if="!loading && subs.length === 0" description="还没有订阅内容，去首页看看吧" />
          <div v-else class="card-grid" v-loading="loading">
            <article
              v-for="card in subs"
              :key="card.id"
              class="kb-card"
              @click="goDetail(card.id)"
            >
              <div class="kb-cover">
                <img v-if="card.coverUrl" :src="card.coverUrl" :alt="card.title" />
                <div v-else class="cover-fallback"><span>{{ card.title.slice(0, 2) }}</span></div>
              </div>
              <div class="kb-info">
                <h3 class="kb-title">{{ card.title }}</h3>
                <p class="kb-summary">{{ card.summary || '点击查看详细内容' }}</p>
                <p class="kb-meta">
                  <span>作者：{{ card.authorName }}</span>
                  <span>{{ card.subscribeCount || 0 }}人已订阅</span>
                </p>
              </div>
            </article>
          </div>

          <div class="pager" v-if="total > pageSize">
            <el-pagination
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              :current-page="page"
              background
              @current-change="onPageChange"
            />
          </div>
        </template>

        <el-empty v-else description="登录后查看已订阅内容" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { User } from '@element-plus/icons-vue'
import { getProfileApi, type Profile } from '@/api/user'
import { getMySubscriptionsApi, type KnowledgeCard } from '@/api/kb'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const profile = ref<Profile | null>(null)
const subs = ref<KnowledgeCard[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 8

async function loadProfile() {
  if (!userStore.token) return
  const res = await getProfileApi()
  profile.value = res.data
}

async function loadSubs() {
  if (!userStore.token) return
  loading.value = true
  try {
    const res = await getMySubscriptionsApi({ page: page.value, size: pageSize })
    subs.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function onPageChange(p: number) {
  page.value = p
  loadSubs()
}

function goDetail(id: number) {
  router.push(`/article/${id}`)
}

function formatDate(d?: string) {
  if (!d) return '—'
  return String(d).slice(0, 10)
}

watch(
  () => userStore.token,
  (token) => {
    if (token) {
      loadProfile()
      loadSubs()
    }
  },
  { immediate: true }
)
</script>

<style scoped>
/* ---------- 顶部 ---------- */
.hero-wrap {
  background: linear-gradient(180deg, #7fe0a8 0%, #d9f5e5 100%);
  padding: 18px 0 0;
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
.user-card {
  background: #fff;
  border-radius: 18px 18px 0 0;
  box-shadow: 0 -6px 24px rgba(0, 158, 77, 0.1);
  padding: 26px 36px;
  display: flex;
  align-items: center;
  gap: 20px;
  min-height: 110px;
}
.user-ava {
  width: 62px;
  height: 62px;
  border-radius: 50%;
  background: var(--brand);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.user-name-block {
  margin-right: auto;
}
.user-name {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 700;
}
.user-sub {
  margin: 0;
  font-size: 12px;
  color: var(--ink-3);
}
.user-field {
  min-width: 180px;
  padding-left: 24px;
  border-left: 1px solid rgba(31, 43, 37, 0.08);
}
.field-label {
  margin: 0 0 8px;
  font-size: 12px;
  color: var(--ink-3);
}
.field-value {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

/* ---------- 已订阅 ---------- */
.sub-wrap {
  padding: 26px 0 50px;
}
.sub-title {
  margin: 0 0 22px;
  font-size: 22px;
}
.card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 22px;
  min-height: 180px;
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
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 4px;
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
.pager {
  display: flex;
  justify-content: center;
  margin-top: 36px;
}

@media (max-width: 992px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .user-card {
    flex-wrap: wrap;
  }
  .user-field {
    border-left: none;
    padding-left: 0;
    min-width: 40%;
  }
}
</style>
