<template>
  <div class="profile-page">
    <div class="card">
      <h1 class="page-title">个人信息管理</h1>

      <div class="form-area" v-loading="loading">
        <div class="avatar-col">
          <div class="avatar-ring">
            <img v-if="form.avatar" :src="form.avatar" alt="头像" class="avatar-img" />
            <el-icon v-else :size="52" color="#c4cdc8"><UserFilled /></el-icon>
          </div>
          <el-upload :show-file-list="false" :auto-upload="false" accept="image/jpeg,image/png" @change="onAvatarPick">
            <button class="btn-outline">{{ avatarUploading ? '上传中…' : '上传头像' }}</button>
          </el-upload>
        </div>

        <el-form label-position="top" class="info-form">
          <div class="grid-2">
            <el-form-item label="昵称：">
              <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="30" />
            </el-form-item>
            <el-form-item label="手机号：">
              <el-input v-model="form.phone" disabled />
            </el-form-item>
          </div>
          <div class="grid-2">
            <el-form-item label="注册时间：">
              <el-input :model-value="fmtDate(form.createdAt)" disabled />
            </el-form-item>
            <el-form-item label="身份角色：">
              <el-input model-value="创作者" disabled />
            </el-form-item>
          </div>
          <h3 class="block-title">创作者认证信息</h3>
          <div class="grid-2">
            <el-form-item label="职业：">
              <el-input v-model="form.occupation" placeholder="如：UI 设计师" maxlength="30" />
            </el-form-item>
            <el-form-item label="工作年限：">
              <el-select v-model="form.workYears" placeholder="请选择" style="width: 100%" clearable>
                <el-option v-for="y in ['应届/在校', '1-3 年', '3-5 年', '5-10 年', '10 年以上']" :key="y" :label="y" :value="y" />
              </el-select>
            </el-form-item>
          </div>
          <div class="grid-2">
            <el-form-item label="工作状态：">
              <el-select v-model="form.workStatus" placeholder="请选择" style="width: 100%" clearable>
                <el-option v-for="s in ['在职', '自由职业', '学生', '待业']" :key="s" :label="s" :value="s" />
              </el-select>
            </el-form-item>
            <el-form-item label="身份证号：">
              <el-input v-model="form.idCard" placeholder="用于实名认证" maxlength="18" />
            </el-form-item>
          </div>
          <el-form-item label="工作证明（PDF/JPG/PNG/DOCX，≤10MB）：">
            <div class="proof-row">
              <el-upload :show-file-list="false" :auto-upload="false" accept=".pdf,.jpg,.jpeg,.png,.docx" @change="onProofPick">
                <button class="btn-outline" type="button">{{ proofUploading ? '上传中…' : form.workProof ? '重新上传' : '上传工作证明' }}</button>
              </el-upload>
              <a v-if="form.workProof" :href="form.workProof" target="_blank" class="proof-link">查看已上传证明</a>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <div class="foot">
        <button class="btn-plain" @click="load">取消</button>
        <button class="btn-primary" :disabled="saving" @click="save">保存</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import type { UploadFile } from 'element-plus'
import { getProfileApi, updateProfileApi, uploadFileApi, type Profile } from '@/api/user'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const avatarUploading = ref(false)
const proofUploading = ref(false)
const form = reactive<Partial<Profile>>({})

async function onProofPick(file: UploadFile) {
  const raw = file.raw
  if (!raw) return
  if (raw.size > 10 * 1024 * 1024) {
    ElMessage.warning('文件不能超过 10MB')
    return
  }
  proofUploading.value = true
  try {
    const res = await uploadFileApi(raw)
    if (res.data) {
      form.workProof = res.data.url
      ElMessage.success('工作证明已上传，点保存生效')
    }
  } finally {
    proofUploading.value = false
  }
}

async function onAvatarPick(file: UploadFile) {
  const raw = file.raw
  if (!raw) return
  if (raw.size > 10 * 1024 * 1024) {
    ElMessage.warning('头像不能超过 10MB')
    return
  }
  avatarUploading.value = true
  try {
    const res = await uploadFileApi(raw)
    if (res.data) {
      form.avatar = res.data.url
      await updateProfileApi({ avatar: form.avatar })
      ElMessage.success('头像已更新')
    }
  } finally {
    avatarUploading.value = false
  }
}

async function load() {
  if (!userStore.token) {
    ElMessage.warning('请先在读者端登录')
    return
  }
  loading.value = true
  try {
    const res = await getProfileApi()
    if (res.data) {
      Object.assign(form, res.data)
    }
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    await updateProfileApi({
      nickname: form.nickname || '',
      occupation: form.occupation || '',
      workYears: form.workYears || '',
      workStatus: form.workStatus || '',
      idCard: form.idCard || '',
      workProof: form.workProof || ''
    })
    userStore.setProfile(form.nickname || '', userStore.roles)
    ElMessage.success('已保存')
  } finally {
    saving.value = false
  }
}

function fmtDate(d?: string) {
  return d ? String(d).slice(0, 10) : '—'
}

onMounted(load)
</script>

<style scoped>
.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(31, 43, 37, 0.06);
  padding: 28px 32px;
}
.page-title {
  margin: 0 0 26px;
  font-size: 22px;
}
.form-area {
  display: flex;
  gap: 48px;
}
.avatar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}
.avatar-ring {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: #f2f5f3;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.btn-outline {
  height: 32px;
  padding: 0 20px;
  border-radius: 6px;
  border: 1px solid var(--brand);
  background: #fff;
  color: var(--brand);
  font-size: 13px;
}
.btn-outline:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.info-form {
  flex: 1;
  min-width: 0;
}
.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 32px;
}
.block-title {
  margin: 8px 0 18px;
  font-size: 16px;
  padding-left: 10px;
  border-left: 4px solid var(--brand);
  line-height: 1.3;
}
.proof-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.proof-link {
  font-size: 13px;
  color: var(--brand);
}
.proof-link:hover {
  text-decoration: underline;
}
.foot {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid rgba(31, 43, 37, 0.08);
}
.btn-plain {
  height: 36px;
  padding: 0 26px;
  border-radius: 6px;
  border: 1px solid rgba(31, 43, 37, 0.15);
  background: #fff;
  color: var(--ink-2);
  font-size: 13px;
}
.btn-primary {
  height: 36px;
  padding: 0 34px;
  border-radius: 6px;
  background: var(--brand);
  color: #fff;
  font-size: 13px;
}
.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
