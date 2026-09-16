<template>
  <div class="home">
    <!-- ==================== Hero ==================== -->
    <section class="hero">
      <div class="container hero-inner">
        <div class="hero-copy">
          <h1>好YOU经验</h1>
          <p class="hero-sub">
            提升职场<span class="hl">软技能</span>&nbsp;&nbsp;来这准没错！
          </p>
          <button class="hero-btn">开始学习</button>
        </div>
        <div class="hero-visual">
          <img :src="heroIllust" alt="开始学习" class="hero-img" />
        </div>
      </div>
      <div class="hero-dots">
        <span v-for="i in 4" :key="i" class="dot" :class="{ active: i === 1 }" />
      </div>
    </section>

    <!-- ==================== 热门职业岗位认知 ==================== -->
    <section class="section">
      <div class="container">
        <h2 class="section-title">热门职业岗位认知</h2>
        <p class="section-desc">财富积累 · 角色成长 · 职业规划 · 知识体系升级，做好这些职场软技能的全面认知</p>

        <div class="course-grid">
          <article v-for="c in courses" :key="c.title" class="course-card">
            <div class="course-cover">
              <img :src="c.cover" :alt="c.title" />
              <img v-if="c.hot" :src="hotBadge" alt="HOT" class="hot-badge" />
            </div>
            <h3>{{ c.title }}</h3>
            <p>{{ c.desc }}</p>
            <button class="course-btn">查看更多</button>
          </article>
        </div>
      </div>
    </section>

    <!-- ==================== 行业大类 ==================== -->
    <section class="section industry-section">
      <div class="container industry-inner">
        <div class="industry-panel">
          <h2>行业大类</h2>
          <p>分别开设大类课程，为你聚焦对应行业的经验与技能，快速找到属于你的成长路径。</p>
          <button class="industry-btn">查看更多</button>
        </div>
        <div class="industry-grid">
          <div v-for="item in industries" :key="item.label" class="industry-card">
            <span class="industry-icon" :style="{ color: item.color, background: item.bg }">
              <el-icon :size="22"><component :is="item.icon" /></el-icon>
            </span>
            <span class="industry-label">{{ item.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ==================== 用好YOU经验做什么？ ==================== -->
    <section class="section">
      <div class="container">
        <h2 class="section-title">用好YOU经验做什么？</h2>
        <p class="section-desc">你关心的职场 · 学习 · 技能 · 考证问题，在这里都能找到过来人的答案</p>

        <div
          v-for="(f, i) in features"
          :key="f.title"
          class="feature-row"
          :class="{ reverse: i % 2 === 1 }"
        >
          <div class="feature-copy">
            <img :src="f.num" :alt="'0' + (i + 1)" class="feature-num" />
            <h3>{{ f.title }}</h3>
            <p class="feature-sub">{{ f.sub }}</p>
            <p class="feature-desc">{{ f.desc }}</p>
          </div>
          <div class="feature-visual">
            <img :src="f.illust" :alt="f.title" />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import {
  Monitor,
  Coin,
  Reading,
  FirstAidKit,
  Management,
  OfficeBuilding,
  ChatDotRound,
  Film
} from '@element-plus/icons-vue'
import heroIllust from '@/assets/reader/hero-illust.png'
import hotBadge from '@/assets/reader/hot.png'
import course1 from '@/assets/reader/course-1.png'
import course2 from '@/assets/reader/course-2.png'
import course3 from '@/assets/reader/course-3.png'
import course4 from '@/assets/reader/course-4.png'
import num1 from '@/assets/reader/num-1.png'
import num2 from '@/assets/reader/num-2.png'
import num3 from '@/assets/reader/num-3.png'
import num4 from '@/assets/reader/num-4.png'
import feat1 from '@/assets/reader/feature-1.png'
import feat2 from '@/assets/reader/feature-2.png'
import feat3 from '@/assets/reader/feature-3.png'
import feat4 from '@/assets/reader/feature-4.png'

const courses = [
  {
    cover: course1,
    title: '商务英语速成课',
    desc: '明星私教带你快速练就地道职场英语口语',
    hot: true
  },
  {
    cover: course2,
    title: '运营专业进阶课',
    desc: '20 天掌握用户 / 内容 / 活动运营全流程',
    hot: false
  },
  {
    cover: course3,
    title: '思维导图实战课',
    desc: '用结构化思考工具，提升 10 倍工作效率',
    hot: false
  },
  {
    cover: course4,
    title: '职场心理直播课',
    desc: '心理调适与压力管理，第五班正在开课',
    hot: false
  }
]

const industries = [
  { label: '互联网 · 通信 · 电子', icon: Monitor, color: '#00b159', bg: '#e4f8ec' },
  { label: '金融 · 财经', icon: Coin, color: '#f5a700', bg: '#fff5da' },
  { label: '教育 · 文化 · 科研', icon: Reading, color: '#3d7dff', bg: '#e6efff' },
  { label: '医疗 · 专业服务', icon: FirstAidKit, color: '#ff7043', bg: '#ffefe8' },
  { label: '产品 · 项目 · 管理', icon: Management, color: '#00b159', bg: '#e4f8ec' },
  { label: '建筑 · 房地产', icon: OfficeBuilding, color: '#f5a700', bg: '#fff5da' },
  { label: '咨询 · 法律 · 人力', icon: ChatDotRound, color: '#3d7dff', bg: '#e6efff' },
  { label: '广告 · 传媒 · 公关', icon: Film, color: '#ff7043', bg: '#ffefe8' }
]

const features = [
  {
    num: num1,
    title: '系统性学习专业技能',
    sub: '构建完整的职业知识体系',
    desc: '一杯奶茶的价格，即可收获成体系的专业课程：知识点精讲 + 行业经验 + 前辈避坑指南，层层递进不走弯路，让每一次学习都指向真实成长。',
    illust: feat1
  },
  {
    num: num2,
    title: '全面提升参与工作场景',
    sub: '提升职业岗位的胜任能力',
    desc: '针对职场新人入职适应期与岗位进阶期设计，还原真实工作场景与业务流程，补齐课堂里学不到的实战能力，让每一分努力都有看得见的回报。',
    illust: feat2
  },
  {
    num: num3,
    title: '针对补齐课程短板',
    sub: '实现高等学历的提升目标',
    desc: '涵盖专升本、考研等重要学历提升路径的基础课程专项板块，配合系统化的学习计划与答疑服务，稳步达成学历进阶目标。',
    illust: feat3
  },
  {
    num: num4,
    title: '速补化掌握考点知识',
    sub: '提高职业技能考证通过率',
    desc: '针对职业技能考证打造速补课程，系统梳理考点手册、精讲高频难点与真题套路，显著提高考试成绩与拿证效率。',
    illust: feat4
  }
]
</script>

<style scoped>
/* ==================== Hero ==================== */
.hero {
  background: linear-gradient(135deg, #00c75f 0%, #00b159 100%);
  color: #fff;
  overflow: hidden;
}
.hero-inner {
  display: flex;
  align-items: center;
  min-height: 420px;
  gap: 40px;
}
.hero-copy h1 {
  margin: 0;
  font-size: 56px;
  font-weight: 700;
  letter-spacing: 2px;
}
.hero-sub {
  margin: 18px 0 0;
  font-size: 30px;
  font-weight: 500;
  opacity: 0.96;
}
.hero-sub .hl {
  color: var(--accent);
}
.hero-btn {
  margin-top: 36px;
  height: 44px;
  padding: 0 40px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.55);
  color: #fff;
  font-size: 15px;
  backdrop-filter: blur(4px);
  transition: background 0.2s;
}
.hero-btn:hover {
  background: rgba(255, 255, 255, 0.34);
}
.hero-visual {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}
.hero-img {
  width: min(620px, 52vw);
  height: auto;
  object-fit: contain;
  mix-blend-mode: normal;
}
.hero-dots {
  display: flex;
  justify-content: center;
  gap: 10px;
  padding-bottom: 22px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.45);
  transition: all 0.2s;
}
.dot.active {
  width: 22px;
  border-radius: 999px;
  background: #fff;
}

/* ==================== 通用区块 ==================== */
.section {
  padding: 72px 0 0;
}
.section-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: var(--ink);
}
.section-desc {
  margin: 12px 0 0;
  font-size: 14px;
  color: var(--ink-3);
}

/* ==================== 热门课程 ==================== */
.course-grid {
  margin-top: 40px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}
.course-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 24px rgba(31, 43, 37, 0.07);
  transition: transform 0.25s, box-shadow 0.25s;
  display: flex;
  flex-direction: column;
}
.course-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 14px 32px rgba(31, 43, 37, 0.12);
}
.course-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  overflow: hidden;
}
.course-cover img:first-child {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.hot-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  width: 44px;
  height: auto;
}
.course-card h3 {
  margin: 16px 18px 0;
  font-size: 17px;
  font-weight: 600;
}
.course-card p {
  margin: 8px 18px 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--ink-3);
  flex: 1;
}
.course-btn {
  margin: 16px 18px 18px;
  height: 34px;
  width: 108px;
  border-radius: 999px;
  background: var(--brand);
  color: #fff;
  font-size: 13px;
  transition: background 0.2s;
}
.course-btn:hover {
  background: var(--brand-deep);
}

/* ==================== 行业大类 ==================== */
.industry-section {
  margin-top: 72px;
  padding: 72px 0 !important;
  background: var(--bg-soft);
}
.industry-inner {
  display: flex;
  gap: 32px;
  align-items: stretch;
}
.industry-panel {
  flex: 0 0 300px;
  background: linear-gradient(135deg, #00c75f, #00b159);
  border-radius: 16px;
  color: #fff;
  padding: 40px 32px;
  display: flex;
  flex-direction: column;
}
.industry-panel h2 {
  margin: 0;
  font-size: 30px;
  font-weight: 700;
}
.industry-panel p {
  margin: 16px 0 0;
  font-size: 13px;
  line-height: 1.9;
  opacity: 0.92;
  flex: 1;
}
.industry-btn {
  align-self: flex-start;
  margin-top: 28px;
  height: 38px;
  padding: 0 28px;
  border-radius: 999px;
  background: var(--accent);
  color: #5a4300;
  font-size: 14px;
  font-weight: 600;
  transition: filter 0.2s;
}
.industry-btn:hover {
  filter: brightness(1.06);
}
.industry-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-auto-rows: 1fr;
  gap: 20px;
}
.industry-card {
  background: #fff;
  border-radius: 12px;
  padding: 22px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 4px 16px rgba(31, 43, 37, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}
.industry-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 24px rgba(31, 43, 37, 0.1);
}
.industry-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.industry-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--ink);
  line-height: 1.5;
}

/* ==================== 功能介绍 ==================== */
.feature-row {
  margin-top: 88px;
  display: flex;
  align-items: center;
  gap: 72px;
}
.feature-row.reverse {
  flex-direction: row-reverse;
}
.feature-copy {
  flex: 1;
}
.feature-num {
  width: 92px;
  height: auto;
}
.feature-copy h3 {
  margin: 14px 0 0;
  font-size: 26px;
  font-weight: 700;
  color: var(--ink);
}
.feature-sub {
  margin: 10px 0 0;
  font-size: 28px;
  font-weight: 300;
  color: #b9c4be;
  letter-spacing: 1px;
}
.feature-desc {
  margin: 22px 0 0;
  max-width: 460px;
  font-size: 14px;
  line-height: 2;
  color: var(--ink-2);
}
.feature-visual {
  flex: 1;
  display: flex;
  justify-content: center;
}
.feature-visual img {
  width: min(440px, 100%);
  height: auto;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1024px) {
  .course-grid,
  .industry-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .industry-inner {
    flex-direction: column;
  }
  .industry-panel {
    flex: none;
  }
  .feature-row,
  .feature-row.reverse {
    flex-direction: column;
    gap: 32px;
    text-align: left;
  }
  .hero-inner {
    flex-direction: column;
    padding: 40px 0;
    text-align: center;
  }
  .hero-visual {
    justify-content: center;
  }
}
</style>
