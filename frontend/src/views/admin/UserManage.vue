<template>
  <div class="page">
    <h1 class="page-title">用户管理</h1>
    <div class="card">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索手机号 / 昵称"
          clearable
          style="width: 260px"
          @keyup.enter="reload"
          @clear="reload"
        />
        <el-button type="primary" @click="reload">搜索</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="nickname" label="昵称" width="160" />
        <el-table-column label="角色" min-width="160">
          <template #default="{ row }">
            <el-tag v-for="r in row.roleNames" :key="r" size="small" style="margin-right: 6px">{{ r }}</el-tag>
            <span v-if="!row.roleNames?.length" class="muted">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <span class="tag" :class="row.status === 1 ? 'ok' : 'off'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="120">
          <template #default="{ row }">{{ fmtDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openRoles(row)">分配角色</el-button>
            <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'" @click="toggle(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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

    <!-- 分配角色弹窗 -->
    <el-dialog v-model="roleBox" title="分配角色" width="420px">
      <el-checkbox-group v-model="pickedRoles">
        <el-checkbox v-for="r in allRoles" :key="r.id" :value="r.id">{{ r.roleName }}（{{ r.roleCode }}）</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleBox = false">取消</el-button>
        <el-button type="primary" @click="saveRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  usersApi,
  userStatusApi,
  rolesApi,
  assignRolesApi,
  type UserRow
} from '@/api/admin'

const rows = ref<UserRow[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const loading = ref(false)

const roleBox = ref(false)
const allRoles = ref<{ id: number; roleCode: string; roleName: string }[]>([])
const pickedRoles = ref<number[]>([])
const roleTarget = ref<UserRow | null>(null)

async function load() {
  loading.value = true
  try {
    const res = await usersApi({ keyword: keyword.value || undefined, page: page.value, size })
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

async function openRoles(row: UserRow) {
  roleTarget.value = row
  pickedRoles.value = [...(row.roleIds || [])]
  if (allRoles.value.length === 0) {
    const res = await rolesApi()
    allRoles.value = res.data || []
  }
  roleBox.value = true
}

async function saveRoles() {
  if (!roleTarget.value) return
  await assignRolesApi(roleTarget.value.id, pickedRoles.value)
  ElMessage.success('已保存')
  roleBox.value = false
  load()
}

async function toggle(row: UserRow) {
  const next: 0 | 1 = row.status === 1 ? 0 : 1
  await userStatusApi(row.id, next)
  ElMessage.success(next === 1 ? '已启用' : '已禁用')
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
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
.tag {
  font-size: 11px;
  border-radius: 4px;
  padding: 2px 10px;
  font-weight: 600;
}
.tag.ok { background: rgba(0, 199, 95, 0.12); color: var(--brand-dark); }
.tag.off { background: #f0f2f1; color: var(--ink-3); }
.muted { font-size: 12px; color: var(--ink-3); }
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
