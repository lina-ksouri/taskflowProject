/// <reference types="vite/client" />

// Typage des variables d'environnement (.env)
interface ImportMetaEnv {
  readonly VITE_KEYCLOAK_URL: string
  readonly VITE_KEYCLOAK_REALM: string
  readonly VITE_KEYCLOAK_CLIENT_ID: string
  readonly VITE_API_URL: string
}
interface ImportMeta {
  readonly env: ImportMetaEnv
}

// Permet d'importer les fichiers .vue dans le TypeScript
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<object, object, unknown>
  export default component
}
