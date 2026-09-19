<template>
  <div class="page">
    <h1 class="page-title">创作者信息列表</h1>
    <div class="card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="nickname" label="昵称" width="160" />
        <el-table-column prop="phone" label="手机号" width="160" />
        <el-table-column prop="reason" label="申请理由" min-width="220" show-overflow-tooltip />
        <el-table-column prop="qualification" label="资质说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="通过时间" width="130">
          <template #default="{ row }">{{ fmtDate(row.auditedAt) }}</template>
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { creatorsApi, type CreatorRow } from '@/api/admin'

const rows = ref<CreatorRow[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await creatorsApi({ page: page.value, size })
    rows.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}
function onPage(p: number) {
  page.value = p
  load()
}
function fmtDate(d?: string) {
  return d ? String(d).slice(0, 10) : '—'
}
onMounted(load)
</script>

<style scoped>
.page-title {
  margin: 0 0 18px;
  font-size: 22px;
}
.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(31, 43, 37, 0.06);
  padding: 20px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
