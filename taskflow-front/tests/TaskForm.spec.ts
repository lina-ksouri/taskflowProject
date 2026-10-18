import { describe, it, expect, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

// vi.hoisted : variables utilisables dans les vi.mock (qui sont remontés en haut du fichier)
const { createTask, FakeApiError } = vi.hoisted(() => ({
  createTask: vi.fn(),
  FakeApiError: class extends Error {
    status: number
    fieldErrors: Record<string, string>
    constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
      super(message)
      this.status = status
      this.fieldErrors = fieldErrors
    }
  }
}))

vi.mock('vue-router', () => ({ useRouter: () => ({ push: vi.fn() }) }))
vi.mock('../src/services/api', () => ({ ApiError: FakeApiError, api: { createTask } }))

import TaskForm from '../src/views/TaskFormView.vue'

const mountForm = () => mount(TaskForm, { global: { stubs: { 'router-link': true } } })

describe('TaskForm', () => {
  it('refuse un titre vide sans appeler l\'API', async () => {
    const w = mountForm()
    await w.get('form').trigger('submit')
    expect(w.get('[data-test=title-error]').text()).toContain('obligatoire')
    expect(createTask).not.toHaveBeenCalled()
  })

  it('affiche les erreurs de l\'API par champ', async () => {
    createTask.mockRejectedValueOnce(new FakeApiError(400, 'Validation', { titre: 'Titre invalide (API)' }))
    const w = mountForm()
    await w.get('[data-test=title]').setValue('Ma tâche')
    await w.get('form').trigger('submit')
    await flushPromises()
    expect(w.get('[data-test=title-error]').text()).toBe('Titre invalide (API)')
  })

  it('envoie les champs avec les noms français attendus par l\'API', async () => {
    createTask.mockResolvedValueOnce({})
    const w = mountForm()
    await w.get('[data-test=title]').setValue('  Ma tâche  ')
    await w.get('form').trigger('submit')
    await flushPromises()
    expect(createTask).toHaveBeenCalledWith({
      titre: 'Ma tâche',
      description: null,
      priorite: 'NORMALE',
      dateEcheance: null
    })
  })
})
