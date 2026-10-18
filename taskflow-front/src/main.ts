import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { initKeycloak } from './services/keycloak'
import './style.css'

// On ne monte l'application qu'après connexion réussie via Keycloak
initKeycloak()
  .then(() => createApp(App).use(router).mount('#app'))
  .catch((e: unknown) => {
    document.getElementById('app')!.textContent = 'Impossible de contacter Keycloak.'
    console.error(e)
  })
