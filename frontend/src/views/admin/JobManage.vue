<template>
  <div class="page">
    <div class="page-head">
      <h1 class="page-title">岗位管理</h1>
      <button class="btn-primary" @click="openCreate">+ 创建岗位</button>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="jobName" label="岗位名称" width="160" />
        <el-table-column prop="jobDuty" label="岗位职责" min-width="220" show-overflow-tooltip />
        <el-table-column prop="jobRequire" label="任职要求" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <span class="tag" :class="row.status === 1 ? 'ok' : 'off'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建/编辑侧边弹窗 -->
    <el-drawer v-model="drawer" :title="editingId ? '编辑岗位' : '创建岗位'" size="520px">
      <el-form label-position="top">
        <el-form-item label="岗位名称" required>
          <el-input v-model="form.jobName" placeholder="请输入岗位名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="岗位职责">
          <el-input v-model="form.jobDuty" type="textarea" :rows="3" placeholder="请输入岗位职责" />
        </el-form-item>
        <el-form-item label="任职要求">
          <el-input v-model="form.jobRequire" type="textarea" :rows="3" placeholder="请输入任职要求" />
        </el-form-item>
        <el-form-item label="核心能力模型">
          <el-input v-model="form.abilityModel" type="textarea" :rows="2" placeholder="请输入核心能力模型" />
        </el-form-item>
        <el-form-item label="典型工作场景">
          <el-input v-model="form.workScene" type="textarea" :rows="2" placeholder="请输入典型工作场景" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-foot">
          <el-button @click="drawer = false">取消</el-button>
          <el-button type="primary" :disabled="saving" @click="save">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { jobListApi, jobCreateApi, jobUpdateApi, jobDeleteApi, type JobRow } from '@/api/admin'

const rows = ref<JobRow[]>([])
const loading = ref(false)
const saving = ref(false)
const drawer = ref(false)
const editingId = ref<number | null>(null)

const form = reactive<Partial<JobRow>>({ status: 1 })

async function load() {
  loading.value = true
  try {
    const res = await jobListApi(true)
    rows.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    jobName: '', jobDuty: '', jobRequire: '', abilityModel: '', workScene: '', status: 1
  })
  drawer.value = true
}

function openEdit(row: JobRow) {
  editingId.value = row.id
  Object.assign(form, row)
  drawer.value = true
}

async function save() {
  if (!form.jobName?.trim()) {
    ElMessage.warning('请填写岗位名称')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await jobUpdateApi(editingId.value, form)
    } else {
      await jobCreateApi(form)
    }
    ElMessage.success('已保存')
    drawer.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onDelete(row: JobRow) {
  await ElMessageBox.confirm(`确定删除岗位「${row.jobName}」吗？`, '删除确认', { type: 'warning' })
  await jobDeleteApi(row.id)
  ElMessage.success('已删除')
  load()
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
.btn-primary {
  height: 38px;
  padding: 0 20px;
  border-radius: 8px;
  background: var(--brand);
  color: #fff;
  font-size: 14px;
}
.btn-primary:hover {
  background: var(--brand-deep);
}
.tag {
  font-size: 11px;
  border-radius: 4px;
  padding: 2px 10px;
  font-weight: 600;
}
.tag.ok { background: rgba(0, 199, 95, 0.12); color: var(--brand-dark); }
.tag.off { background: #f0f2f1; color: var(--ink-3); }
.drawer-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
