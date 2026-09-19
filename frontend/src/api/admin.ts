import request from '@/utils/request'

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface ApplyRow {
  id: number
  userId: number
  reason: string
  qualification: string
  status: number
  auditRemark?: string
  auditedAt?: string
  createdAt: string
  nickname: string
  phone: string
}

export interface CreatorRow {
  applyId: number
  userId: number
  nickname: string
  phone: string
  reason: string
  qualification: string
  auditedAt: string
}

/** 创作者申请分页（status: 0待审 1通过 2驳回） */
export function appliesApi(params: { status?: number; page: number; size: number }) {
  return request.get<any, ApiResult<{ total: number; list: ApplyRow[] }>>('/api/creator/admin/applies', { params })
}

export function auditApplyApi(id: number, data: { action: 1 | 2; remark?: string }) {
  return request.put<any, ApiResult<null>>(`/api/creator/admin/apply/${id}/audit`, data)
}

export function creatorsApi(params: { page: number; size: number }) {
  return request.get<any, ApiResult<{ total: number; list: CreatorRow[] }>>('/api/creator/admin/creators', { params })
}

// ---------- 岗位管理 ----------

export interface JobRow {
  id: number
  jobName: string
  jobDuty?: string
  jobRequire?: string
  abilityModel?: string
  workScene?: string
  status: number
  createdAt?: string
}

export function jobListApi(all = true) {
  return request.get<any, ApiResult<JobRow[]>>('/api/job/list', { params: { all } })
}

export function jobCreateApi(data: Partial<JobRow>) {
  return request.post<any, ApiResult<number>>('/api/job', data)
}

export function jobUpdateApi(id: number, data: Partial<JobRow>) {
  return request.put<any, ApiResult<null>>(`/api/job/${id}`, data)
}

export function jobDeleteApi(id: number) {
  return request.delete<any, ApiResult<null>>(`/api/job/${id}`)
}

// ---------- 用户管理 ----------

export interface UserRow {
  id: number
  phone: string
  nickname: string
  status: number
  createdAt: string
  roleIds: number[]
  roleNames: string[]
}

export function usersApi(params: { keyword?: string; page: number; size: number }) {
  return request.get<any, ApiResult<{ total: number; list: UserRow[] }>>('/api/user/admin/users', { params })
}

export function userStatusApi(id: number, status: 0 | 1) {
  return request.put<any, ApiResult<null>>(`/api/user/admin/user/${id}/status`, { status })
}

export function rolesApi() {
  return request.get<any, ApiResult<{ id: number; roleCode: string; roleName: string }[]>>('/api/user/admin/roles')
}

export function assignRolesApi(id: number, roleIds: number[]) {
  return request.put<any, ApiResult<null>>(`/api/user/admin/user/${id}/roles`, { roleIds })
}

// ---------- 审核者端：知识审核 ----------

export interface AuditRow {
  id: number
  kbNo: string
  name: string
  kbType: string
  summary: string
  status: number
  currentVersion: number
  createdAt: string
  updatedAt: string
  authorName: string
  moduleCount?: number
  articleCount?: number
}

export function auditPendingApi(params: { status?: number; page: number; size: number }) {
  return request.get<any, ApiResult<{ total: number; list: AuditRow[] }>>('/api/audit/pending', { params })
}

export function auditDetailApi(id: number) {
  return request.get<any, ApiResult<Record<string, unknown>>>(`/api/audit/${id}`)
}

export function auditActionApi(id: number, data: { action: 1 | 2; remark?: string }) {
  return request.put<any, ApiResult<null>>(`/api/audit/${id}/action`, data)
}
