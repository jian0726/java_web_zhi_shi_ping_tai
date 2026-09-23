<template>
  <el-dialog
    v-model="visible"
    :title="mode === 'register' ? '注册' : '登录'"
    width="400px"
    align-center
    :close-on-click-modal="false"
  >
    <!-- 登录方式切换（仅登录态显示） -->
    <el-tabs v-if="mode === 'login'" v-model="loginType" class="auth-tabs">
      <el-tab-pane label="密码登录" name="password" />
      <el-tab-pane label="验证码登录" name="sms" />
      <el-tab-pane label="扫码登录" name="qrcode" />
    </el-tabs>

    <!-- 扫码登录 -->
    <div v-if="mode === 'login' && loginType === 'qrcode'" class="qr-panel">
      <div class="qr-box">
        <el-icon :size="64" color="#c4cdc8"><Iphone /></el-icon>
      </div>
      <p class="qr-tip">请使用微信扫描二维码登录</p>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="扫码登录需接入微信开放平台（网站应用资质），当前为界面占位，可先用验证码登录"
      />
    </div>

    <!-- 密码登录 / 注册 -->
    <el-form v-else :model="form" label-width="0" @submit.prevent>
      <el-form-item>
        <el-input v-model="form.phone" placeholder="手机号" maxlength="11" />
      </el-form-item>

      <!-- 验证码登录 -->
      <template v-if="mode === 'login' && loginType === 'sms'">
        <el-form-item>
          <div class="sms-row">
            <el-input v-model="form.code" placeholder="验证码" maxlength="6" @keyup.enter="submit" />
            <el-button :disabled="countdown > 0" @click="sendCode">
              {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
            </el-button>
          </div>
          <p v-if="devCode" class="dev-code">开发环境验证码：{{ devCode }}（5 分钟内有效）</p>
        </el-form-item>
      </template>

      <!-- 密码登录 / 注册 -->
      <template v-else>
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="密码（至少 6 位）"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-form-item v-if="mode === 'register'">
          <el-input v-model="form.nickname" placeholder="昵称（选填）" />
        </el-form-item>
      </template>

      <el-button type="primary" class="submit-btn" :loading="loading" @click="submit">
        {{ mode === 'login' ? '登录' : '注册' }}
      </el-button>
    </el-form>

    <p class="switch-tip">
      <template v-if="mode === 'login'">还没有账号？<a @click="mode = 'register'">立即注册</a></template>
      <template v-else>已有账号？<a @click="mode = 'login'">直接登录</a></template>
    </p>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Iphone } from '@element-plus/icons-vue'
import { loginApi, registerApi, sendSmsApi, loginBySmsApi } from '@/api/user'
import { useUserStore } from '@/store/user'

const visible = ref(false)
const mode = ref<'login' | 'register'>('login')
const loginType = ref<'password' | 'sms' | 'qrcode'>('password')
const loading = ref(false)
const form = reactive({ phone: '', password: '', nickname: '', code: '' })
const countdown = ref(0)
const devCode = ref('')
const userStore = useUserStore()

function open(m: 'login' | 'register' = 'login') {
  mode.value = m
  visible.value = true
}
defineExpose({ open })

async function sendCode() {
  if (!/^1\d{10}$/.test(form.phone)) return ElMessage.warning('请输入正确的手机号')
  const res = await sendSmsApi(form.phone)
  if (res.code === 200 && res.data) {
    devCode.value = res.data.code
    ElMessage.success('验证码已生成')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  }
}

function onLoginSuccess(token: string, nickname: string, roleCodes?: string[]) {
  userStore.setToken(token)
  userStore.setProfile(nickname, roleCodes?.length ? roleCodes : ['READER'])
  visible.value = false
  ElMessage.success(`欢迎回来，${nickname}`)
}

async function submit() {
  if (!/^1\d{10}$/.test(form.phone)) return ElMessage.warning('请输入正确的手机号')
  loading.value = true
  try {
    // 注册
    if (mode.value === 'register') {
      if (form.password.length < 6) {
        ElMessage.warning('密码至少 6 位')
        return
      }
      const res = await registerApi({ ...form })
      if (res.code !== 200) return ElMessage.error(res.message)
      ElMessage.success('注册成功，已自动登录')
      const loginRes = await loginApi({ phone: form.phone, password: form.password })
      if (loginRes.code === 200 && loginRes.data) {
        onLoginSuccess(loginRes.data.token, loginRes.data.nickname, loginRes.data.roleCodes)
      }
      return
    }
    // 验证码登录
    if (loginType.value === 'sms') {
      if (!form.code.trim()) return ElMessage.warning('请输入验证码')
      const res = await loginBySmsApi({ phone: form.phone, code: form.code })
      if (res.code !== 200) return ElMessage.error(res.message)
      if (res.data) onLoginSuccess(res.data.token, res.data.nickname, res.data.roleCodes)
      return
    }
    // 密码登录
    if (form.password.length < 6) return ElMessage.warning('密码至少 6 位')
    const res = await loginApi({ phone: form.phone, password: form.password })
    if (res.code !== 200) return ElMessage.error(res.message)
    if (res.data) onLoginSuccess(res.data.token, res.data.nickname, res.data.roleCodes)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.submit-btn {
  width: 100%;
}
.switch-tip {
  text-align: center;
  font-size: 13px;
  color: #888;
  margin: 4px 0 0;
}
.switch-tip a {
  color: var(--el-color-primary);
  cursor: pointer;
}
.sms-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.dev-code {
  width: 100%;
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--brand-dark);
}
.qr-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  padding: 10px 0 6px;
}
.qr-box {
  width: 180px;
  height: 180px;
  border-radius: 12px;
  background: #f2f5f3;
  display: flex;
  align-items: center;
  justify-content: center;
}
.qr-tip {
  margin: 0;
  font-size: 13px;
  color: var(--ink-2);
}
</style>
