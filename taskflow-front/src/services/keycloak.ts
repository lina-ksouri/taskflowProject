import Keycloak from 'keycloak-js'
import { reactive } from 'vue'

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID
})

interface AuthState {
  ready: boolean
  username: string
  roles: string[]
}

export const auth = reactive<AuthState>({ ready: false, username: '', roles: [] })

function refreshState(): void {
  const t = keycloak.tokenParsed
  auth.username = (t?.name as string | undefined) || t?.preferred_username || ''
  auth.roles = t?.realm_access?.roles || []
}

export const isAdmin = (): boolean => auth.roles.includes('admin')

export async function initKeycloak(): Promise<void> {
  await keycloak.init({
    onLoad: 'login-required',
    pkceMethod: 'S256',
    checkLoginIframe: false
  })
  refreshState()
  auth.ready = true

  keycloak.onTokenExpired = () => {
    keycloak.updateToken(30).then(refreshState).catch(() => keycloak.login())
  }
}

export async function getToken(): Promise<string | undefined> {
  try {
    await keycloak.updateToken(30)
  } catch {
    await keycloak.login()
  }
  return keycloak.token
}

export function logout(): void {
  keycloak.logout({ redirectUri: window.location.origin })
}

export default keycloak
