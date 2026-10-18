<script setup lang="ts">
import type { Stats } from '../types'
import AppIcon from './AppIcon.vue'

defineProps<{ stats: Stats }>()
</script>

<template>
  <div class="stats">
    <article class="card stat stat-ok">
      <span class="stat-icon"><AppIcon name="check-circle" :size="30" /></span>
      <strong data-test="stat-completed">{{ stats.completed }}</strong>
      <span class="stat-label">Terminées</span>
    </article>
    <article class="card stat stat-progress">
      <span class="stat-icon"><AppIcon name="activity" :size="30" /></span>
      <strong data-test="stat-inprogress">{{ stats.inProgress }}</strong>
      <span class="stat-label">En cours</span>
    </article>
    <article class="card stat stat-late">
      <span class="stat-icon"><AppIcon name="alert" :size="30" /></span>
      <strong data-test="stat-overdue">{{ stats.overdue }}</strong>
      <span class="stat-label">En retard</span>
    </article>
  </div>
</template>
