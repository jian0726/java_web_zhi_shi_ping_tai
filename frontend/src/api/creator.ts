import request from '@/utils/request'

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface BaseRow {
  id: number
  kbNo: string
  name: string
  kbType?: string
  summary?: string
  coverUrl?: string | null
  status: number
  currentVersion: number
  subscribeCount?: number
  publishedAt?: string
  createdAt?: string
  updatedAt?: string
  authorName: string
  moduleCount?: number
  articleCount?: number
}

export interface BaseDetail extends BaseRow {
  intro?: string
  displayType?: string
}

export interface BaseUpsertPayload {
  name: string
  kbType?: string
  summary?: string
  intro?: string
  displayType?: string
  coverUrl?: string
  submit?: boolean
}

export interface ModuleRow {
  id: number
  baseId: number
  moduleName: string
  sortOrder: number
}

export interface ArticleRow {
  id: number
  moduleId: number
  title: string
  summary?: string
  updatedAt?: string
}

// ---------- 知识库 ----------

/** 我的知识库分页（status: 0草稿 1待审 2已发布 3驳回，不传查全部） */
export function myKnowledgeApi(params: { status?: number; page: number; size: number }) {
  return request.get<any, ApiResult<{ total: number; list: BaseRow[] }>>('/api/creator/base/page', { params })
}

export function myBaseDetailApi(id: number) {
  return request.get<any, ApiResult<BaseDetail>>(`/api/creator/base/${id}`)
}

export function createBaseApi(data: BaseUpsertPayload) {
  return request.post<any, ApiResult<number>>('/api/creator/base', data)
}

export function updateBaseApi(id: number, data: BaseUpsertPayload) {
  return request.put<any, ApiResult<null>>(`/api/creator/base/${id}`, data)
}

export function deleteBaseApi(id: number) {
  return request.delete<any, ApiResult<null>>(`/api/creator/base/${id}`)
}

// ---------- 知识模块 ----------

export function moduleListApi(baseId: number) {
  return request.get<any, ApiResult<ModuleRow[]>>(`/api/creator/base/${baseId}/module/list`)
}

export function moduleCreateApi(baseId: number, moduleName: string) {
  return request.post<any, ApiResult<number>>(`/api/creator/base/${baseId}/module`, { moduleName })
}

export function moduleUpdateApi(baseId: number, id: number, data: { moduleName?: string; sortOrder?: number }) {
  return request.put<any, ApiResult<null>>(`/api/creator/base/${baseId}/module/${id}`, data)
}

export function moduleDeleteApi(baseId: number, id: number) {
  return request.delete<any, ApiResult<null>>(`/api/creator/base/${baseId}/module/${id}`)
}

// ---------- 模块下文章 ----------

export function articleListApi(baseId: number) {
  return request.get<any, ApiResult<ArticleRow[]>>(`/api/creator/base/${baseId}/articles`)
}

export function articleGetApi(baseId: number, articleId: number) {
  return request.get<any, ApiResult<ArticleRow & { content?: string }>>(`/api/creator/base/${baseId}/article/${articleId}`)
}

export function articleCreateApi(baseId: number, data: { moduleId: number; title: string; summary?: string; content?: string }) {
  return request.post<any, ApiResult<number>>(`/api/creator/base/${baseId}/article`, data)
}

export function articleUpdateApi(baseId: number, articleId: number, data: { moduleId?: number; title?: string; summary?: string; content?: string }) {
  return request.put<any, ApiResult<null>>(`/api/creator/base/${baseId}/article/${articleId}`, data)
}

export function articleDeleteApi(baseId: number, articleId: number) {
  return request.delete<any, ApiResult<null>>(`/api/creator/base/${baseId}/article/${articleId}`)
}

// ---------- 创作者申请 ----------

/** 提交创作者申请 */
export function applyCreatorApi(data: { reason: string; qualification: string }) {
  return request.post<any, ApiResult<number>>('/api/creator/apply', data)
}

/** 我的申请状态 */
export function myApplyApi() {
  return request.get<any, ApiResult<{ id: number; status: number; auditRemark: string; createdAt: string } | null>>(
    '/api/creator/apply/my'
  )
}

// ---------- 协作者 ----------

export interface CollaboratorRow {
  id: number
  userId: number
  role: string
  createdAt: string
  nickname: string
  phone: string
}

export function collaboratorsApi(baseId: number) {
  return request.get<any, ApiResult<CollaboratorRow[]>>(`/api/creator/base/${baseId}/collaborators`)
}

export function addCollaboratorApi(baseId: number, phone: string) {
  return request.post<any, ApiResult<number>>(`/api/creator/base/${baseId}/collaborators`, { phone })
}

export function removeCollaboratorApi(baseId: number, collaboratorId: number) {
  return request.delete<any, ApiResult<null>>(`/api/creator/base/${baseId}/collaborators/${collaboratorId}`)
}
