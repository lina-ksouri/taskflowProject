import { createRouter, createWebHistory } from 'vue-router'
import { isAdmin } from '../services/keycloak'
import Dashboard from '../views/DashboardView.vue'
import Tasks from '../views/TasksView.vue'
import TaskForm from '../views/TaskFormView.vue'
import Profile from '../views/ProfileView.vue'
import Admin from '../views/AdminView.vue'
import Forbidden from '../views/ForbiddenView.vue'

const routes = [
  { path: '/', name: 'dashboard', component: Dashboard },
  { path: '/tasks', name: 'tasks', component: Tasks },
  { path: '/tasks/new', name: 'task-new', component: TaskForm },
  { path: '/tasks/:id/edit', name: 'task-edit', component: TaskForm, props: true },
  { path: '/profile', name: 'profile', component: Profile },
  { path: '/admin', name: 'admin', component: Admin, meta: { admin: true } },
  { path: '/forbidden', name: 'forbidden', component: Forbidden },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({ history: createWebHistory(), routes })

// Protège la page Administration, même par URL directe
router.beforeEach((to) => {
  if (to.meta.admin && !isAdmin()) return { name: 'forbidden' }
})

export default router
