import { createRouter, createWebHistory } from 'vue-router'

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

export default router
