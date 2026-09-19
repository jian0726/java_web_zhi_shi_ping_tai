<template>
  <div class="page">
    <div class="page-head">
      <h1 class="page-title">审核知识库</h1>
      <el-radio-group v-model="statusFilter" @change="reload">
        <el-radio-button :value="1">待审核</el-radio-button>
        <el-radio-button :value="2">已发布</el-radio-button>
        <el-radio-button :value="3">已驳回</el-radio-button>
        <el-radio-button :value="0">草稿</el-radio-button>
      </el-radio-group>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="name" label="知识库名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="kbNo" label="编号" width="170" />
        <el-table-column prop="kbType" label="类别" width="110" />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column label="模块/文章" width="110">
          <template #default="{ row }">{{ row.moduleCount || 0 }} / {{ row.articleCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="版本" width="70">
          <template #default="{ row }">V{{ row.currentVersion }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <span class="tag" :class="['draft', 'pending', 'ok', 'rejected'][row.status]">
              {{ ['草稿', '待审核', '已发布', '已驳回'][row.status] }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="120">
          <template #default="{ row }">{{ fmtDate(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 1">
              <el-button size="small" type="primary" @click="openDetail(row)">审核</el-button>
              <el-button size="small" @click="openReject(row)">驳回</el-button>
            </template>
            <el-button v-else size="small" @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

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
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailBox" :title="current?.name || '审核详情'" width="680px" top="6vh">
      <div v-loading="detailLoading" class="dlg-body">
        <template v-if="detail">
          <p class="dlg-row">
            <b>编号：</b>{{ detail.kbNo }}&nbsp;&nbsp;
            <b>作者：</b>{{ detail.authorName }}&nbsp;&nbsp;
            <b>类别：</b>{{ detail.kbType || '—' }}
          </p>
          <p class="dlg-row"><b>摘要：</b>{{ detail.summary || '—' }}</p>
          <div class="rich" v-html="detail.intro || '<p>暂无介绍</p>'"></div>
        </template>
      </div>
      <template #footer>
        <el-button @click="detailBox = false">关闭</el-button>
        <el-button v-if="current?.status === 1" type="danger" @click="openReject(current!)">不通过</el-button>
        <el-button v-if="current?.status === 1" type="primary" @click="approve">通过并发布</el-button>
      </template>
    </el-dialog>

    <!-- 驳回理由弹窗 -->
    <el-dialog v-model="rejectBox" title="审核不通过理由" width="440px">
      <el-input v-model="rejectRemark" type="textarea" :rows="4" placeholder="请填写不通过理由（必填）" />
      <template #footer>
        <el-button @click="rejectBox = false">取消</el-button>
        <el-button type="primary" :disabled="!rejectRemark.trim()" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { auditPendingApi, auditDetailApi, auditActionApi, type AuditRow } from '@/api/admin'

const rows = ref<AuditRow[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const statusFilter = ref(1)
const loading = ref(false)

const detailBox = ref(false)
const detailLoading = ref(false)
const detail = ref<Record<string, unknown> | null>(null)
const current = ref<AuditRow | null>(null)
const rejectBox = ref(false)
const rejectRemark = ref('')

async function load() {
  loading.value = true
  try {
    const res = await auditPendingApi({ status: statusFilter.value, page: page.value, size })
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

async function openDetail(row: AuditRow) {
  current.value = row
  detail.value = null
  detailBox.value = true
  detailLoading.value = true
  try {
    const res = await auditDetailApi(row.id)
    detail.value = res.data
  } finally {
    detailLoading.value = false
  }
}

async function approve() {
  if (!current.value) return
  await auditActionApi(current.value.id, { action: 1 })
  ElMessage.success('已通过并发布')
  detailBox.value = false
  load()
}

function openReject(row: AuditRow) {
  current.value = row
  rejectRemark.value = ''
  rejectBox.value = true
}

async function confirmReject() {
  if (!current.value) return
  await auditActionApi(current.value.id, { action: 2, remark: rejectRemark.value.trim() })
  ElMessage.success('已驳回')
  rejectBox.value = false
  detailBox.value = false
  load()
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
  margin-bottom: 18px;
}
.page-title {
  margin: 0;
  font-size: 22px;
}
.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(31, 43, 37, 0.06);
  padding: 20px;
}
.tag {
  font-size: 11px;
  border-radius: 4px;
  padding: 2px 10px;
  font-weight: 600;
}
.tag.draft { background: #f0f2f1; color: var(--ink-3); }
.tag.pending { background: #fff7e0; color: #c78a00; }
.tag.ok { background: rgba(0, 199, 95, 0.12); color: var(--brand-dark); }
.tag.rejected { background: #fdeaea; color: #d03050; }
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.dlg-body {
  max-height: 60vh;
  overflow-y: auto;
}
.dlg-row {
  margin: 8px 0;
  font-size: 14px;
}
.rich {
  margin-top: 12px;
  font-size: 14px;
  line-height: 1.9;
  color: var(--ink-2);
}
.rich :deep(h1), .rich :deep(h2), .rich :deep(h3) {
  color: var(--ink);
}
</style>
