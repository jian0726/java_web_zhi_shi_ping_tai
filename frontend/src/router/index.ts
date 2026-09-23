import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/ReaderLayout.vue'),
    children: [
      { path: '', component: () => import('@/views/reader/Home.vue') },
      { path: 'category', component: () => import('@/views/reader/Category.vue') },
      { path: 'search', component: () => import('@/views/reader/SearchResult.vue') },
      { path: 'article/:id', component: () => import('@/views/reader/ArticleDetail.vue') },
      { path: 'read/:id', component: () => import('@/views/reader/ReadingPage.vue') },
      { path: 'job/:id', component: () => import('@/views/reader/JobDetail.vue') },
      { path: 'profile', component: () => import('@/views/reader/Profile.vue') }
    ]
  },
  {
    path: '/creator',
    component: () => import('@/layouts/CreatorLayout.vue'),
    children: [
      { path: '', component: () => import('@/views/creator/KnowledgeList.vue') },
      { path: 'knowledge/:id', component: () => import('@/views/creator/KnowledgeDetail.vue') },
      { path: 'profile', component: () => import('@/views/creator/Profile.vue') }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      { path: '', redirect: '/admin/audit-creator' },
      { path: 'audit-creator', component: () => import('@/views/admin/AuditCreator.vue') },
      { path: 'creators', component: () => import('@/views/admin/CreatorList.vue') },
      { path: 'audit-knowledge', component: () => import('@/views/admin/KnowledgeAudit.vue') },
      { path: 'jobs', component: () => import('@/views/admin/JobManage.vue') },
      { path: 'users', component: () => import('@/views/admin/UserManage.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/** 游客（未登录）可浏览的页面：首页、分类文章、搜索、文章详情/阅读 */
const GUEST_ALLOWED = /^\/$|^\/(category|search)|^\/(article|read)\/\d+$/

router.beforeEach((to) => {
  if (GUEST_ALLOWED.test(to.path)) return true

  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return { path: '/', query: { needLogin: '1' } }
  }

  let roles: string[] = []
  try {
    roles = JSON.parse(localStorage.getItem('roles') || '[]')
  } catch {
    roles = []
  }

  if (to.path.startsWith('/creator') && !roles.includes('CREATOR')) {
    ElMessage.warning('该区域需要创作者身份，请先申请并通过审核')
    return '/'
  }

  if (to.path.startsWith('/admin')) {
    const isAdmin = roles.includes('ADMIN')
    const isAuditor = roles.includes('AUDITOR')
    const auditPage = to.path.startsWith('/admin/audit-')
    if (!isAdmin && !(isAuditor && auditPage)) {
      ElMessage.warning('该区域需要平台管理员权限')
      return '/'
    }
  }

  return true
})

export default router
