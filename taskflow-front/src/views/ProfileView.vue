<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '../services/api'
import type { Me } from '../types'
import AppIcon from '../components/AppIcon.vue'

const me = ref<Me | null>(null)
const error = ref('')

onMounted(async () => {
  try { me.value = await api.me() } catch (e) { error.value = (e as Error).message }
})
</script>

<template>
  <h1 class="page-title">
    <span class="title-icon"><AppIcon name="user" :size="24" /></span>
    Profil
  </h1>
  <p v-if="error" class="error">{{ error }}</p>
  <div v-if="me" class="card">
    <ul class="profile-list">
      <li>
        <span class="profile-icon"><AppIcon name="user" :size="20" /></span>
        <div><span class="muted">Nom</span><strong>{{ me.name || me.username }}</strong></div>
      </li>
      <li>
        <span class="profile-icon"><AppIcon name="mail" :size="20" /></span>
        <div><span class="muted">Email</span><strong>{{ me.email }}</strong></div>
      </li>
      <li>
        <span class="profile-icon"><AppIcon name="shield" :size="20" /></span>
        <div>
          <span class="muted">Rôles</span>
          <span>
            <span v-for="r in me.roles" :key="r" class="badge role">{{ r }}</span>
          </span>
        </div>
      </li>
    </ul>
  </div>
</template>
