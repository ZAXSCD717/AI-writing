import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView
  },
  {
    path: '/novel/input',
    name: 'NovelInput',
    component: () => import('../views/NovelInputView.vue')
  },
  {
    path: '/scripts',
    name: 'ScriptList',
    component: () => import('../views/ScriptListView.vue')
  },
  {
    path: '/novels/:id/chapters',
    name: 'ChapterSelect',
    component: () => import('../views/ChapterSelectView.vue'),
    props: true
  },
  {
    path: '/scripts/:id',
    name: 'ScriptDetail',
    component: () => import('../views/ScriptDetailView.vue'),
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
