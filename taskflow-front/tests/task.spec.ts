import { describe, it, expect } from 'vitest'
import { isOverdue } from '../src/utils/task'

describe('isOverdue', () => {
  it('vrai si échéance passée et non terminée', () => {
    expect(isOverdue({ estTermine: false, dateEcheance: '2000-01-01' })).toBe(true)
  })
  it('faux si terminée', () => {
    expect(isOverdue({ estTermine: true, dateEcheance: '2000-01-01' })).toBe(false)
  })
  it('faux sans échéance', () => {
    expect(isOverdue({ estTermine: false, dateEcheance: null })).toBe(false)
  })
})
