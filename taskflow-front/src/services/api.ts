import { getToken } from './keycloak'
import type { AdminStat, Me, Page, QueryParams, Stats, Task, TaskPayload } from '../types'

const API_URL = import.meta.env.VITE_API_URL

export class ApiError extends Error {
  status: number
  fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

/** Lit le format d'erreur du backend -> ADAPTEZ ICI si votre format est différent. */
function parseError(status: number, body: any): ApiError {
  const fieldErrors: Record<string, string> = {}
  const errs = body?.errors ?? body?.fieldErrors ?? body?.details
  if (Array.isArray(errs)) {
    errs.forEach((e: { field?: string; message: string }) => {
      if (e.field) fieldErrors[e.field] = e.message
    })
  } else if (errs && typeof errs === 'object') {
    Object.assign(fieldErrors, errs)
  }
  return new ApiError(status, body?.message || `Erreur ${status}`, fieldErrors)
}

interface RequestOptions {
  params?: QueryParams
  body?: unknown
}

async function request<T>(method: string, path: string, { params, body }: RequestOptions = {}): Promise<T> {
  const url = new URL(API_URL + path)
  Object.entries(params || {}).forEach(([k, v]) => {
    if (v !== '' && v !== null && v !== undefined) url.searchParams.set(k, String(v))
  })

  const res = await fetch(url, {
    method,
    headers: {
      Authorization: `Bearer ${await getToken()}`,
      ...(body ? { 'Content-Type': 'application/json' } : {})
    },
    body: body ? JSON.stringify(body) : undefined
  })

  if (!res.ok) {
    let data: unknown = null
    try { data = await res.json() } catch { /* corps vide */ }
    throw parseError(res.status, data)
  }
  if (res.status === 204) return null as T
  const text = await res.text()
  return (text ? JSON.parse(text) : null) as T
}

export const api = {
  me: () => request<Me>('GET', '/api/me'),

  tasks: (params?: QueryParams) => request<Page<Task>>('GET', '/api/tasks', { params }),
  task: (identifiant: string ) => request<Task>('GET', `/api/tasks/${identifiant}`),
  createTask: (data: TaskPayload) => request<Task>('POST', '/api/tasks', { body: data }),
  updateTask: (identifiant: string , data: TaskPayload) => request<Task>('PUT', `/api/tasks/${identifiant}`, { body: data }),
  toggleTask: (identifiant: string ) => request<Task>('PATCH', `/api/tasks/${identifiant}/toggle`),
  deleteTask: (identifiant: string ) => request<null>('DELETE', `/api/tasks/${identifiant}`),
  stats: () => request<Partial<Stats>>('GET', '/api/tasks/stats'),

  adminTasks: (params?: QueryParams) => request<Page<Task>>('GET', '/api/admin/tasks', { params }),
  adminDelete: (identifiant: string ) => request<null>('DELETE', `/api/admin/tasks/${identifiant}`),
  adminStats: () => request<unknown>('GET', '/api/admin/stats')
}

export type { AdminStat }
