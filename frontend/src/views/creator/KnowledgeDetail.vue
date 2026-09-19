<template>
  <div class="kb-detail" v-loading="loading">
    <div class="crumb">
      <router-link to="/creator">知识库管理</router-link>
      <span class="sep">&gt;</span>
      <span>知识库详情</span>
    </div>

    <template v-if="detail">
      <!-- 知识库信息卡 -->
      <div class="hero card">
        <div class="hero-cover">
          <img v-if="detail.coverUrl" :src="detail.coverUrl" alt="" />
          <span v-else>{{ detail.name.slice(0, 2) }}</span>
        </div>
        <div class="hero-info">
          <h1 class="title">{{ detail.name }}</h1>
          <p class="meta">
            <span class="chip">{{ detail.kbType || '未分类' }}</span>
            <span class="tag" :class="statusClass(detail.status)">{{ statusText(detail.status) }}</span>
            <span>V{{ detail.currentVersion }}</span>
            <span>编号：{{ detail.kbNo }}</span>
            <span>{{ detail.subscribeCount || 0 }}人已订阅</span>
          </p>
          <p class="summary">{{ detail.summary || '暂无摘要' }}</p>
          <div class="ops">
            <button class="btn-primary" :disabled="detail.status === 1" @click="openEdit">编辑信息</button>
            <span v-if="detail.status === 1" class="tip">审核中，暂不可编辑</span>
            <span v-else-if="detail.status === 3 && rejectRemark" class="tip danger">驳回理由：{{ rejectRemark }}</span>
          </div>
        </div>
      </div>

      <!-- 模块与文章管理 -->
      <div class="card">
        <div class="sec-head">
          <h2 class="sec-title">知识模块与文章</h2>
          <div class="sec-ops">
            <el-input v-model="newModuleName" placeholder="新模块名称" maxlength="50" style="width: 200px" />
            <button class="btn-outline" @click="addModule">+ 添加模块</button>
          </div>
        </div>

        <el-empty v-if="modules.length === 0" description="还没有模块，先添加一个模块（章节）" :image-size="70" />
        <div v-for="mod in modules" :key="mod.id" class="module-block">
          <div class="module-head">
            <el-icon :size="16" color="var(--brand)"><FolderOpened /></el-icon>
            <span class="module-name">{{ mod.moduleName }}</span>
            <el-button size="small" text type="primary" @click="openArticleEdit(mod, null)">+ 文章</el-button>
            <el-button size="small" text @click="renameModule(mod)">改名</el-button>
            <el-button size="small" text type="danger" @click="deleteModule(mod)">删除</el-button>
          </div>
          <div v-if="articlesByModule[mod.id]?.length" class="article-list">
            <div v-for="a in articlesByModule[mod.id]" :key="a.id" class="article-row">
              <span class="article-title" :title="a.title">{{ a.title }}</span>
              <span class="article-date">{{ fmtDate(a.updatedAt) }}</span>
              <el-button size="small" text type="primary" @click="openArticleEdit(mod, a)">编辑</el-button>
              <el-button size="small" text type="danger" @click="deleteArticle(a)">删除</el-button>
            </div>
          </div>
          <p v-else class="module-empty">模块下还没有文章</p>
        </div>
      </div>

      <!-- 协作者 -->
      <div class="card">
        <div class="sec-head">
          <h2 class="sec-title">协作者</h2>
          <div class="sec-ops">
            <el-input v-model="newPhone" placeholder="输入协作者手机号" maxlength="11" style="width: 200px" />
            <button class="btn-outline" @click="addCollab">添加协作者</button>
          </div>
        </div>
        <el-table :data="collaborators" v-loading="collabLoading" size="small">
          <el-table-column prop="nickname" label="昵称" width="140" />
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column label="角色" width="100">
            <template #default="{ row }">
              <span class="tag" :class="row.role === 'OWNER' ? 'owner' : 'member'">
                {{ row.role === 'OWNER' ? '所有者' : '协作者' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="添加时间" width="120">
            <template #default="{ row }">{{ fmtDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button v-if="row.role !== 'OWNER'" size="small" type="danger" @click="removeCollab(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>
    <el-empty v-else-if="!loading" description="知识库不存在或无权查看" />

    <!-- 编辑知识库信息抽屉 -->
    <el-drawer v-model="editDrawer" title="编辑知识库信息" size="480px">
      <el-form label-position="top">
        <el-form-item label="知识库名称">
          <el-input v-model="editForm.name" maxlength="100" />
        </el-form-item>
        <el-form-item label="知识类别">
          <el-select v-model="editForm.kbType" style="width: 100%" clearable>
            <el-option v-for="t in K_TYPES" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="editForm.summary" type="textarea" :rows="3" maxlength="200" />
        </el-form-item>
        <el-form-item label="课程介绍（富文本）">
          <el-input v-model="editForm.intro" type="textarea" :rows="8" />
        </el-form-item>
        <el-form-item label="显示类型">
          <el-radio-group v-model="editForm.displayType">
            <el-radio value="公开">公开</el-radio>
            <el-radio value="私有">私有</el-radio>
            <el-radio value="隐藏">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-foot">
          <el-button @click="editDrawer = false">取消</el-button>
          <el-button
            type="primary"
            :disabled="saving || detail?.status === 1"
            @click="saveEdit"
          >保存</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 文章编辑弹窗 -->
    <el-dialog v-model="articleBox" :title="editingArticle ? '编辑文章' : '添加文章'" width="620px" top="6vh">
      <el-form label-position="top">
        <el-form-item label="所属模块">
          <el-select v-model="articleForm.moduleId" style="width: 100%">
            <el-option v-for="m in modules" :key="m.id" :label="m.moduleName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="文章标题" required>
          <el-input v-model="articleForm.title" maxlength="200" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="articleForm.summary" type="textarea" :rows="2" maxlength="300" />
        </el-form-item>
        <el-form-item label="正文（支持 HTML 片段）" required>
          <el-input v-model="articleForm.content" type="textarea" :rows="12" placeholder="请输入正文..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="articleBox = false">取消</el-button>
        <el-button type="primary" :disabled="articleSaving" @click="saveArticle">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FolderOpened } from '@element-plus/icons-vue'
import {
  myBaseDetailApi,
  updateBaseApi,
  moduleListApi,
  moduleCreateApi,
  moduleUpdateApi,
  moduleDeleteApi,
  articleListApi,
  articleGetApi,
  articleCreateApi,
  articleUpdateApi,
  articleDeleteApi,
  myApplyApi,
  collaboratorsApi,
  addCollaboratorApi,
  removeCollaboratorApi,
  type BaseDetail,
  type ModuleRow,
  type ArticleRow,
  type CollaboratorRow
} from '@/api/creator'

const K_TYPES = ['职业技能', '求职面试', '职场软技能', '行业认知', '学习方法']

const route = useRoute()
const baseId = computed(() => Number(route.params.id))

const loading = ref(false)
const saving = ref(false)
const detail = ref<BaseDetail | null>(null)
const rejectRemark = ref('')

const modules = ref<ModuleRow[]>([])
const articles = ref<ArticleRow[]>([])
const collaborators = ref<CollaboratorRow[]>([])
const collabLoading = ref(false)
const newPhone = ref('')
const newModuleName = ref('')

const editDrawer = ref(false)
const editForm = reactive({ name: '', kbType: '', summary: '', intro: '', displayType: '公开' })

const articleBox = ref(false)
const articleSaving = ref(false)
const editingArticle = ref<ArticleRow | null>(null)
const editingModule = ref<ModuleRow | null>(null)
const articleForm = reactive({ moduleId: 0, title: '', summary: '', content: '' })

const articlesByModule = computed<Record<number, ArticleRow[]>>(() => {
  const map: Record<number, ArticleRow[]> = {}
  for (const a of articles.value) {
    (map[a.moduleId] = map[a.moduleId] || []).push(a)
  }
  return map
})

async function load() {
  loading.value = true
  try {
    const res = await myBaseDetailApi(baseId.value)
    detail.value = res.data
    const apply = await myApplyApi()
    if (apply.data && apply.data.status === 2) {
      rejectRemark.value = apply.data.auditRemark
    }
    await Promise.all([loadModules(), loadArticles(), loadCollab()])
  } finally {
    loading.value = false
  }
}

async function loadModules() {
  const res = await moduleListApi(baseId.value)
  modules.value = res.data || []
}

async function loadArticles() {
  const res = await articleListApi(baseId.value)
  articles.value = res.data || []
}

async function loadCollab() {
  collabLoading.value = true
  try {
    const res = await collaboratorsApi(baseId.value)
    collaborators.value = res.data || []
  } finally {
    collabLoading.value = false
  }
}

// ---------- 知识库信息 ----------
function openEdit() {
  if (!detail.value) return
  Object.assign(editForm, {
    name: detail.value.name,
    kbType: detail.value.kbType || '',
    summary: detail.value.summary || '',
    intro: detail.value.intro || '',
    displayType: detail.value.displayType || '公开'
  })
  editDrawer.value = true
}

async function saveEdit() {
  saving.value = true
  try {
    await updateBaseApi(baseId.value, editForm)
    ElMessage.success('已保存')
    editDrawer.value = false
    load()
  } finally {
    saving.value = false
  }
}

// ---------- 模块 ----------
async function addModule() {
  const name = newModuleName.value.trim()
  if (!name) return ElMessage.warning('请输入模块名称')
  await moduleCreateApi(baseId.value, name)
  newModuleName.value = ''
  ElMessage.success('模块已添加')
  loadModules()
}

async function renameModule(mod: ModuleRow) {
  const name = await ElMessageBox.prompt('修改模块名称', '改名', {
    inputValue: mod.moduleName,
    confirmButtonText: '保存',
    cancelButtonText: '取消'
  })
  await moduleUpdateApi(baseId.value, mod.id, { moduleName: name.value.trim() })
  ElMessage.success('已修改')
  loadModules()
}

async function deleteModule(mod: ModuleRow) {
  await ElMessageBox.confirm(`确定删除模块「${mod.moduleName}」吗？（模块下需无文章）`, '删除确认', { type: 'warning' })
  await moduleDeleteApi(baseId.value, mod.id)
  ElMessage.success('已删除')
  loadModules()
}

// ---------- 文章 ----------
function openArticleEdit(mod: ModuleRow, article: ArticleRow | null) {
  editingModule.value = mod
  editingArticle.value = article
  articleForm.moduleId = mod.id
  articleForm.title = article?.title || ''
  articleForm.summary = article?.summary || ''
  articleForm.content = ''
  if (article) {
    articleGetApi(baseId.value, article.id).then((res) => {
      articleForm.content = res.data?.content || ''
    })
  }
  articleBox.value = true
}

async function saveArticle() {
  if (!articleForm.title.trim()) return ElMessage.warning('请填写文章标题')
  articleSaving.value = true
  try {
    if (editingArticle.value) {
      await articleUpdateApi(baseId.value, editingArticle.value.id, articleForm)
    } else {
      await articleCreateApi(baseId.value, articleForm)
    }
    ElMessage.success('已保存')
    articleBox.value = false
    loadArticles()
  } finally {
    articleSaving.value = false
  }
}

async function deleteArticle(a: ArticleRow) {
  await ElMessageBox.confirm(`确定删除文章《${a.title}》吗？`, '删除确认', { type: 'warning' })
  await articleDeleteApi(baseId.value, a.id)
  ElMessage.success('已删除')
  loadArticles()
}

// ---------- 协作者 ----------
async function addCollab() {
  if (!/^1\d{10}$/.test(newPhone.value.trim())) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  await addCollaboratorApi(baseId.value, newPhone.value.trim())
  ElMessage.success('已添加')
  newPhone.value = ''
  loadCollab()
}

async function removeCollab(row: CollaboratorRow) {
  await ElMessageBox.confirm(`确定移除协作者「${row.nickname}」吗？`, '移除确认', { type: 'warning' })
  await removeCollaboratorApi(baseId.value, row.id)
  ElMessage.success('已移除')
  loadCollab()
}

// ---------- 工具 ----------
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
.crumb {
  font-size: 13px;
  color: var(--ink-3);
  margin-bottom: 16px;
}
.crumb a:hover {
  color: var(--brand);
}
.sep {
  margin: 0 8px;
}
.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(31, 43, 37, 0.06);
  padding: 24px 28px;
  margin-bottom: 18px;
}
.hero {
  display: flex;
  gap: 24px;
}
.hero-cover {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(0, 199, 95, 0.9), rgba(0, 158, 77, 0.85));
  display: flex;
  align-items: center;
  justify-content: center;
}
.hero-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.hero-cover span {
  color: #fff;
  font-size: 30px;
  font-weight: 700;
}
.hero-info {
  flex: 1;
  min-width: 0;
}
.title {
  margin: 0 0 10px;
  font-size: 22px;
}
.meta {
  margin: 0 0 10px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--ink-3);
  flex-wrap: wrap;
}
.chip {
  background: rgba(0, 199, 95, 0.1);
  color: var(--brand);
  border-radius: 4px;
  padding: 1px 8px;
  font-weight: 600;
}
.tag {
  font-size: 11px;
  border-radius: 4px;
  padding: 2px 10px;
  font-weight: 600;
}
.tag.draft { background: #f0f2f1; color: var(--ink-3); }
.tag.pending { background: #fff7e0; color: #c78a00; }
.tag.published { background: rgba(0, 199, 95, 0.12); color: var(--brand-dark); }
.tag.rejected { background: #fdeaea; color: #d03050; }
.tag.owner { background: rgba(0, 199, 95, 0.12); color: var(--brand-dark); }
.tag.member { background: #f0f2f1; color: var(--ink-2); }
.summary {
  margin: 0 0 14px;
  font-size: 13px;
  color: var(--ink-2);
  line-height: 1.8;
}
.ops {
  display: flex;
  align-items: center;
  gap: 14px;
}
.tip {
  font-size: 12px;
  color: var(--ink-3);
}
.tip.danger {
  color: #d03050;
}
.btn-primary {
  height: 36px;
  padding: 0 24px;
  border-radius: 8px;
  background: var(--brand);
  color: #fff;
  font-size: 14px;
}
.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-outline {
  height: 32px;
  padding: 0 16px;
  border-radius: 6px;
  border: 1px solid var(--brand);
  background: #fff;
  color: var(--brand);
  font-size: 13px;
}
.sec-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 14px;
  flex-wrap: wrap;
}
.sec-title {
  margin: 0;
  font-size: 17px;
  padding-left: 10px;
  border-left: 4px solid var(--brand);
  line-height: 1.3;
}
.sec-ops {
  display: flex;
  gap: 10px;
  align-items: center;
}
.module-block {
  border: 1px solid rgba(31, 43, 37, 0.08);
  border-radius: 10px;
  padding: 14px 18px;
  margin-bottom: 14px;
}
.module-head {
  display: flex;
  align-items: center;
  gap: 10px;
}
.module-name {
  font-weight: 600;
  font-size: 15px;
  flex: 1;
}
.article-list {
  margin-top: 10px;
  border-top: 1px dashed rgba(31, 43, 37, 0.1);
  padding-top: 8px;
}
.article-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 4px;
  border-radius: 6px;
}
.article-row:hover {
  background: var(--bg-soft);
}
.article-title {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.article-date {
  font-size: 12px;
  color: var(--ink-3);
}
.module-empty {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--ink-3);
}
.drawer-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
