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
          v-html="renderFormattedContent(message.content)"
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
          @info="showAttachmentInfo"
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
    @close="
      showInfoDialog = false
    "
  />
  
</template>

<script setup lang="ts">
import { ref, computed } from "vue"
import { getUserColor } from "@/utils/Utils"
import MessageReactions from "@/components/MessageReactions.vue"
import MessageAttachments from "@/components/MessageAttachments.vue"
import { useMessageStore } from "@/stores/useMessageStore"
import type { Attachment } from "@/stores/useMessageStore"
import AttachmentPreviewModal from "@/components/AttachmentPreviewModal.vue"
import AttachmentInfoDialog from "@/components/AttachmentInfoDialog.vue"

import MarkdownIt from "markdown-it"
import hljs from "highlight.js"
import DOMPurify from "dompurify"

import "highlight.js/styles/github-dark.css"

import { useAlertDialog } from "@/composables/useAlertDialog"

const alertDialog = useAlertDialog()
const { showSuccess, showWarning, showError } = alertDialog


const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: false,

  highlight(code, language) {
    if (language && hljs.getLanguage(language)) {
      return hljs.highlight(code, {
        language
      }).value
    }

    return ""
  }
})

const defaultLinkOpen =
  md.renderer.rules.link_open ??
  ((tokens, idx, options, _env, self) =>
    self.renderToken(tokens, idx, options))

md.renderer.rules.link_open = (
  tokens,
  idx,
  options,
  env,
  self
) => {
  tokens[idx].attrSet("target", "_blank")
  tokens[idx].attrSet("rel", "noopener noreferrer")

  return defaultLinkOpen(
    tokens,
    idx,
    options,
    env,
    self
  )
}

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
const messageStore = useMessageStore()

const localEditContent = computed({
  get: () => props.editContent,
  set: (val) => emit('update:editContent', val)
})

function renderFormattedContent(content: string) {
  if (!content) return ""

  // 1. MarkdownをHTMLへ変換
  const renderedHtml = md.render(content)

  // 2. 危険なHTMLを除去
  const sanitizedHtml = DOMPurify.sanitize(renderedHtml)

  // 3. メンション表示を付与
  const container = document.createElement("div")
  container.innerHTML = sanitizedHtml

  applyMentionFormatting(container)

  return container.innerHTML
}

function applyMentionFormatting(container: HTMLElement) {
  const mentionedUserIds: number[] =
    props.message.mentionedUserIds ?? []

  if (mentionedUserIds.length === 0) {
    return
  }

  const walker = document.createTreeWalker(
    container,
    NodeFilter.SHOW_TEXT
  )

  const textNodes: Text[] = []

  while (walker.nextNode()) {
    const node = walker.currentNode as Text

    const parent = node.parentElement

    // コード・リンク内はメンション化しない
    if (
      parent?.closest("code") ||
      parent?.closest("pre") ||
      parent?.closest("a")
    ) {
      continue
    }

    textNodes.push(node)
  }

  for (const textNode of textNodes) {
    replaceMentionsInTextNode(
      textNode,
      mentionedUserIds
    )
  }
}

function replaceMentionsInTextNode(
  textNode: Text,
  mentionedUserIds: number[]
) {
  const text = textNode.textContent ?? ""

  const mentionRegex = /@([^\s]+)/g

  let lastIndex = 0
  let match: RegExpExecArray | null

  const fragment = document.createDocumentFragment()

  while ((match = mentionRegex.exec(text)) !== null) {
    const fullMatch = match[0]
    const username = match[1]

    const user =
      props.allUsers.find(
        (u: any) => u.username === username
      ) ||
      props.members.find(
        (m: any) => m.username === username
      )

    if (!user) {
      continue
    }

    const userId = Number(
      user.userId ?? user.id
    )

    const isMentioned =
      mentionedUserIds.some(
        (id: number) =>
          Number(id) === userId
      )

    if (!isMentioned) {
      continue
    }

    // メンションより前の通常文字
    fragment.append(
      document.createTextNode(
        text.slice(lastIndex, match.index)
      )
    )

    // メンション部分
    const span = document.createElement("span")

    span.className = "mention-pill"
    span.dataset.username = username
    span.textContent = fullMatch

    fragment.append(span)

    lastIndex =
      match.index + fullMatch.length
  }

  // メンションが1件もなかった場合
  if (lastIndex === 0) {
    return
  }

  // 最後の通常文字
  fragment.append(
    document.createTextNode(
      text.slice(lastIndex)
    )
  )

  textNode.replaceWith(fragment)
}

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

const previewAttachment = ref<Attachment | null>(null)

function openPreview(att: Attachment) {
  console.log("Opening preview for attachment:", att)
  previewAttachment.value = att
}

function closePreview() {
  previewAttachment.value = null
}

const API_BASE = import.meta.env.VITE_API_BASE_URL

function downloadFile(att: Attachment) {
  const link = document.createElement("a")
  link.href = `${API_BASE + ":8080"}${"/files/download/" + att.id}`
  link.download = att.originalFileName
  link.click()
}

function deleteFile(att: Attachment) {
  const result = showWarning("ファイル削除", `本当に「${att.originalFileName}」を削除しますか？`)
  if (!result) {
    messageStore.deleteAttachment(att.id).then(() => {
      showSuccess("ファイル削除に成功しました", `「${att.originalFileName}」を削除しました。`)
    }).catch(() => {
      showError("ファイル削除エラー", `「${att.originalFileName}」の削除に失敗しました。`)
    })
  }
}

const selectedAttachment = ref<any | null>(null)
const showInfoDialog = ref(false)

function showAttachmentInfo(att: Attachment) {
  selectedAttachment.value = att
  showInfoDialog.value = true
}

// function downloadThumbnail(att: Attachment) {
//   const link = document.createElement("a")
//   link.href = `${API_BASE + ":8080"}${att.thumbnailUrl}`
//   console.log("Downloading thumbnail:", link.href, "as", att.originalFileName)
//   link.download = att.originalFileName + "_thumb.jpg"
//   link.click()
// }



</script>