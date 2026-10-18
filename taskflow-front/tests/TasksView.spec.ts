import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const { tasks, toggleTask, deleteTask } = vi.hoisted(() => ({
  tasks: vi.fn(),
  toggleTask: vi.fn(),
  deleteTask: vi.fn()
}))

vi.mock('../src/services/api', () => ({ api: { tasks, toggleTask, deleteTask } }))

import TasksView from '../src/views/TasksView.vue'

const uneTache = (estTermine = false) => ({
  identifiant: 'abc-123',
  titre: 'Écrire le rapport',
  description: null,
  priorite: 'NORMALE',
  dateEcheance: null,
  estTermine
})

const mountView = () => mount(TasksView, { global: { stubs: { 'router-link': true } } })

describe('TasksView', () => {
  beforeEach(() => {
    tasks.mockReset()
    toggleTask.mockReset()
    tasks.mockResolvedValue({ content: [uneTache()], totalPages: 1 })
    toggleTask.mockResolvedValue({})
  })

  it('affiche un bouton Terminer qui appelle le toggle avec l\'identifiant', async () => {
    const w = mountView()
    await flushPromises()
    const bouton = w.get('[data-test=toggle]')
    expect(bouton.text()).toContain('Terminer')
    await bouton.trigger('click')
    await flushPromises()
    expect(toggleTask).toHaveBeenCalledWith('abc-123')
    // la liste est rechargée après le changement de statut
    expect(tasks).toHaveBeenCalledTimes(2)
  })

  it('affiche Rouvrir pour une tâche terminée', async () => {
    tasks.mockResolvedValue({ content: [uneTache(true)], totalPages: 1 })
    const w = mountView()
    await flushPromises()
    expect(w.get('[data-test=toggle]').text()).toContain('Rouvrir')
  })

  it('envoie le tri sous la forme sortBy + direction', async () => {
    const w = mountView()
    await flushPromises()
    expect(tasks).toHaveBeenLastCalledWith(expect.objectContaining({ sortBy: 'dateEcheance', direction: 'asc' }))

    await w.get('[data-test=filter-sort]').setValue('dateCreation,desc')
    await flushPromises()
    expect(tasks).toHaveBeenLastCalledWith(expect.objectContaining({ sortBy: 'dateCreation', direction: 'desc' }))
  })
})
