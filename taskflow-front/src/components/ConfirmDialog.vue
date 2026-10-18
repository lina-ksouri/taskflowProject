<script setup lang="ts">
withDefaults(defineProps<{ message?: string }>(), { message: 'Êtes-vous sûr ?' })
defineEmits<{ confirm: []; cancel: [] }>()
</script>

<template>
  <div class="overlay">
    <div class="dialog" role="dialog">
      <p>{{ message }}</p>
      <div class="actions">
        <button class="btn btn-outline" data-test="cancel" @click="$emit('cancel')">Annuler</button>
        <button class="btn btn-danger" data-test="confirm" @click="$emit('confirm')">Supprimer</button>
      </div>
    </div>
  </div>
</template>
