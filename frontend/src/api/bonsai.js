import request from '@/utils/request'

export function getUserBonsais(userId, params) {
  return request.get(`/bonsais/user/${userId}`, { params })
}

export function getUserBonsaiList(userId) {
  return request.get(`/bonsais/user/${userId}/list`)
}

export function getUserBonsaiProfile(userId) {
  return request.get(`/bonsais/user/${userId}/profile`)
}

export function getBonsaiById(id) {
  return request.get(`/bonsais/${id}`)
}

export function createBonsai(data) {
  return request.post('/bonsais', data)
}

export function updateBonsai(data) {
  return request.put('/bonsais', data)
}

export function deleteBonsai(id) {
  return request.delete(`/bonsais/${id}`)
}

export function getCareSummary(bonsaiId) {
  return request.get(`/bonsais/${bonsaiId}/care-summary`)
}
