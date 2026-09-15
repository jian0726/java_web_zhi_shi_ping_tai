import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/ReaderLayout.vue'),
    children: [{ path: '', component: () => import('@/views/reader/Home.vue') }]
  },
  {
    path: '/creator',
    component: () => import('@/layouts/CreatorLayout.vue'),
    children: [{ path: '', component: () => import('@/views/creator/Home.vue') }]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [{ path: '', component: () => import('@/views/admin/Home.vue') }]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
