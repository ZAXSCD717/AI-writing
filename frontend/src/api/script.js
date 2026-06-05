import request from './index'

export function getScript(id) {
  return request.get(`/scripts/${id}`)
}

export function listScripts(page = 1, size = 10) {
  return request.get('/scripts', { params: { page, size } })
}

export function updateScript(id, data) {
  return request.put(`/scripts/${id}`, data)
}

export function exportScript(id) {
  return `${request.defaults.baseURL}/scripts/${id}/export`
}
