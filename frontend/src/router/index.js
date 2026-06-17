import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
  },
  {
    path: '/post/:id',
    name: 'PostDetail',
    component: () => import('@/views/PostDetail.vue'),
  },
  {
    path: '/publish',
    name: 'Publish',
    component: () => import('@/views/Publish.vue'),
  },
  {
    path: '/care-logs',
    name: 'CareLogs',
    component: () => import('@/views/CareLogs.vue'),
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
  },
  {
    path: '/bonsais',
    name: 'BonsaiList',
    component: () => import('@/views/BonsaiList.vue'),
  },
  {
    path: '/bonsais/create',
    name: 'BonsaiCreate',
    component: () => import('@/views/BonsaiCreate.vue'),
  },
  {
    path: '/bonsais/:id/edit',
    name: 'BonsaiEdit',
    component: () => import('@/views/BonsaiCreate.vue'),
  },
  {
    path: '/bonsais/:id/events/create',
    name: 'EventCreate',
    component: () => import('@/views/EventCreate.vue'),
  },
  {
    path: '/bonsais/:id',
    name: 'BonsaiDetail',
    component: () => import('@/views/BonsaiDetail.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
