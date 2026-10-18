<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, ApiError } from '../services/api'
import { PRIORITIES } from '../utils/task'
import type { Priority, TaskPayload } from '../types'
import AppIcon from '../components/AppIcon.vue'

const props = defineProps<{ id?: string }>()
const router = useRouter()
const isEdit = computed(() => !!props.id)

const form = reactive<{ titre: string; description: string; priorite: Priority; dateEcheance: string }>({
  titre: '', description: '', priorite: 'NORMALE', dateEcheance: ''
})
const errors = ref<Record<string, string>>({})     // erreurs par champ (validation locale + API)
const globalError = ref('')
const saving = ref(false)

onMounted(async () => {
  if (!isEdit.value) return
  try {
    const t = await api.task(props.id!)
    form.titre = t.titre
    form.description = t.description || ''
    form.priorite = t.priorite
    form.dateEcheance = t.dateEcheance ? t.dateEcheance.substring(0, 10) : ''
  } catch (e) {
    globalError.value = (e as Error).message
  }
})

/** Validation côté client (le backend valide aussi). */
function validate() {
  const e: Record<string, string> = {}
  if (!form.titre.trim()) e.titre = 'Le titre est obligatoire.'
  else if (form.titre.length > 100) e.titre = 'Le titre ne doit pas dépasser 100 caractères.'
  errors.value = e
  return Object.keys(e).length === 0
}

async function submit() {
  globalError.value = ''
  if (!validate()) return
  saving.value = true

  const payload: TaskPayload = {
    titre: form.titre.trim(),
    description: form.description || null,
    priorite: form.priorite,
    dateEcheance: form.dateEcheance || null
  }

  try {
    if (props.id) await api.updateTask(props.id, payload)
    else await api.createTask(payload)
    router.push('/tasks')
  } catch (e) {
    // Affiche les erreurs de l'API champ par champ
    if (e instanceof ApiError && Object.keys(e.fieldErrors).length) errors.value = e.fieldErrors
    globalError.value = (e as Error).message
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <h1 class="page-title">
    <span class="title-icon"><AppIcon :name="isEdit ? 'edit' : 'plus'" :size="24" /></span>
    {{ isEdit ? 'Modifier la tâche' : 'Nouvelle tâche' }}
  </h1>

  <form class="card form" novalidate @submit.prevent="submit">
    <p v-if="globalError" class="error">{{ globalError }}</p>

    <label>
      Titre *
      <input v-model="form.titre" data-test="title" maxlength="200" />
      <small v-if="errors.titre" class="error" data-test="title-error">{{ errors.titre }}</small>
    </label>

    <label>
      Description
      <textarea v-model="form.description" rows="3" />
      <small v-if="errors.description" class="error">{{ errors.description }}</small>
    </label>

    <label>
      Priorité
      <select v-model="form.priorite">
        <option v-for="p in PRIORITIES" :key="p.value" :value="p.value">{{ p.label }}</option>
      </select>
      <small v-if="errors.priorite" class="error">{{ errors.priorite }}</small>
    </label>

    <label>
      Date d'échéance
      <input v-model="form.dateEcheance" type="date" />
      <small v-if="errors.dateEcheance" class="error">{{ errors.dateEcheance }}</small>
    </label>

    <div class="actions">
      <router-link to="/tasks" class="btn btn-outline">Annuler</router-link>
      <button class="btn" type="submit" :disabled="saving">
        <AppIcon name="check" :size="16" />
        {{ saving ? 'Enregistrement…' : 'Enregistrer' }}
      </button>
    </div>
  </form>
</template>
