// Les types partagés : ils décrivent la forme des données échangées avec l'API.
export type Priority = 'BASSE' | 'NORMALE' | 'HAUTE'

export interface Task {
  identifiant: string
  titre: string
  description: string | null
  priorite: Priority
  dateEcheance: string | null
  estTermine: boolean
  ownerId?: string
  proprietaire?: string
  dateCreation?: string
  dateMiseAJour?: string
}

/** Données envoyées à l'API pour créer / modifier une tâche. */
export interface TaskPayload {
  titre: string
  description: string | null
  priorite: Priority
  dateEcheance: string | null
}

/** Réponse paginée Spring Data. */
export interface Page<T> {
  content: T[]
  totalPages: number
  totalElements?: number
  number?: number
  size?: number
}

export interface Stats {
  completed: number
  inProgress: number
  overdue: number
}

export interface AdminStat extends Stats {
  owner: string
}

export interface Me {
  name?: string
  username?: string
  email: string
  roles: string[]
}

export type QueryParams = Record<string, string | number | boolean | null | undefined>
