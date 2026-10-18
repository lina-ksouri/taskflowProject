import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ConfirmDialog from '../src/components/ConfirmDialog.vue'

describe('ConfirmDialog', () => {
  it('affiche le message', () => {
    expect(mount(ConfirmDialog, { props: { message: 'Supprimer ?' } }).text()).toContain('Supprimer ?')
  })
  it('émet confirm et cancel', async () => {
    const w = mount(ConfirmDialog)
    await w.get('[data-test=confirm]').trigger('click')
    await w.get('[data-test=cancel]').trigger('click')
    expect(w.emitted('confirm')).toHaveLength(1)
    expect(w.emitted('cancel')).toHaveLength(1)
  })
})
