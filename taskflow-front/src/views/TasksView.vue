<script setup lang="ts">
import { reactive, ref, watch, onMounted } from 'vue'
import { api } from '../services/api'
import { formatDate, isOverdue, priorityLabel, PRIORITIES } from '../utils/task'
import type { Task } from '../types'
import AppPagination from '../components/AppPagination.vue'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import AppIcon from '../components/AppIcon.vue'

const tasks = ref<Task[]>([])
const totalPages = ref(0)
const error = ref('')
const toDelete = ref<Task | null>(null) // tâche en attente de confirmation
const busyId = ref<string | null>(null) // tâche dont le statut est en train de changer

const filters = reactive({
  page: 0,
  completed: '',   // '' = tous, 'true' = terminées, 'false' = en cours
  priority: '',
  sort: 'dateEcheance,asc' // "champ,sens" : on le découpe en sortBy + direction pour l'API
})

/** Paramètres envoyés à GET /api/tasks (mêmes noms que dans le Swagger du back). */
function buildQuery() {
  const [sortBy, direction] = filters.sort.split(',')
  return {
    page: filters.page,
    size: 8,
    completed: filters.completed,
    priority: filters.priority,
    sortBy,
    direction
  }
}

async function load() {
  error.value = ''
  try {
    const res = await api.tasks(buildQuery())
    tasks.value = res.content
    totalPages.value = res.totalPages
  } catch (e) {
    error.value = (e as Error).message
  }
}

// Quand un filtre change on revient à la page 0
watch(() => [filters.completed, filters.priority, filters.sort], () => {
  filters.page = 0
  load()
})
onMounted(load)

function goTo(p: number) {
  filters.page = p
  load()
}

/** Terminer / rouvrir une tâche : PATCH /api/tasks/{id}/toggle. */
async function toggle(task: Task) {
  if (busyId.value !== null) return // évite le double clic
  busyId.value = task.identifiant
  let failure = ''
  try {
    await api.toggleTask(task.identifiant)
  } catch (e) {
    failure = (e as Error).message
  }
  await load() // recharge la liste (et remet la case dans le bon état en cas d'erreur)
  if (failure) error.value = failure
  busyId.value = null
}

async function confirmDelete() {
  try {
    await api.deleteTask(toDelete.value!.identifiant)
    toDelete.value = null
    // si on supprime le dernier élément d'une page, on recule d'une page
    if (tasks.value.length === 1 && filters.page > 0) filters.page--
    await load()
  } catch (e) {
    error.value = (e as Error).message
    toDelete.value = null
  }
}
</script>

<template>
  <div class="header">
    <h1 class="page-title">
      <span class="title-icon"><AppIcon name="tasks" :size="24" /></span>
      Mes tâches
    </h1>
    <router-link to="/tasks/new" class="btn"><AppIcon name="plus" :size="18" /> Nouvelle tâche</router-link>
  </div>

  <!-- Chaque champ a maintenant un titre visible : on voit à quoi il sert -->
  <div class="card filters">
    <label class="field">
      <span class="field-label"><AppIcon name="filter" :size="16" /> Statut</span>
      <select v-model="filters.completed" data-test="filter-status">
        <option value="">Tous les statuts</option>
        <option value="false">En cours</option>
        <option value="true">Terminées</option>
      </select>
    </label>
    <label class="field">
      <span class="field-label"><AppIcon name="flag" :size="16" /> Priorité</span>
      <select v-model="filters.priority" data-test="filter-priority">
        <option value="">Toutes priorités</option>
        <option v-for="p in PRIORITIES" :key="p.value" :value="p.value">{{ p.label }}</option>
      </select>
    </label>
    <label class="field">
      <span class="field-label"><AppIcon name="sort" :size="16" /> Trier par</span>
      <select v-model="filters.sort" data-test="filter-sort">
        <option value="dateEcheance,asc">Échéance (Asc)</option>
        <option value="dateEcheance,desc">Échéance (Desc)</option>
        <option value="dateCreation,desc">Création (Desc)</option>
        <option value="dateCreation,asc">Création (Asc)</option>
      </select>
    </label>
  </div>

  <p v-if="error" class="error">{{ error }}</p>
  <p v-if="!tasks.length && !error" class="muted">Aucune tâche pour ces filtres.</p>

  <ul class="list">
    <li
      v-for="t in tasks"
      :key="t.identifiant"
      class="card row"
      :class="{ overdue: isOverdue(t), done: t.estTermine }"
    >
      <div class="grow">
        <label class="check">
          <input type="checkbox" :checked="t.estTermine" :disabled="busyId !== null" @change="toggle(t)" />
          <strong>{{ t.titre }}</strong>
        </label>
        <p v-if="t.description" class="muted">{{ t.description }}</p>
        <div class="meta">
          <span class="badge" :class="'prio-' + t.priorite">
            <AppIcon name="flag" :size="13" /> {{ priorityLabel(t.priorite) }}
          </span>
          <span class="muted meta-date"><AppIcon name="calendar" :size="14" /> {{ formatDate(t.dateEcheance) }}</span>
          <span v-if="isOverdue(t)" class="badge late"><AppIcon name="alert" :size="13" /> En retard</span>
        </div>
      </div>
      <div class="actions">
        <!-- Bouton visible : appelle l'API PATCH /api/tasks/{id}/toggle -->
        <button
          class="btn btn-outline"
          data-test="toggle"
          :disabled="busyId !== null"
          @click="toggle(t)"
        >
          <AppIcon :name="t.estTermine ? 'undo' : 'check'" :size="16" />
          {{ t.estTermine ? 'Rouvrir' : 'Terminer' }}
        </button>
        <router-link :to="`/tasks/${t.identifiant}/edit`" class="btn btn-outline">
          <AppIcon name="edit" :size="16" /> Modifier
        </router-link>
        <button class="btn btn-danger" @click="toDelete = t">
          <AppIcon name="trash" :size="16" /> Supprimer
        </button>
      </div>
    </li>
  </ul>

  <AppPagination :page="filters.page" :total-pages="totalPages" @change="goTo" />

  <ConfirmDialog
    v-if="toDelete"
    :message="`Supprimer la tâche « ${toDelete.titre} » ?`"
    @confirm="confirmDelete"
    @cancel="toDelete = null"
  />
</template>
