import { defineStore } from 'pinia'
import { getUserById } from '@/api/user'

const DEFAULT_USER_ID = 1

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null,
    userLoaded: false,
  }),
  getters: {
    isLoggedIn: (state) => !!state.currentUser,
    userId: (state) => state.currentUser?.id ?? DEFAULT_USER_ID,
  },
  actions: {
    setUser(user) {
      this.currentUser = user
      this.userLoaded = true
    },
    clearUser() {
      this.currentUser = null
      this.userLoaded = false
    },
    async fetchCurrentUser(forceRefresh = false) {
      if (this.userLoaded && !forceRefresh && this.currentUser) {
        return this.currentUser
      }
      try {
        const id = this.currentUser?.id ?? DEFAULT_USER_ID
        const user = await getUserById(id)
        if (user) {
          this.setUser(user)
        }
        return user
      } catch (e) {
        console.warn('获取当前用户信息失败', e)
        if (!this.currentUser) {
          this.currentUser = {
            id: DEFAULT_USER_ID,
            username: 'bonsai_master',
            nickname: '盆景爱好者',
            avatar: '',
            email: '',
            bio: '',
          }
        }
        this.userLoaded = true
        return this.currentUser
      }
    },
  },
  persist: {
    key: 'bonsai-user',
    storage: localStorage,
    pick: ['currentUser'],
  },
})
