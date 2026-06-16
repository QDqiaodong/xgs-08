import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: {
      id: 1,
      username: 'bonsai_master',
      nickname: '盆景大师',
      avatar: '',
      email: '',
      bio: '',
    },
  }),
  getters: {
    isLoggedIn: (state) => !!state.currentUser,
    userId: (state) => state.currentUser?.id,
  },
  actions: {
    setUser(user) {
      this.currentUser = user
    },
    clearUser() {
      this.currentUser = null
    },
  },
  persist: {
    key: 'bonsai-user',
    storage: localStorage,
  },
})
