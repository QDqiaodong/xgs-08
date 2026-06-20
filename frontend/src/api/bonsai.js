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

export function validateBonsai(data) {
  return request.post('/bonsais/validate', data)
}

export function getStageImages(bonsaiId) {
  return request.get(`/bonsais/${bonsaiId}/stage-images`)
}

export function getStageImagesGrouped(bonsaiId) {
  return request.get(`/bonsais/${bonsaiId}/stage-images/grouped`)
}

export function addStageImage(bonsaiId, data) {
  return request.post(`/bonsais/${bonsaiId}/stage-images`, data)
}

export function addStageImagesBatch(bonsaiId, data) {
  return request.post(`/bonsais/${bonsaiId}/stage-images/batch`, data)
}

export function updateStageImage(imageId, data) {
  return request.put(`/bonsais/stage-images/${imageId}`, data)
}

export function deleteStageImage(imageId) {
  return request.delete(`/bonsais/stage-images/${imageId}`)
}

export function deleteStageImagesByStage(bonsaiId, stage) {
  return request.delete(`/bonsais/${bonsaiId}/stage-images/stage/${stage}`)
}
