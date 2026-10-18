import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'

const logout = vi.hoisted(() => vi.fn())
vi.mock('../src/services/keycloak', async () => {
  const { reactive } = await import('vue')
  return { auth: reactive({ username: 'Alice', roles: ['user'] }), logout }
})

import { auth } from '../src/services/keycloak'
import NavBar from '../src/components/NavBar.vue'

const mountNav = () => mount(NavBar, { global: { stubs: { 'router-link': { template: '<a><slot/></a>' } } } })

describe('NavBar', () => {
  beforeEach(() => { auth.roles = ['user']; logout.mockClear() })

  it('affiche le nom de l\'utilisateur', () => {
    expect(mountNav().get('[data-test=username]').text()).toBe('Alice')
  })
  it('cache le lien Administration pour un simple user', () => {
    expect(mountNav().find('[data-test=admin-link]').exists()).toBe(false)
  })
  it('affiche le lien Administration pour un admin', () => {
    auth.roles = ['user', 'admin']
    expect(mountNav().find('[data-test=admin-link]').exists()).toBe(true)
  })
  it('appelle logout au clic sur Déconnexion', async () => {
    await mountNav().get('[data-test=logout]').trigger('click')
    expect(logout).toHaveBeenCalled()
  })
})
