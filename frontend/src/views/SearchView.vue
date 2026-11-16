<!-- src/views/SearchView.vue （ファイル名に合わせて調整してください） -->
<template>
  <div class="search-layout">
    <div class="search-sidebar">
      <h2>検索条件</h2>

      <form @submit.prevent="submit" class="search__form">
        <div class="search-field">
          <label>キーワード</label>
          <input type="text" v-model="query" placeholder="キーワードを入力" />
        </div>

        <div class="search-field">
          <label>チャンネル名</label>
          <input
            type="text"
            v-model="channelInput"
            list="channel-options"
            placeholder="選択またはチャンネル名を入力"
          />
          <datalist id="channel-options">
            <option
              v-for="c in channelStore.channels"
              :key="c.id"
              :value="c.name"
            ></option>
          </datalist>
        </div>

        <div class="search-field">
          <label>ユーザー名</label>
          <input
            type="text"
            v-model="userInput"
            list="user-options"
            placeholder="選択またはユーザー名を入力"
          />
          <datalist id="user-options">
            <!-- キャメルケース(userId, username)に修正 -->
            <option
              v-for="u in userStore.users"
              :key="u.userId"
              :value="u.username"
            ></option>
          </datalist>
        </div>

        <div class="search-field">
          <label>開始日</label>
          <input type="date" v-model="dateFrom" />
        </div>

        <div class="search-field">
          <label>終了日</label>
          <input type="date" v-model="dateTo" />
        </div>

        <button type="submit" class="btn btn--primary" :disabled="searchStore.loading">
          {{ searchStore.loading ? "検索中..." : "検索" }}
        </button>
      </form>
    </div>

    <div class="search-main">
      <h2>検索結果</h2>

      <p v-if="searchStore.error" class="error-message">
        {{ searchStore.error }}
      </p>

      <ul v-else-if="searchStore.hasResults" class="search__result-list">
        <!-- キャメルケース(channelId, createdAt)に修正 -->
        <li 
          v-for="r in searchStore.results" 
          :key="r.id" 
          class="search__result-item clickable-item"
          @click="jumpToMessage(r.channelId, r.id)"
        >
          <div class="search__description">{{ r.content }}</div>
          <div class="search__meta">
            チャンネル：{{ getChannelName(r.channelId) }} /
            投稿者：{{ getUserName(r.userId) }} /
            投稿日時：{{ formatDate(r.createdAt) }}
          </div>
        </li>
      </ul>

      <p v-else-if="searched && !searchStore.loading" class="no-result">
        該当するメッセージはありません
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useSearchStore } from "@/stores/useSearchStore"
import { useUserStore } from "@/stores/useUserStore"
import { useChannelStore } from "@/stores/useChannelStore"
import { useAuthStore } from "@/stores/useAuthStore"

const router = useRouter()
const searchStore = useSearchStore()
const userStore = useUserStore()
const channelStore = useChannelStore()
const authStore = useAuthStore()

const query = ref("")
const channelInput = ref("")
const userInput = ref("")
const dateFrom = ref<string | null>(null)
const dateTo = ref<string | null>(null)

const searched = ref(false)

onMounted(async () => {
  await Promise.all([
    channelStore.fetchChannels(),
    userStore.fetchAllUsers()
  ])
})

async function submit() {
  // キャメルケース(userId)に対応しつつ、念のためフォールバックも用意
  const requestUserId = authStore.user?.userId ?? (authStore.user as any)?.user_id

  if (!requestUserId) {
    searchStore.error = "ログインユーザー情報が取得できません。"
    return
  }

  // 入力されたチャンネル名から ID を検索
  let targetChannelId: number | null = null
  if (channelInput.value.trim()) {
    const foundChannel = channelStore.channels.find(
      c => c.name === channelInput.value.trim()
    )
    targetChannelId = foundChannel ? foundChannel.id : null
  }

  // 入力されたユーザー名から ID を検索（userId を参照）
  let targetUserId: number | null = null
  if (userInput.value.trim()) {
    const foundUser = userStore.users.find(
      u => u.username === userInput.value.trim()
    )
    // キャメルケース(userId)またはスネークケースに対応
    targetUserId = foundUser ? (foundUser.userId ?? (foundUser as any).user_id) : null
  }

  searchStore.setQuery(query.value)
  searchStore.setChannelIdFilter(targetChannelId)
  searchStore.setUserFilter(targetUserId)
  searchStore.setDateRange(dateFrom.value, dateTo.value)

  await searchStore.search(requestUserId)
  searched.value = true
}

function jumpToMessage(channelId: number, messageId: number) {
  router.push({
    path: `/home/${channelId}`,
    query: { highlightMessageId: String(messageId) }
  })
}

function formatDate(dateString: string) {
  if (!dateString) return ""
  const replaced = dateString.replace("T", " ")
  const parts = replaced.split(":")
  if (parts.length >= 2) {
    return `${parts[0]}:${parts[1]}`
  }
  return replaced
}

// ユーザー名の逆引き（userId を参照）
function getChannelName(id: number) {
  const ch = channelStore.channels.find(c => c.id === id)
  return ch ? ch.name : `チャンネルID: ${id}`
}

function getUserName(id: number) {
  const u = userStore.users.find(item => {
    const uid = item.userId ?? (item as any).user_id
    return Number(uid) === Number(id)
  })
  return u ? u.username : `ユーザーID: ${id}`
}
</script>