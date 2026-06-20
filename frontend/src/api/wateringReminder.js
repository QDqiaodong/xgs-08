import request from '@/utils/request'

export function getUserWateringReminders(userId) {
  return request.get(`/watering-reminders/user/${userId}`)
}

export function getBonsaiWateringReminder(bonsaiId) {
  return request.get(`/watering-reminders/bonsai/${bonsaiId}`)
}
