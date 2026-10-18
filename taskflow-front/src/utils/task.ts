import type { Priority, Stats, Task } from '../types'

export const PRIORITIES: { value: Priority; label: string }[] = [
  { value: 'BASSE', label: 'Basse' },
  { value: 'NORMALE', label: 'Normale' },
  { value: 'HAUTE', label: 'Haute' }
]

export const priorityLabel = (v: string): string => PRIORITIES.find((p) => p.value === v)?.label || v

export function isOverdue(task: Pick<Task, 'estTermine' | 'dateEcheance'>): boolean {
  if (task.estTermine || !task.dateEcheance) return false
  const due = new Date(task.dateEcheance)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return due < today
}

export function formatDate(d: string | null | undefined): string {
  return d ? new Date(d).toLocaleDateString('fr-FR') : '—'
}

/** Normalise les stats (accepte inProgress/pending/active, overdue/late). */
export function normalizeStats(s: Record<string, any> = {}): Stats {
  return {
    completed: s.completed ?? s.done ?? 0,
    inProgress: s.inProgress ?? s.pending ?? s.active ?? 0,
    overdue: s.overdue ?? s.late ?? 0
  }
}
