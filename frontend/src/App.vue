<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'
import { useToastStore } from '@/stores/useToastStore'
import { useConfigStore } from "@/stores/useConfigStore"
import Toast from '@/components/Toast.vue'
import ConfigErrorView from '@/views/ConfigErrorView.vue'

const router = useRouter()
const authStore = useAuthStore()
const toastStore = useToastStore()
const configStore = useConfigStore()
//const authChannel = new BroadcastChannel('auth_channel')

// 401（セッション切れ）検知時の共通ハンドラ
const handleUnauthorized = () => {
  if (typeof authStore.clearUser === 'function') {
    authStore.clearUser()
  } else {
    authStore.user = null
  }

  toastStore.showToast('セッションが切れました。再度ログインしてください。', 'error')

  if (router.currentRoute.value.path !== '/login') {
    router.push('/login')
  }
}


onMounted(() => {
  window.addEventListener('auth:unauthorized', handleUnauthorized as EventListener)

  // 別タブ・別ウィンドウからのログイン/ログアウト通知を受信
  // authChannel.onmessage = (event) => {
  //   const { type, userId } = event.data

  //   if (type === 'LOGOUT') {
  //     authStore.clearUser()
  //     if (router.currentRoute.value.path !== '/login') {
  //       toastStore.showToast('別ウィンドウでログアウトされました。', 'info')
  //       router.push('/login')
  //     }
  //   }

  //   if (type === 'LOGIN') {
  //     // ログイン画面にいる場合や、今表示しているユーザーと違うユーザーでログインされた場合
  //     if (router.currentRoute.value.path === '/login' || (authStore.user && authStore.user.userId !== userId)) {
  //       window.location.reload()
  //     }
  //   }
  // }
})

onUnmounted(() => {
  window.removeEventListener('auth:unauthorized', handleUnauthorized as EventListener)
  //authChannel.close() // クリーンアップ
})
</script>

<template>

  <Toast />

  <ConfigLoadingView
    v-if="!configStore.loaded && !configStore.loadFailed"
  />

  <ConfigErrorView
    v-else-if="configStore.loadFailed"
  />

  <RouterView
    v-else
  />

</template>