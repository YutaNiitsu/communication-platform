<!-- src/components/ChannelCreateModal.vue -->
<template>
  <div class="modal-overlay" @click.self="handleClose">
    <div class="modal-card">
      <h2 class="modal-title">チャンネル作成</h2>

      <form @submit.prevent="submit">
        <!-- チャンネル名入力 -->
        <div class="form-group">
          <label class="form-label">チャンネル名</label>
          <input
            type="text"
            v-model="name"
            class="form-input"
            placeholder="例: general"
            required
          />
        </div>

        <!-- 参加させるユーザー（スクロール可能なチェックボックスリスト） -->
        <div class="form-group">
          <label class="form-label">参加させるユーザー</label>
          <div class="user-list-box">
            <div v-for="u in allUsers" :key="u.userId" class="user-item">
              <label class="checkbox-label" :for="'user-' + u.userId">
                <input
                  type="checkbox"
                  :value="u.userId"
                  v-model="selectedUsers"
                  :id="'user-' + u.userId"
                  class="checkbox-input"
                />
                <span class="checkbox-label-text">
                  {{ u.username }} <span class="user-role">({{ formatRole(u.role) }})</span>
                </span>
              </label>
            </div>
          </div>
        </div>

        <!-- エラー・成功メッセージ -->
        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
        <p v-if="successMessage" class="success-message">{{ successMessage }}</p>

        <!-- アクションボタン（左：作成、右：キャンセル） -->
        <div class="modal-actions">
          <button type="submit" class="btn-primary">
            作成
          </button>
          <button type="button" class="btn-secondary" @click="handleClose">
            キャンセル
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useChannelStore } from "@/stores/useChannelStore"
import { useUserStore } from "@/stores/useUserStore"
import { useAuthStore } from "@/stores/useAuthStore"
import { useMemberStore } from "@/stores/useMemberStore"

const router = useRouter()
const channelStore = useChannelStore()
const userStore = useUserStore()
const authStore = useAuthStore()
const memberStore = useMemberStore()

const name = ref("")
const errorMessage = ref("")
const successMessage = ref("")

// 全ユーザー一覧 ＆ 選択されたユーザーID配列
const allUsers = ref<any[]>([])
const selectedUsers = ref<number[]>([])

onMounted(async () => {
  try {
    allUsers.value = await userStore.fetchAllUsers()
  } catch (e) {
    console.error("ユーザー一覧の取得に失敗しました", e)
    errorMessage.value = "ユーザー一覧の取得に失敗しました"
  }
})

// ロール値（0 または 1）を日本語に変換する関数
function formatRole(role: number): string {
  if (role === 0) {
    return "管理者"
  } else if (role === 1) {
    return "一般ユーザー"
  }
  return "不明"
}

// 作成処理
async function submit() {
  if (!name.value.trim()) {
    errorMessage.value = "チャンネル名を入力してください"
    return
  }

  errorMessage.value = ""
  successMessage.value = ""

  try {
    // 1. チャンネル作成
    const newChannel = await channelStore.createChannel(name.value)

    // 2. 作成者を自動参加
    if (authStore.user?.userId) {
      await memberStore.addMember(newChannel.id, authStore.user.userId)
    }

    // 3. 選択したユーザーを追加で参加させる
    for (const userId of selectedUsers.value) {
      if (authStore.user?.userId !== userId) {
        await memberStore.addMember(newChannel.id, userId)
      }
    }

    successMessage.value = `チャンネル「${newChannel.name}」を作成しました！`

    setTimeout(() => {
      router.back()
    }, 800)
  } catch (e: any) {
    errorMessage.value = e?.message || "作成に失敗しました"
  }
}

// 閉じる・キャンセル処理
const handleClose = () => {
  router.back()
}
</script>