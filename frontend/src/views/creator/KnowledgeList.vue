<template>
  <div class="kb-manage">
    <div class="page-head">
      <h1 class="page-title">知识库管理列表</h1>
      <div class="head-right">
        <el-radio-group v-model="statusFilter" @change="reload">
          <el-radio-button :value="-1">全部</el-radio-button>
          <el-radio-button :value="0">草稿</el-radio-button>
          <el-radio-button :value="1">待审核</el-radio-button>
          <el-radio-button :value="2">已发布</el-radio-button>
          <el-radio-button :value="3">已驳回</el-radio-button>
        </el-radio-group>
        <button class="btn-primary" @click="openCreate">
          <el-icon><Plus /></el-icon>
          创建知识库
        </button>
      </div>
    </div>

    <el-empty v-if="!loading && rows.length === 0" description="还没有知识库，点右上角创建一个吧" />
    <div v-else class="card-list" v-loading="loading">
      <article v-for="row in rows" :key="row.id" class="kb-row" @click="goDetail(row.id)">
        <div class="kb-cover">
          <img v-if="row.coverUrl" :src="row.coverUrl" alt="" />
          <span v-else>{{ row.name.slice(0, 2) }}</span>
        </div>
        <div class="kb-body">
          <h2 class="kb-title">{{ row.name }}</h2>
          <p class="kb-meta">
            <span class="meta-chip">{{ row.kbType || '未分类' }}</span>
            <span class="meta-text">作者：{{ row.authorName }}</span>
            <span class="meta-text">更新：{{ fmtDate(row.updatedAt) }}</span>
          </p>
          <p class="kb-summary">{{ row.summary || '暂无摘要' }}</p>
          <div class="kb-tags">
            <span class="tag" :class="statusClass(row.status)">{{ statusText(row.status) }}</span>
            <span class="tag plain">V{{ row.currentVersion }}</span>
            <span class="tag plain">{{ row.moduleCount || 0 }} 模块</span>
            <span class="tag plain">{{ row.articleCount || 0 }} 文章</span>
          </div>
        </div>
        <div class="kb-ops" @click.stop>
          <button class="op-btn" :disabled="row.status === 1" @click="openEdit(row)">编辑</button>
          <button class="op-btn danger" :disabled="row.status === 1" @click="onDelete(row)">删除</button>
        </div>
      </article>
    </div>

    <div class="pager">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        background
        @current-change="onPage"
      />
    </div>

    <!-- 创建 / 编辑抽屉 -->
    <el-drawer v-model="drawer" :title="editingId ? '编辑知识库' : '新建知识库'" size="480px">
      <el-form label-position="top" class="kb-form">
        <el-form-item label="知识库名称" required>
          <el-input v-model="form.name" placeholder="请输入知识库名称..." maxlength="100" />
        </el-form-item>
        <el-form-item label="知识类别">
          <el-select v-model="form.kbType" placeholder="请选择类别" style="width: 100%" clearable>
            <el-option v-for="t in K_TYPES" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="知识库摘要">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="请输入..." maxlength="200" />
        </el-form-item>
        <el-form-item label="课程介绍（富文本）">
          <el-input v-model="form.intro" type="textarea" :rows="6" placeholder="支持 HTML 片段，展示在知识库详情页「课程介绍」" />
        </el-form-item>
        <el-form-item label="显示类型">
          <el-radio-group v-model="form.displayType">
            <el-radio value="公开">公开</el-radio>
            <el-radio value="私有">私有</el-radio>
            <el-radio value="隐藏">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上传封面（JPG/PNG，≤10MB）">
          <div class="cover-upload">
            <div v-if="form.coverUrl" class="cover-preview">
              <img :src="form.coverUrl" alt="封面" />
              <button class="cover-del" @click="form.coverUrl = ''">移除</button>
            </div>
            <el-upload
              v-else
              :show-file-list="false"
              :auto-upload="false"
              accept="image/jpeg,image/png"
              @change="onCoverPick"
            >
              <div class="cover-placeholder">
                <el-icon :size="26"><Plus /></el-icon>
                <span>上传封面</span>
              </div>
            </el-upload>
            <span v-if="coverUploading" class="cover-tip">上传中…</span>
          </div>
        </el-form-item>
        <el-alert type="info" :closable="false" show-icon title="创建后在知识库详情里继续添加模块与文章" />
      </el-form>
      <template #footer>
        <div class="drawer-foot">
          <button class="btn-plain" @click="drawer = false">关闭</button>
          <button class="btn-plain ghost" :disabled="saving" @click="save(false)">存草稿</button>
          <button class="btn-primary" :disabled="saving" @click="save(true)">保存并提交审核</button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadFile } from 'element-plus'
import {
  myKnowledgeApi,
  myBaseDetailApi,
  createBaseApi,
  updateBaseApi,
  deleteBaseApi,
  type BaseRow
} from '@/api/creator'
import { uploadFileApi } from '@/api/user'
import { useUserStore } from '@/store/user'

const K_TYPES = ['职业技能', '求职面试', '职场软技能', '行业认知', '学习方法']

const router = useRouter()
const userStore = useUserStore()

const rows = ref<BaseRow[]>([])
const total = ref(0)
const page = ref(1)
const size = 6
const statusFilter = ref(-1)
const loading = ref(false)
const saving = ref(false)

const drawer = ref(false)
const editingId = ref<number | null>(null)
const coverUploading = ref(false)
const form = ref({ ...emptyForm() })

function emptyForm() {
  return { name: '', kbType: '', summary: '', intro: '', displayType: '公开', coverUrl: '' }
}

async function onCoverPick(file: UploadFile) {
  const raw = file.raw
  if (!raw) return
  if (raw.size > 10 * 1024 * 1024) {
    ElMessage.warning('封面不能超过 10MB')
    return
  }
  coverUploading.value = true
  try {
    const res = await uploadFileApi(raw)
    if (res.data) {
      form.value.coverUrl = res.data.url
      ElMessage.success('封面上传成功')
    }
  } finally {
    coverUploading.value = false
  }
}

async function load() {
  if (!userStore.token) {
    ElMessage.warning('请先在读者端登录')
    return
  }
  loading.value = true
  try {
    const res = await myKnowledgeApi({
      status: statusFilter.value === -1 ? undefined : statusFilter.value,
      page: page.value,
      size
    })
    rows.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

function onPage(p: number) {
  page.value = p
  load()
}

function openCreate() {
  editingId.value = null
  form.value = emptyForm()
  drawer.value = true
}

function openEdit(row: BaseRow) {
  editingId.value = row.id
  form.value = {
    name: row.name,
    kbType: row.kbType || '',
    summary: row.summary || '',
    intro: '',
    displayType: '公开',
    coverUrl: row.coverUrl || ''
  }
  drawer.value = true
  myBaseDetailApi(row.id).then((res) => {
    if (res.data) {
      form.value.intro = res.data.intro || ''
      form.value.displayType = res.data.displayType || '公开'
      form.value.coverUrl = res.data.coverUrl || ''
    }
  })
}

async function save(submit: boolean) {
  if (!form.value.name.trim()) {
    ElMessage.warning('请填写知识库名称')
    return
  }
  saving.value = true
  try {
    const payload = { ...form.value, submit }
    if (editingId.value) {
      await updateBaseApi(editingId.value, payload)
    } else {
      await createBaseApi(payload)
    }
    ElMessage.success(submit ? '已提交审核' : '草稿已保存')
    drawer.value = false
    reload()
  } finally {
    saving.value = false
  }
}

async function onDelete(row: BaseRow) {
  await ElMessageBox.confirm(`确定删除知识库《${row.name}》吗？其下模块与文章将一并失效。`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteBaseApi(row.id)
  ElMessage.success('已删除')
  load()
}

function goDetail(id: number) {
  router.push(`/creator/knowledge/${id}`)
}

function statusText(s: number) {
  return ['草稿', '待审核', '已发布', '已驳回'][s] ?? '未知'
}
function statusClass(s: number) {
  return ['draft', 'pending', 'published', 'rejected'][s] ?? 'draft'
}
function fmtDate(d?: string) {
  return d ? String(d).slice(0, 10) : '—'
}

onMounted(load)
</script>

<style scoped>
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-title {
  margin: 0;
  font-size: 22px;
}
.head-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.btn-primary {
  height: 38px;
  padding: 0 20px;
  border-radius: 8px;
  background: var(--brand);
  color: #fff;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.btn-primary:hover {
  background: var(--brand-deep);
}
.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 160px;
}
.kb-row {
  display: flex;
  gap: 18px;
  background: #fff;
  border-radius: 12px;
  padding: 18px 22px;
  box-shadow: 0 2px 10px rgba(31, 43, 37, 0.06);
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.kb-row:hover {
  box-shadow: 0 6px 20px rgba(0, 199, 95, 0.14);
}
.kb-cover {
  width: 96px;
  height: 96px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(0, 199, 95, 0.9), rgba(0, 158, 77, 0.85));
  display: flex;
  align-items: center;
  justify-content: center;
}
.kb-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.kb-cover span {
  color: #fff;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 2px;
}
.kb-body {
  flex: 1;
  min-width: 0;
}
.kb-title {
  margin: 0 0 8px;
  font-size: 17px;
}
.kb-meta {
  margin: 0 0 8px;
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 12px;
  color: var(--ink-3);
}
.meta-chip {
  background: rgba(0, 199, 95, 0.1);
  color: var(--brand);
  border-radius: 4px;
  padding: 1px 8px;
  font-weight: 600;
}
.kb-summary {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--ink-2);
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.kb-tags {
  display: flex;
  gap: 8px;
}
.tag {
  font-size: 11px;
  border-radius: 4px;
  padding: 2px 10px;
  font-weight: 600;
}
.tag.draft {
  background: #f0f2f1;
  color: var(--ink-3);
}
.tag.pending {
  background: #fff7e0;
  color: #c78a00;
}
.tag.published {
  background: rgba(0, 199, 95, 0.12);
  color: var(--brand-dark);
}
.tag.rejected {
  background: #fdeaea;
  color: #d03050;
}
.tag.plain {
  background: #f0f2f1;
  color: var(--ink-2);
}
.kb-ops {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
}
.op-btn {
  height: 30px;
  padding: 0 18px;
  border-radius: 6px;
  border: 1px solid var(--brand);
  background: #fff;
  color: var(--brand);
  font-size: 13px;
}
.op-btn:hover:not(:disabled) {
  background: var(--brand);
  color: #fff;
}
.op-btn.danger {
  border-color: #d03050;
  color: #d03050;
}
.op-btn.danger:hover:not(:disabled) {
  background: #d03050;
  color: #fff;
}
.op-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 22px;
}

.drawer-foot {
  display: flex;
  gap: 10px;
}
.btn-plain {
  height: 36px;
  padding: 0 22px;
  border-radius: 6px;
  border: 1px solid rgba(31, 43, 37, 0.15);
  background: #fff;
  color: var(--ink-2);
  font-size: 13px;
}
.btn-plain.ghost {
  border-color: var(--brand);
  color: var(--brand);
}
.drawer-foot .btn-primary {
  margin-left: auto;
}

/* 封面上传 */
.cover-upload {
  display: flex;
  align-items: center;
  gap: 14px;
}
.cover-preview {
  position: relative;
  width: 160px;
  height: 90px;
  border-radius: 8px;
  overflow: hidden;
}
.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-del {
  position: absolute;
  right: 6px;
  bottom: 6px;
  height: 22px;
  padding: 0 10px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 11px;
}
.cover-placeholder {
  width: 160px;
  height: 90px;
  border: 1px dashed rgba(31, 43, 37, 0.25);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: var(--ink-3);
  font-size: 12px;
  cursor: pointer;
}
.cover-placeholder:hover {
  border-color: var(--brand);
  color: var(--brand);
}
.cover-tip {
  font-size: 12px;
  color: var(--ink-3);
}
</style>
