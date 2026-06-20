<template>
  <div id="app">
    <router-view v-if="userStore.userLoaded" />
    <div v-else class="app-loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(async () => {
  await userStore.fetchCurrentUser()
})
</script>

<style>
#app {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.app-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}
</style>
