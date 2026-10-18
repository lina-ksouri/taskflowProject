<script setup lang="ts">
import AppIcon from '../components/AppIcon.vue'
</script>

<template>
  <div class="card empty-state">
    <span class="title-icon"><AppIcon name="lock" :size="24" /></span>
    <h1>Accès refusé</h1>
    <p>Vous n'avez pas les droits pour accéder à cette page.</p>
    <router-link to="/" class="btn"><AppIcon name="home" :size="16" /> Retour à l'accueil</router-link>
  </div>
</template>
