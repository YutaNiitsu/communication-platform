<!-- src/components/ChannelThreadSidebar.vue -->
<template>
  <aside class="thread-sidebar">
    <div class="thread-sidebar__header">
      <span class="thread-title">スレッド</span>
      <button class="thread-close-btn" @click="$emit('close')">閉じる</button>
    </div>

    <div class="thread-sidebar__content">
      <div class="thread-parent-message">
        <div class="message__avatar" :style="{ backgroundColor: getUserColor(activeThreadMessage.userId) }">
          {{ activeThreadMessage.username.charAt(0).toUpperCase() }}
        </div>
        <div class="message__body">
          <div class="message__header">
            <span class="message__username">{{ activeThreadMessage.username }}</span>
            <span class="message__time">{{ formatTimestamp(activeThreadMessage.createdAt) }}</span>
          </div>
          <div class="message__content" v-html="renderFormattedContent(activeThreadMessage.content)"></div>
        </div>
      </div>

      <hr class="thread-divider" />

      <!-- スレッド返信一覧（ChannelMessageItem を活用し、返信ボタンのみ非表示に設定） -->
      <ul class="thread-reply-list channel-detail__message-list">
        <ChannelMessageItem
          v-for="reply in threadReplies"
          :key="reply.id"
          :channel="channel"
          :message="reply"
          :current-user-id="authStore.user?.userId"
          :is-admin="authStore.isAdmin"
          :editing-message="editingMessage"
          v-model:edit-content="localEditContent"
          :active-kebab-id="activeKebabId"
          :all-users="allUsers"
          :members="members"
          :hide-reply-button="true"
          @toggle-reaction="(msgId, emoji) => $emit('toggle-reaction', msgId, emoji)"
          @open-thread="() => {}"
          @open-emoji-picker="(msgId) => $emit('open-emoji-picker', msgId)"
          @toggle-kebab="(msgId) => $emit('toggle-kebab', msgId)"
          @close-kebab="() => $emit('close-kebab')"
          @open-edit="(msg) => $emit('open-edit', msg)"
          @delete-message="(msgId) => $emit('delete-message', msgId)"
          @save-edit="() => $emit('save-edit')"
          @cancel-edit="() => $emit('cancel-edit')"
          @content-mouseover="(e) => $emit('content-mouseover', e)"
          @content-mouseout="(e) => $emit('content-mouseout', e)"
        />
      </ul>
    </div>

    <!-- スレッド返信フォーム -->
    <form @submit.prevent="handleSubmit" class="thread-reply__form">
      <div class="message-input-wrapper">
        <textarea 
          v-model="localNewMessage" 
          placeholder="返信する" 
          required 
          maxlength="1000"
        ></textarea>

        <div class="input-inner-controls">
          <button
            type="button"
            class="btn-template-trigger"
            @click.stop="toggleThreadTemplateDropdown"
          >
            テンプレート ▾
          </button>

          <div v-if="showThreadTemplateDropdown" class="template-dropdown-menu" ref="threadDropdownRef">
            <ul class="template-dropdown__list" v-if="templateList.length > 0">
              <li
                v-for="tpl in templateList"
                :key="tpl.id"
                class="template-dropdown__item"
                @click="selectTemplate(tpl.content)"
              >
                <span class="template-item-title">{{ tpl.title }}</span>
                <span v-if="tpl.isShared" class="template-badge-shared">共有</span>
              </li>
            </ul>
            <div v-else class="template-dropdown__empty">
              利用可能なテンプレートがありません
            </div>
            <div class="template-dropdown__footer" @click="$emit('go-template-management')">
              + テンプレートを管理・新規作成
            </div>
          </div>
        </div>
      </div>

      <div class="thread-reply__actions">
        <button type="submit" class="btn btn--submit">送信</button>
      </div>
    </form>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue"
import { getUserColor } from "@/utils/Utils"
import ChannelMessageItem from "@/components/ChannelMessageItem.vue"
import { useAuthStore } from "@/stores/useAuthStore"

const authStore = useAuthStore()

const props = defineProps<{
  activeThreadMessage: any
  threadReplies: any[]
  templateList: any[]
  editingMessage: any
  editContent: string
  activeKebabId: number | null
  allUsers: any[]
  members: any[]
  channel: any
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submit-reply', content: string): void
  (e: 'go-template-management'): void
  (e: 'toggle-reaction', messageId: number, emoji: string): void
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

const localNewMessage = ref("")
const showThreadTemplateDropdown = ref(false)
const threadDropdownRef = ref<HTMLElement | null>(null)

const localEditContent = computed({
  get: () => props.editContent,
  set: (val) => emit('update:editContent', val)
})

function toggleThreadTemplateDropdown() {
  showThreadTemplateDropdown.value = !showThreadTemplateDropdown.value
}

function selectTemplate(content: string) {
  localNewMessage.value = content
  showThreadTemplateDropdown.value = false
}

function handleSubmit() {
  if (!localNewMessage.value.trim()) return
  emit('submit-reply', localNewMessage.value)
  localNewMessage.value = ""
}

function handleClickOutside(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (
    showThreadTemplateDropdown.value && 
    threadDropdownRef.value && 
    !threadDropdownRef.value.contains(target) && 
    !target.closest('.thread-reply__form .btn-template-trigger')
  ) {
    showThreadTemplateDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

function formatTimestamp(timestamp: string) {
  if (!timestamp) return ""
  let formattedString = timestamp
  if (!formattedString.endsWith("Z") && !formattedString.includes("+") && formattedString.lastIndexOf("-") > 10) {
    formattedString += "Z"
  }
  const date = new Date(formattedString)
  if (isNaN(date.getTime())) return timestamp

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}/${month}/${day} ${hours}:${minutes}`
}

function renderFormattedContent(content: string) {
  if (!content) return ""
  return content.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}
</script>