<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '../services/api'
import { formatDate, normalizeStats } from '../utils/task'
import type { AdminStat, Task } from '../types'
import AppPagination from '../components/AppPagination.vue'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import AppIcon from '../components/AppIcon.vue'

const tasks = ref<Task[]>([])
const stats = ref<AdminStat[]>([])
const page = ref(0)
const totalPages = ref(0)
const owner = ref('')
const error = ref('')
const toDelete = ref<Task | null>(null)

async function loadTasks() {
  try {
    const res = await api.adminTasks({ page: page.value, size: 10, owner: owner.value })
    tasks.value = res.content
    totalPages.value = res.totalPages
  } catch (e) {
    error.value = (e as Error).message
  }
}

async function loadStats() {
  try {
    const data = await api.adminStats()
    // accepte un tableau [{owner, completed, ...}] ou un objet { alice: {...}, bob: {...} }
    stats.value = Array.isArray(data)
      ? data.map((s: Record<string, any>) => ({
          owner: s.owner ?? s.username ?? s.ownerName ?? s.proprietaire ?? s.ownerId,
          ...normalizeStats(s)
        }))
      : Object.entries(data as Record<string, Record<string, any>>).map(([name, s]) => ({ owner: name, ...normalizeStats(s) }))
  } catch (e) {
    error.value = (e as Error).message
  }
}

function filter() { page.value = 0; loadTasks() }
function goTo(p: number) { page.value = p; loadTasks() }

async function confirmDelete() {
  try {
    await api.adminDelete(toDelete.value!.identifiant)
    toDelete.value = null
    if (tasks.value.length === 1 && page.value > 0) page.value--
    await Promise.all([loadTasks(), loadStats()])
  } catch (e) {
    error.value = (e as Error).message
    toDelete.value = null
  }
}

onMounted(() => { loadTasks(); loadStats() })
</script>

<template>
  <h1 class="page-title">
    <span class="title-icon"><AppIcon name="shield" :size="24" /></span>
    Administration
  </h1>
  <p v-if="error" class="error">{{ error }}</p>

  <h2 class="section-title"><AppIcon name="users" :size="22" /> Statistiques par utilisateur</h2>
  <div class="table-wrap">
    <table>
      <thead><tr><th>Utilisateur</th><th>Terminées</th><th>En cours</th><th>En retard</th></tr></thead>
      <tbody>
        <tr v-for="s in stats" :key="s.owner">
          <td>{{ s.owner }}</td><td>{{ s.completed }}</td><td>{{ s.inProgress }}</td><td>{{ s.overdue }}</td>
        </tr>
      </tbody>
    </table>
  </div>

  <h2 class="section-title"><AppIcon name="list" :size="22" /> Toutes les tâches</h2>
  <form class="card filters filters-admin" @submit.prevent="filter">
    <label class="field">
      <span class="field-label"><AppIcon name="search" :size="16" /> Utilisateur</span>
      <input v-model="owner" placeholder="Filtrer par utilisateur (ex : alice)" />
    </label>
    <button class="btn"><AppIcon name="filter" :size="16" /> Filtrer</button>
  </form>

  <div class="table-wrap">
    <table>
      <thead><tr><th>Titre</th><th>Propriétaire</th><th>Échéance</th><th>Statut</th><th></th></tr></thead>
      <tbody>
        <tr v-for="t in tasks" :key="t.identifiant">
          <td>{{ t.titre }}</td>
          <td>{{ t.proprietaire }}</td>
          <td>{{ formatDate(t.dateEcheance) }}</td>
          <td>{{ t.estTermine ? 'Terminée' : 'En cours' }}</td>
          <td>
            <button class="btn btn-danger" @click="toDelete = t">
              <AppIcon name="trash" :size="16" /> Supprimer
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <AppPagination :page="page" :total-pages="totalPages" @change="goTo" />

  <ConfirmDialog
    v-if="toDelete"
    :message="`Supprimer la tâche « ${toDelete.titre} » de ${toDelete.proprietaire} ?`"
    @confirm="confirmDelete"
    @cancel="toDelete = null"
  />
</template>
