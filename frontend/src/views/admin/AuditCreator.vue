<template>
  <div class="page">
    <div class="page-head">
      <h1 class="page-title">审核创作者</h1>
      <el-radio-group v-model="statusFilter" @change="reload">
        <el-radio-button :value="0">待审核</el-radio-button>
        <el-radio-button :value="1">已通过</el-radio-button>
        <el-radio-button :value="2">已驳回</el-radio-button>
      </el-radio-group>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="reason" label="申请理由" min-width="200" show-overflow-tooltip />
        <el-table-column prop="qualification" label="资质说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="申请时间" width="120">
          <template #default="{ row }">{{ fmtDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <span class="tag" :class="['pending', 'ok', 'rejected'][row.status]">
              {{ ['待审核', '已通过', '已驳回'][row.status] }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="primary" @click="openDetail(row)">详情</el-button>
              <el-button size="small" @click="doAudit(row, 2)">驳回</el-button>
            </template>
            <span v-else class="muted">{{ fmtDate(row.auditedAt) }} 处理</span>
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
    <el-dialog v-model="detailBox" title="申请详情" width="520px">
      <template v-if="current">
        <p class="dlg-row"><b>昵称：</b>{{ current.nickname }}</p>
        <p class="dlg-row"><b>手机号：</b>{{ current.phone }}</p>
        <p class="dlg-row"><b>申请理由：</b>{{ current.reason || '—' }}</p>
        <p class="dlg-row"><b>资质说明：</b>{{ current.qualification || '—' }}</p>
        <p class="dlg-row"><b>申请时间：</b>{{ fmtDate(current.createdAt) }}</p>
      </template>
      <template #footer>
        <el-button @click="detailBox = false">取消</el-button>
        <el-button type="danger" @click="doAudit(current!, 2)">不通过</el-button>
        <el-button type="primary" @click="doAudit(current!, 1)">通过</el-button>
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
import { appliesApi, auditApplyApi, type ApplyRow } from '@/api/admin'

const rows = ref<ApplyRow[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const statusFilter = ref(0)
const loading = ref(false)

const detailBox = ref(false)
const rejectBox = ref(false)
const rejectRemark = ref('')
const current = ref<ApplyRow | null>(null)

async function load() {
  loading.value = true
  try {
    const res = await appliesApi({ status: statusFilter.value, page: page.value, size })
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

function openDetail(row: ApplyRow) {
  current.value = row
  detailBox.value = true
}

function doAudit(row: ApplyRow, action: 1 | 2) {
  current.value = row
  detailBox.value = false
  if (action === 2) {
    rejectRemark.value = ''
    rejectBox.value = true
    return
  }
  auditApplyApi(row.id, { action: 1 }).then(() => {
    ElMessage.success('已通过')
    load()
  })
}

async function confirmReject() {
  if (!current.value) return
  await auditApplyApi(current.value.id, { action: 2, remark: rejectRemark.value.trim() })
  ElMessage.success('已驳回')
  rejectBox.value = false
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
.tag.pending { background: #fff7e0; color: #c78a00; }
.tag.ok { background: rgba(0, 199, 95, 0.12); color: var(--brand-dark); }
.tag.rejected { background: #fdeaea; color: #d03050; }
.muted { font-size: 12px; color: var(--ink-3); }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
.dlg-row { margin: 8px 0; font-size: 14px; line-height: 1.7; }
</style>
