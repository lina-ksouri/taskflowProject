<script setup lang="ts">
import { ref } from 'vue'
import { auth, logout } from '../services/keycloak'
import AppIcon from './AppIcon.vue'

const open = ref(false)
</script>

<template>
  <nav class="navbar">
    <router-link to="/" class="brand">
      <span class="brand-mark"><AppIcon name="tasks" :size="18" /></span>
      TaskFlow
    </router-link>

    <button class="burger" aria-label="Menu" :aria-expanded="open" @click="open = !open">
      <AppIcon name="menu" :size="24" />
    </button>

    <div class="links" :class="{ open }" @click="open = false">
      <!-- Les liens démarrent à gauche, juste après le logo -->
      <div class="nav-main">
        <router-link to="/" class="nav-link"><AppIcon name="layout" :size="18" /> Tableau de bord</router-link>
        <router-link to="/tasks" class="nav-link"><AppIcon name="tasks" :size="18" /> Mes tâches</router-link>
        <router-link to="/profile" class="nav-link"><AppIcon name="user" :size="18" /> Profil</router-link>
        <router-link v-if="auth.roles.includes('admin')" to="/admin" class="nav-link" data-test="admin-link">
          <AppIcon name="shield" :size="18" /> Administration
        </router-link>
      </div>

      <!-- Utilisateur et déconnexion poussés tout à droite -->
      <div class="nav-user">
        <span class="user" data-test="username"><AppIcon name="user" :size="16" />{{ auth.username }}</span>
        <button class="btn btn-ghost" data-test="logout" @click="logout">
          <AppIcon name="logout" :size="16" /> Déconnexion
        </button>
      </div>
    </div>
  </nav>
</template>
