import request from '@/utils/request'

export function getEventsByBonsaiId(bonsaiId) {
  return request.get(`/lifecycle-events/bonsai/${bonsaiId}`)
}

export function getEventById(id) {
  return request.get(`/lifecycle-events/${id}`)
}

export function createEvent(data) {
  return request.post('/lifecycle-events', data)
}

export function updateEvent(data) {
  return request.put('/lifecycle-events', data)
}

export function deleteEvent(id, userId) {
  return request.delete(`/lifecycle-events/${id}`, { params: { userId } })
}
