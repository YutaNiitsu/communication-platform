<template>
  <div class="message-reactions" v-if="reactions && reactions.length > 0">
    <button
      v-for="reaction in reactions"
      :key="`${messageId}-${reaction.emoji}`"
      class="reaction-badge"
      :class="{ 'reacted': reaction.users.includes(currentUserId) }"
      :aria-label="reactionTooltips[`${messageId}-${reaction.emoji}`] || '読み込み中...'"
      @click="$emit('toggle-reaction', messageId, reaction.emoji)"
      @mouseover="handleReactionMouseOver(messageId, reaction.emoji, reaction.users)"
    >
      <span class="reaction-emoji">{{ reaction.emoji }}</span>
      <span class="reaction-count">{{ reaction.users.length }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue"
import { useUserStore } from "@/stores/useUserStore"

const props = defineProps<{
  messageId: number
  reactions: any[]
  currentUserId: number | undefined
}>()

defineEmits<{
  (e: 'toggle-reaction', messageId: number, emoji: string): void
}>()

const userStore = useUserStore()
const reactionTooltips = ref<Record<string, string>>({})

async function handleReactionMouseOver(messageId: number, emoji: string, userIds: number[]) {
  const key = `${messageId}-${emoji}`

  if (!reactionTooltips.value[key]) {
    try {
      const nameList = await Promise.all(
        userIds.map(async (id) => {
          if (props.currentUserId && Number(id) === Number(props.currentUserId)) {
            return "あなた"
          }
          const user = await userStore.fetchUserById(id)
          return user ? `${user.username}さん` : `ユーザー_${id}さん`
        })
      )
      
      reactionTooltips.value[key] = `${nameList.join(', ')} がリアクションしました`
    } catch (e) {
      reactionTooltips.value[key] = 'リアクションユーザーの取得に失敗しました'
    }
  }
}
</script>