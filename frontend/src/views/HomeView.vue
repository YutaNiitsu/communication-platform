<!-- src/views/HomeView.vue -->
<template>
  <div class="home-layout-container">
    <div class="main-content-area">
      <!-- 左側サイドバー（チャンネル一覧コンポーネント） -->
      <ChannelListView />

      <!-- 右側メイン領域: チャット詳細 ＆ モーダル -->
      <main class="content-pane">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useChannelStore } from "@/stores/useChannelStore"
import { useUserStore } from "@/stores/useUserStore"
import { webSocketService } from "@/services/websocketService"
import ChannelListView from "@/views/ChannelListView.vue"

const router = useRouter()
const route = useRoute()
const channelStore = useChannelStore()
const userStore = useUserStore()

const channels = computed(() => channelStore.channels)

const currentChannelId = computed(() => {
  const id = route.params.id
  return id ? Number(id) : null
})

onMounted(async () => {
  try {
    await Promise.all([
      channelStore.fetchChannels(),
      userStore.fetchAllUsers()
    ])
    
    console.log('【HTTP】初期データの読み込みが完了しました。')
  } catch (error) {
    console.error('初期データの取得に失敗しました:', error)
  }

  await Promise.all([
    channelStore.fetchChannels(),
    userStore.fetchAllUsers()
  ])

  console.log('【WS】WebSocket接続を開始します...')
  webSocketService.connect()

  checkAndSelectFirstChannel()
})

onUnmounted(() => {
  webSocketService.disconnect()
})

watch(currentChannelId, (newId, oldId) => {
  if (oldId !== null) {
    webSocketService.unsubscribe(oldId)
  }
  if (newId !== null) {
    webSocketService.subscribe(newId)
  }
})

watch(
  [() => channels.value, () => route.path],
  () => {
    checkAndSelectFirstChannel()
  }
)

function checkAndSelectFirstChannel() {
  if ((route.path === '/home' || route.path === '/home/') && channels.value.length > 0) {
    const firstChannelId = channels.value[0].id
    router.replace(`/home/${firstChannelId}`)
  } else if (currentChannelId.value !== null) {
    webSocketService.subscribe(currentChannelId.value)
  }
}
</script>