<!-- src/components/ChannelInfoModal.vue -->
<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <h2>チャンネル情報</h2>
      
      <div class="info-box">
        <p>チャンネル名 : {{ channel.name }}</p>
        <p>チャンネル作成日 : {{ formatTimestamp(channel.createdAt) }}</p>
      </div>

      <div class="section-header">
        <!-- ★ ストアの members.length を参照して常に最新の人数を表示 -->
        <h3>参加メンバー ({{ memberStore.members.length }}名)</h3>
        <button class="btn btn--secondary" @click="openInviteUserAddModal">+ ユーザーを追加</button>
      </div>

      <div class="member-scroll-box">
        <ul class="member-list">
          <!-- ★ props.members ではなく memberStore.members をループさせる -->
          <li v-for="m in memberStore.members" :key="m.userId">
            ・ {{ m.username }} ({{ formatRole(m.role) }})
          </li>
        </ul>
      </div>

      <div class="channel-management-section">
        <h3>チャンネル管理・操作</h3>
        <div class="management-actions">
          <button class="btn btn--danger" @click="$emit('leave')">チャンネルから退出する</button>
          <button class="btn btn--danger" @click="openInviteEditModal">チャンネルを編集する</button>
          <button class="btn btn--danger" @click="$emit('archive')">{{ channel.status === 0 ? 'アーカイブ化する' : 'アーカイブ化解除する' }}</button>
          <button v-if="authStore.isAdmin" class="btn btn--danger" @click="$emit('delete')">
            チャンネルを削除する <span class="note"></span>
          </button>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn--secondary" @click="$emit('close')">閉じる</button>
      </div>

      <!-- ユーザー追加サブモーダル（チェックボックス形式） -->
      <div v-if="showInviteUserAddModal" class="sub-modal-overlay">
        <div class="sub-modal-content">
          <h3>追加するメンバーを選択</h3>
          
          <div class="user-checkbox-list">
            <label v-for="u in availableUsers" :key="u.userId" class="checkbox-item">
              <input 
                type="checkbox" 
                :value="u.userId" 
                v-model="selectedUserIds"
              />
              {{ u.username }}
            </label>
          </div>

          <div class="sub-modal-actions">
            <button class="btn btn--primary" @click="submitInvite">決定</button>
            <button class="btn btn--secondary" @click="closeInviteUserAddModal">キャンセル</button>
          </div>
        </div>
      </div>

      <!-- チャンネル編集サブモーダル -->
      <div v-if="showInviteEditModal" class="sub-modal-overlay">
        <div class="sub-modal-content">
          <p>チャンネルid : {{ channel.id }}</p>
          <p>チャンネル名 : {{ channel.name }}</p>
          <p>チャンネルステータス : {{ channel.status }}</p>
          <p>チャンネル作成日 : {{ formatTimestamp(channel.createdAt) }}</p>
          <!-- 新しいチャンネル名入力 -->
          <div class="form-group">
            <label class="form-label">新しいチャンネル名</label>
            <input
              type="text"
              v-model="newChannelName"
              class="form-input"
              placeholder="例: general"
              required
            />
          </div>
          <div class="sub-modal-actions">
            <button class="btn btn--primary" @click="submitEdit">作成</button>
            <button class="btn btn--secondary" @click="closeInviteEditModal">キャンセル</button>
          </div>
        </div>
      </div>

    </div>
  </div>
  <AlertDialog
    :visible="visible"
    :title="title"
    :message="message"
    :type="type"
    @confirm="confirm"
    @cancel="cancel"
  />
</template>

<script setup lang="ts">
import { ref, computed } from "vue"
import { useAuthStore } from "@/stores/useAuthStore"
import { useMemberStore } from "@/stores/useMemberStore"
import { useChannelStore } from "@/stores/useChannelStore"
import AlertDialog from "@/components/AlertDialog.vue"
import { useAlertDialog } from "@/composables/useAlertDialog"

const {
  visible,
  title,
  message,
  type,
  confirm,
  cancel,
  showSuccess,
  //showWarning,
  showError
} = useAlertDialog()

const props = defineProps<{
  channel: any
  members: any[] // ※親との互換性のために残してもOKですが、内部ではストアを使います
  allUsers: any[]
  channelId: number
}>()

const emit = defineEmits(["close", "leave", "edit", "archive", "delete", "memberUpdated"])

const authStore = useAuthStore()
const memberStore = useMemberStore() // ★ Piniaストアを利用
const channelStore = useChannelStore()

const showInviteUserAddModal = ref(false)
const selectedUserIds = ref<number[]>([])

const showInviteEditModal = ref(false)
const newChannelName = ref("")


// まだチャンネルに参加していないユーザーを抽出
// ★ store の members を基準に判定するように変更
const availableUsers = computed(() => {
  const memberIds = new Set(memberStore.members.map(m => m.userId))
  return props.allUsers.filter(u => !memberIds.has(u.userId))
})

// ユーザー追加モーダルの表示非表示
function openInviteUserAddModal() {
  selectedUserIds.value = []
  showInviteUserAddModal.value = true
}

function closeInviteUserAddModal() {
  showInviteUserAddModal.value = false
}

// 編集モーダルの表示非表示
function openInviteEditModal() {
  selectedUserIds.value = []
  showInviteEditModal.value = true
}

function closeInviteEditModal() {
  showInviteEditModal.value = false
}

// 日時フォーマット
function formatTimestamp(timestamp: string) {
  if (!timestamp) return ""
  const replaced = timestamp.replace("T", " ")
  const parts = replaced.split(":")
  if (parts.length >= 2) {
    return `${parts[0]}:${parts[1]}`
  }
  return replaced
}

// 権限変換（0なら管理者、1なら一般ユーザーに変換）
function formatRole(role: any) {
  switch (role) {
    case 0:
    case '0':
    case 'admin':
    case 'administrator':
    case '管理者':
      return '管理者'
    case 1:
    case '1':
    case 'user':
    case 'general':
    case '一般ユーザー':
      return '一般ユーザー'
    default:
      return role
  }
}

// ユーザー一括招待
async function submitInvite() {
  if (selectedUserIds.value.length === 0) {
    showError("ユーザー追加エラー", "ユーザーを選択してください")
    return
  }

  try {
    for (const userId of selectedUserIds.value) {
      await memberStore.addMember(props.channelId, userId)
    }
    // ストア側で fetchMembers を呼ぶか、WebSocketで自動追加されるため再取得は任意
    await memberStore.fetchMembers(props.channelId)
    showSuccess("ユーザー追加に成功しました", "選択したユーザーを追加しました")
    closeInviteUserAddModal()
    emit("memberUpdated")
  } catch (e: any) {
    showError("ユーザー追加エラー", e.message || "招待に失敗しました")
  }
}

// チャンネル編集
async function submitEdit() {
  if (newChannelName.value.length === 0) {
    showError("チャンネル編集エラー", "チャンネル名を入力してください")
    return
  }

  try {
    // 1. チャンネル編集
    const editChannel = await channelStore.editChannel(props.channelId, newChannelName.value, props.channel.status)

    showSuccess("チャンネル編集に成功しました", `チャンネル名を「${editChannel.name}」に変更しました!`)

    closeInviteEditModal();
    channelStore.applyWebSocketEvent("channel_updated");
  } catch (e: any) {
    showError("チャンネル編集エラー", e.message || "作成に失敗しました")
  }

}

</script>