import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: {
      id: 1,
      username: 'bonsai_master',
      nickname: '盆景大师',
      avatar: '',
    },
  }),
  actions: {
    setUser(user) {
      this.currentUser = user
    },
    clearUser() {
      this.currentUser = null
    },
  },
  persist: true,
})
