import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AppPagination from '../src/components/AppPagination.vue'

describe('AppPagination', () => {
  it('ne s\'affiche pas pour une seule page', () => {
    expect(mount(AppPagination, { props: { page: 0, totalPages: 1 } }).find('.pagination').exists()).toBe(false)
  })
  it('désactive Précédent sur la première page', () => {
    const w = mount(AppPagination, { props: { page: 0, totalPages: 3 } })
    expect(w.get('[data-test=prev]').attributes('disabled')).toBeDefined()
  })
  it('émet la page suivante', async () => {
    const w = mount(AppPagination, { props: { page: 0, totalPages: 3 } })
    await w.get('[data-test=next]').trigger('click')
    expect(w.emitted('change')![0]).toEqual([1])
  })
})
