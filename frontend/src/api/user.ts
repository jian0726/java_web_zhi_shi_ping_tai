import request from '@/utils/request'

/** 统一响应体包装 */
interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface LoginResult {
  token: string
  nickname: string
  phone: string
}

export function registerApi(data: { phone: string; password: string; nickname?: string }) {
  return request.post<any, ApiResult<null>>('/api/user/register', data)
}

export function loginApi(data: { phone: string; password: string }) {
  return request.post<any, ApiResult<LoginResult>>('/api/user/login', data)
}

export interface Profile {
  id: number
  phone: string
  nickname: string
  avatar?: string | null
  createdAt?: string
  /** 创作者认证信息 */
  occupation?: string | null
  workYears?: string | null
  workStatus?: string | null
  idCard?: string | null
  workProof?: string | null
}

export function getProfileApi() {
  return request.get<any, ApiResult<Profile>>('/api/user/profile')
}

export function updateProfileApi(data: {
  nickname?: string
  avatar?: string
  occupation?: string
  workYears?: string
  workStatus?: string
  idCard?: string
  workProof?: string
}) {
  return request.put<any, ApiResult<null>>('/api/user/profile', data)
}

/** 发送短信验证码（开发模式直接回显验证码） */
export function sendSmsApi(phone: string) {
  return request.post<any, ApiResult<{ code: string; tip: string }>>('/api/user/sms/send', { phone })
}

/** 验证码登录（未注册自动注册） */
export function loginBySmsApi(data: { phone: string; code: string }) {
  return request.post<any, ApiResult<LoginResult>>('/api/user/login/sms', data)
}

/** 上传文件（file 服务，≤10MB，PDF/JPG/PNG/DOCX），返回可访问 url */
export function uploadFileApi(file: File) {
  const fd = new FormData()
  fd.append('file', file)
  return request.post<any, ApiResult<{ url: string; name: string; size: string }>>(
    '/api/file/upload',
    fd,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )
}
