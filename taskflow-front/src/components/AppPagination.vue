<script setup lang="ts">
// page commence à 0 (comme Spring Data)
defineProps<{ page: number; totalPages: number }>()
defineEmits<{ change: [page: number] }>()
</script>

<template>
  <div v-if="totalPages > 1" class="pagination">
    <button class="btn btn-outline" data-test="prev" :disabled="page === 0" @click="$emit('change', page - 1)">
      ‹ Précédent
    </button>
    <span>Page {{ page + 1 }} / {{ totalPages }}</span>
    <button class="btn btn-outline" data-test="next" :disabled="page >= totalPages - 1" @click="$emit('change', page + 1)">
      Suivant ›
    </button>
  </div>
</template>
