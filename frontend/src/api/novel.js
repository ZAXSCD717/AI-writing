import request from './index'

export function uploadNovel(data) {
  return request.post('/novels', data)
}

export function getNovel(id) {
  return request.get(`/novels/${id}`)
}

export function listNovels(page = 1, size = 10) {
  return request.get('/novels', { params: { page, size } })
}

export function getChapters(id) {
  return request.get(`/novels/${id}/chapters`)
}

export function convertNovel(id, chapterIndices = null) {
  const data = chapterIndices ? { chapterIndices } : {}
  return request.post(`/novels/${id}/convert`, data)
}
