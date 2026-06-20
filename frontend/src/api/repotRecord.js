import request from '@/utils/request'

export function getBonsaiRepotRecords(bonsaiId) {
  return request.get(`/repot-records/bonsai/${bonsaiId}`)
}

export function getUserRepotRecords(userId, params) {
  return request.get(`/repot-records/user/${userId}`, { params })
}

export function getUserRepotRecordList(userId) {
  return request.get(`/repot-records/user/${userId}/list`)
}

export function getRepotRecordById(id) {
  return request.get(`/repot-records/${id}`)
}

export function createRepotRecord(data) {
  return request.post('/repot-records', data)
}

export function updateRepotRecord(data) {
  return request.put('/repot-records', data)
}

export function deleteRepotRecord(id) {
  return request.delete(`/repot-records/${id}`)
}
