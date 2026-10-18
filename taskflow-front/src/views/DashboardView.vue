<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '../services/api'
import { formatDate, isOverdue, normalizeStats, priorityLabel } from '../utils/task'
import type { Stats, Task } from '../types'
import StatsCards from '../components/StatsCards.vue'
import AppIcon from '../components/AppIcon.vue'

const stats = ref<Stats>(normalizeStats())
const upcoming = ref<Task[]>([])
const error = ref('')

onMounted(async () => {
  try {
    stats.value = normalizeStats(await api.stats())
    // On récupère les tâches non terminées triées par échéance, puis on garde les 5 premières avec une date
  const page = await api.tasks({
  estTermine: false,
  sortBy: 'dateEcheance',
  direction: 'asc',
  size: 50
})
upcoming.value = page.content.filter((t) => t.dateEcheance).slice(0, 5)
  } catch (e) {
    error.value = (e as Error).message
  }
})
</script>

<template>
  <h1 class="page-title">
    <span class="title-icon"><AppIcon name="layout" :size="24" /></span>
    Tableau de bord
  </h1>
  <p v-if="error" class="error">{{ error }}</p>

  <StatsCards :stats="stats" />

  <h2 class="section-title"><AppIcon name="calendar" :size="22" /> 5 prochaines échéances</h2>
  <p v-if="!upcoming.length" class="muted">Aucune échéance à venir.</p>
  <ul class="list">
    <li v-for="t in upcoming" :key="t.identifiant" class="card row" :class="{ overdue: isOverdue(t), done: t.estTermine }">
      <div>
        <strong>{{ t.titre }}</strong>
        <div class="muted">Priorité : {{ priorityLabel(t.priorite) }}</div>
      </div>
      <span class="badge badge-date"><AppIcon name="calendar" :size="14" /> {{ formatDate(t.dateEcheance) }}</span>
    </li>
  </ul>
</template>
