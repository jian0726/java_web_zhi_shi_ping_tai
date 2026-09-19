import request from '@/utils/request'

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

/** 知识库卡片（分类页/推荐/我的订阅通用） */
export interface KnowledgeCard {
  id: number
  title: string
  kbType?: string
  summary?: string
  coverUrl?: string | null
  publishedAt?: string
  subscribeCount?: number
  authorName: string
  articleCount?: number
}

/** 模块-文章树节点 */
export interface ChapterNode {
  moduleId: number
  moduleName: string
  articles: { id: number; title: string; moduleId: number }[]
}

export interface KnowledgeDetail {
  id: number
  kbNo: string
  title: string
  kbType?: string
  summary?: string
  intro?: string
  displayType?: string
  coverUrl?: string | null
  authorName: string
  publishedAt?: string
  subscribeCount?: number
  chapters: ChapterNode[]
  subscribed: boolean
}

export interface ArticleDetail {
  id: number
  baseId: number
  moduleId: number
  title: string
  summary?: string
  content?: string
  authorName: string
  publishedAt?: string
}

/** 知识库分类列表（含数量） */
export function getCategoriesApi() {
  return request.get<any, ApiResult<{ type: string; count: number }[]>>('/api/kb/categories')
}

/** 知识库分页（sort: new|hot） */
export function getKbPageApi(params: { type?: string; sort?: string; page: number; size: number }) {
  return request.get<any, ApiResult<{ total: number; list: KnowledgeCard[] }>>('/api/kb/page', { params })
}

/** 知识库详情（含模块-文章树 + subscribed） */
export function getKbDetailApi(baseId: number) {
  return request.get<any, ApiResult<KnowledgeDetail>>(`/api/kb/${baseId}`)
}

/** 文章详情（阅读页正文） */
export function getArticleApi(articleId: number) {
  return request.get<any, ApiResult<ArticleDetail>>(`/api/kb/article/${articleId}`)
}

/** 指定知识库的模块-文章树 */
export function getKbChaptersApi(baseId: number) {
  return request.get<any, ApiResult<ChapterNode[]>>(`/api/kb/${baseId}/chapters`)
}

/** 推荐知识库 */
export function getKbRecommendApi(params: { excludeId?: number; kbType?: string; limit?: number }) {
  return request.get<any, ApiResult<KnowledgeCard[]>>('/api/kb/recommend', { params })
}

/** 订阅 / 取消订阅知识库 */
export function subscribeApi(baseId: number) {
  return request.post<any, ApiResult<null>>(`/api/kb/${baseId}/subscribe`)
}
export function unsubscribeApi(baseId: number) {
  return request.delete<any, ApiResult<null>>(`/api/kb/${baseId}/subscribe`)
}

/** 我的订阅分页 */
export function getMySubscriptionsApi(params: { page: number; size: number }) {
  return request.get<any, ApiResult<{ total: number; list: KnowledgeCard[] }>>('/api/kb/my/subscriptions', { params })
}
