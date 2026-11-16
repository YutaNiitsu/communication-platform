<!-- src/views/ChannelListView.vue -->
<template>
  <aside class="sidebar">
    <div class="sidebar__header">
      <span>▽ チャンネル一覧</span>
      <!-- 通常時：ホバーアクションエリア -->
      <div class="sidebar__header-hover-actions">
        <div class="kebab-menu-container" ref="kebabContainerRef">
          <button @click.stop="toggleKebabMenu" class="kebab-btn">⋮</button>
          
          <div v-if="isKebabOpen" class="kebab-dropdown">
            <button 
              @click="toggleArchivedChannels"
              class="kebab-dropdown__item"
            >
              {{ showArchived ? 'アーカイブを非表示' : 'アーカイブを表示' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- スクロール可能なチャンネル一覧エリア -->
    <ul class="channel-list">
      <li
        v-for="channel in displayedChannels"
        :key="channel.id"
        @click="selectChannel(channel.id)"
        :class="['channel-item', { 
          'is-active': currentChannelId === channel.id,
          'is-archived': channel.status === 1 /* 💡 必要に応じてアーカイブ用のスタイルを当てるため */
        }]"
      >
        {{ channel.status === 1 ? '🔒' : '#' }} {{ channel.name }}
      </li>
    </ul>

    <!-- 最下部固定のチャンネル作成ボタン -->
    <div class="sidebar__footer">
      <button @click="goCreateChannel" class="sidebar__create-btn">+ チャンネル作成</button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useChannelStore } from "@/stores/useChannelStore"

const router = useRouter()
const route = useRoute()
const channelStore = useChannelStore()

const showArchived = ref(localStorage.getItem("showArchivedChannels") === "true")
const isKebabOpen = ref(false)
const allChannels = computed(() => channelStore.channels)
// メニューの外側クリック検知用のDOM参照
const kebabContainerRef = ref<HTMLElement | null>(null)
const currentChannelId = computed(() => {
  const id = route.params.id
  return id ? Number(id) : null
})

onMounted(async () => {
  // コンポーネント表示時にチャンネル一覧をフェッチする
  if (channelStore.channels.length === 0) {
    await channelStore.fetchChannels()
  }

  document.addEventListener("click", handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside)
})

function selectChannel(id: number) {
  router.push(`/home/${id}`)
}

function goCreateChannel() {
  router.push("/home/channels/create")
}

function toggleKebabMenu() {
  isKebabOpen.value = !isKebabOpen.value
}

// メニューの外側をクリックしたら閉じる判定ロジック
function handleClickOutside(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (kebabContainerRef.value && !kebabContainerRef.value.contains(target)) {
    isKebabOpen.value = false
  }
}

const displayedChannels = computed(() => {
  return allChannels.value.filter(channel => {
    if (showArchived.value) {
      // 「アーカイブを表示」の時は、通常(0)もアーカイブ(1)もすべて表示
      return channel.status === 0 || channel.status === 1
    } else {
      // 「アーカイブを非表示」の時は、通常(0)のチャンネルのみ表示
      return channel.status === 0
    }
  })
})

function toggleArchivedChannels() {
  showArchived.value = !showArchived.value
  localStorage.setItem("showArchivedChannels", String(showArchived.value))
  isKebabOpen.value = false
}

</script>