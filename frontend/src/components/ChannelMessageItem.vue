<!-- src/components/ChannelMessageItem.vue -->
<template>
  <li class="channel-detail__message-item" :data-message-id="message.id">
    <div class="message__avatar" :style="{ backgroundColor: getUserColor(message.userId) }">
      {{ message.username.charAt(0).toUpperCase() }}
    </div>

    <div class="message__body">
      <div class="message__header">
        <span class="message__username">{{ message.username }}</span>
        <span class="message__time">{{ formatTimestamp(message.createdAt) }}</span>
      </div>

      <div class="message__content">
        <textarea
          v-if="isEditing"
          v-model="localEditContent"
          class="message__edit-textarea"
          maxlength="1000"
        ></textarea>

        <div 
          v-else 
          class="message__formatted-content" 
          v-html="renderFormattedContent(message.content, allUsers, members, message.mentionedUserIds)"
          @mouseover="$emit('content-mouseover', $event)"
          @mouseout="$emit('content-mouseout', $event)"
        ></div>
        <MessageAttachments
          v-if="message.attachments?.length > 0"
          :attachments="[...message.attachments]"
          :message-user-id="message.userId"
          :current-user-id="currentUserId"
          :is-admin="isAdmin"
          @preview="openPreview"
          @download="downloadFile"
          @delete="deleteFile"
          @info="showInfo"
        />
        <AttachmentPreviewModal
          v-if="previewAttachment"
          :attachment="previewAttachment"
          @close="closePreview"
        />

      </div>

      <!-- リアクションバッジ一覧エリア -->
      <MessageReactions
        v-if="channel.status==0"
        :message-id="message.id"
        :reactions="message.reactions"
        :current-user-id="currentUserId"
        @toggle-reaction="(msgId, emoji) => $emit('toggle-reaction', msgId, emoji)"
      />

      <!-- Slack風スレッド返信件数表示エリア -->
      <div class="message-thread-summary" v-if="message.replyCount > 0">
        <button class="thread-summary-btn" @click="$emit('open-thread', message)">
          <span class="thread-summary-icon">💬</span>
          <span class="thread-summary-text">{{ message.replyCount }}件の返信</span>
        </button>
      </div>

      <!-- 編集中の場合のアクション -->
      <div class="message__actions" v-if="isEditing">
        <button @click="$emit('save-edit')" class="btn">保存</button>
        <button @click="$emit('cancel-edit')" class="btn btn--secondary">キャンセル</button>
      </div>

      <!-- 通常時：ホバーアクションエリア -->
      <div class="message-hover-actions" v-else-if="channel.status==0">
        <button class="action-btn emoji-trigger-btn" @click="$emit('open-emoji-picker', message.id)" title="リアクションを追加">
          😀
        </button>

        <!-- hideReplyButtonがtrueの場合はスレッド内の返信とみなし、返信ボタンを非表示にする -->
        <button 
          v-if="!hideReplyButton" 
          class="action-btn reply-btn" 
          @click="$emit('open-thread', message)"
        >
          返信
        </button>

        <div 
          class="kebab-menu-container" 
          v-if="message.userId === currentUserId || isAdmin"
        >
          <button class="kebab-btn" @click="$emit('toggle-kebab', message.id)">⋮</button>
          
          <div class="kebab-dropdown" v-if="activeKebabId === message.id">
            <button 
              v-if="message.userId === currentUserId"
              @click="$emit('open-edit', message); $emit('close-kebab')"
              class="kebab-dropdown__item"
            >
              編集
            </button>
            <button 
              v-if="message.userId === currentUserId || isAdmin"
              @click="$emit('delete-message', message.id); $emit('close-kebab')"
              class="kebab-dropdown__item kebab-dropdown__item--danger"
            >
              削除
            </button>
          </div>
        </div>
      </div>

    </div>
  </li>
  <AttachmentInfoDialog
    v-if="
      showInfoDialog &&
      selectedAttachment
    "
    :attachment="selectedAttachment"
    @close="close"
  />
  
</template>

<script setup lang="ts">
import { computed } from "vue"
import { getUserColor } from "@/utils/Color"
import { formatTimestamp } from "@/utils/DateTime"
import { renderFormattedContent } from "@/utils/Markdown"
import { downloadFile, deleteFile } from "@/utils/File"
import { useAttachmentPreview } from "@/composables/useAttachmentPreview"
import { useAttachmentInfo } from "@/composables/useAttachmentInfo"
import MessageReactions from "@/components/MessageReactions.vue"
import MessageAttachments from "@/components/MessageAttachments.vue"
import AttachmentPreviewModal from "@/components/AttachmentPreviewModal.vue"
import AttachmentInfoDialog from "@/components/AttachmentInfoDialog.vue"

import "highlight.js/styles/github-dark.css"

const { previewAttachment, openPreview, closePreview } = useAttachmentPreview()
const { selectedAttachment, showInfoDialog, showInfo, close } = useAttachmentInfo()

const props = defineProps<{
  channel: any
  message: any
  currentUserId: number | undefined
  isAdmin: boolean
  editingMessage: any
  editContent: string
  activeKebabId: number | null
  allUsers: any[]
  members: any[]
  hideReplyButton?: boolean // 追加：スレッド内での返信ボタン非表示制御フラグ
}>()

const emit = defineEmits<{
  (e: 'toggle-reaction', messageId: number, emoji: string): void
  (e: 'open-thread', message: any): void
  (e: 'open-emoji-picker', messageId: number): void
  (e: 'toggle-kebab', messageId: number): void
  (e: 'close-kebab'): void
  (e: 'open-edit', message: any): void
  (e: 'delete-message', messageId: number): void
  (e: 'save-edit'): void
  (e: 'cancel-edit'): void
  (e: 'content-mouseover', event: MouseEvent): void
  (e: 'content-mouseout', event: MouseEvent): void
  (e: 'update:editContent', value: string): void
}>()

const isEditing = computed(() => props.editingMessage?.id === props.message.id)

const localEditContent = computed({
  get: () => props.editContent,
  set: (val) => emit('update:editContent', val)
})


// function downloadThumbnail(att: Attachment) {
//   const link = document.createElement("a")
//   link.href = `${API_BASE + ":8080"}${att.thumbnailUrl}`
//   console.log("Downloading thumbnail:", link.href, "as", att.originalFileName)
//   link.download = att.originalFileName + "_thumb.jpg"
//   link.click()
// }



</script>