<!-- src/views/ChannelDetailView.vue -->
<template>
  <div class="channel-detail__wrapper" :class="{ 'has-thread': activeThreadMessage }">
    <!-- メインコンテンツ領域 -->
    <div class="channel-detail__container" v-if="channel">

      <!-- チャンネル情報エリア -->
      <div class="channel-header-bar" @click="toggleInfo">
        <span class="channel-title-badge">{{ channelInitial }}</span>
        <span class="channel-name">{{ channel.name }}</span>
        <span class="channel-info-hint">▼ チャンネル情報</span>
      </div>
      
      <!-- チャンネル情報モーダル -->
      <ChannelInfoModal
        v-if="showInfo"
        :channel="channel"
        :members="members"
        :all-users="allUsers"
        :channel-id="channelId"
        @close="toggleInfo"
        @leave="leaveChannel"
        @edit="editChannel"
        @archive="archiveChannel"
        @delete="deleteChannel"
        @memberUpdated="reloadMembers"
      />

      <!-- メッセージ一覧 -->
      <ul class="channel-detail__message-list" :class="{ 'is-channel-archived': channel.status === 1 }" ref="messageList">
        <!-- 過去ログ読み込み中のローディングスピナーUI -->
        <li v-if="isLoadingPast" class="message-list__loading-item">
          <div class="loading-spinner"></div>
          <span>過去のメッセージを読み込み中...</span>
        </li>
        <li v-if="channel.status === 1" class="message-list__archive-banner">
          🔒 このチャンネルはアーカイブされているため、閲覧のみ可能です。
        </li>
        <!-- これ以上過去ログがない場合の案内 -->
        <!-- <li v-if="!hasMorePastMessages && channelMessages.length >= FORM_CONFIG.PAGE_RANGE" class="message-list__empty-hint">
          ✨ これより過去のメッセージはありません
        </li> -->
        <ChannelMessageItem
          v-for="m in channelMessages"
          :key="m.id + '-' + m.attachments?.length"
          :channel="channel"
          :message="m"
          :current-user-id="authStore.user?.userId"
          :is-admin="authStore.isAdmin"
          :editing-message="editingMessage"
          v-model:edit-content="editContent"
          :active-kebab-id="activeKebabId"
          :all-users="allUsers"
          :members="members"
          @toggle-reaction="toggleReaction"
          @open-thread="openThread"
          @open-emoji-picker="openEmojiPicker"
          @toggle-kebab="toggleKebab"
          @close-kebab="closeKebab"
          @open-edit="openEdit"
          @delete-message="deleteMessage"
          @save-edit="saveEdit"
          @cancel-edit="cancelEdit"
          @content-mouseover="handleContentMouseOver"
          @content-mouseout="handleContentMouseOut"
        />
      </ul>

      <!-- メッセージ投稿フォーム -->
      <form v-if="channel.status==0" @submit.prevent="submitMessage" class="channel-detail__form" :style="formStyles">
        <div class="message-input-wrapper">
          <div class="markdown-toolbar">
            <button
              type="button"
              class="markdown-toolbar__button"
              title="太字"
              @click="applyMarkdownFormat('bold')"
            >
              <strong>B</strong>
            </button>

            <button
              type="button"
              class="markdown-toolbar__button"
              title="斜体"
              @click="applyMarkdownFormat('italic')"
            >
              <em>I</em>
            </button>

            <button
              type="button"
              class="markdown-toolbar__button"
              title="取り消し線"
              @click="applyMarkdownFormat('strike')"
            >
              <s>S</s>
            </button>

            <button
              type="button"
              class="markdown-toolbar__button"
              title="リンク"
              @click="applyMarkdownFormat('link')"
            >
              🔗
            </button>

            <button
              type="button"
              class="markdown-toolbar__button"
              title="番号付きリスト"
              @click="applyMarkdownFormat('orderedList')"
            >
              1.
            </button>

            <button
              type="button"
              class="markdown-toolbar__button"
              title="箇条書き"
              @click="applyMarkdownFormat('bulletList')"
            >
              •
            </button>

            <button
              type="button"
              class="markdown-toolbar__button"
              title="引用"
              @click="applyMarkdownFormat('quote')"
            >
              ❝
            </button>

            <button
              type="button"
              class="markdown-toolbar__button"
              title="インラインコード"
              @click="applyMarkdownFormat('inlineCode')"
            >
              &lt;/&gt;
            </button>

            <button
              type="button"
              class="markdown-toolbar__button"
              title="コードブロック"
              @click="applyMarkdownFormat('codeBlock')"
            >
              <span>{ }</span>
            </button>

          </div>

          <div v-if="showMentionPopup" class="mention-dropdown-menu" ref="mentionDropdownRef">
            <ul class="mention-dropdown__list">
              <li
                v-for="user in mentionCandidates"
                :key="user.userId"
                class="mention-dropdown__item"
                @click="selectMentionUser(user)"
              >
                <span class="mention-username">{{ user.username }}</span>
                <span v-if="user.isNotMember" class="mention-note">チャンネル未参加</span>
              </li>
            </ul>
          </div>
          <div
            class="drop-area"
            :class="{ dragging: isDragging }"
            @dragenter="onDragEnter"
            @dragleave="onDragLeave"
            @dragover="onDragOver"
            @drop="onDrop"
          >
            <textarea 
              ref="textareaRef"
              v-model="newMessage" 
              @beforeinput="handleBeforeInput"
              @input="handleInput"
              @click="handleMentionCursorMove"
              @keyup="handleMentionCursorMove"
              @keydown="handleMarkdownEnter"
              placeholder="メッセージを入力" 
              required 
              :maxlength="configStore.config!.message.maxLength"
            ></textarea>
          </div>

          <!-- 送信前添付ファイル一覧 -->
          <div v-if="selectedFiles.length > 0" class="pre-attachment-list">
            <div
              v-for="item in selectedFiles"
              :key="item.name"
              class="pre-attachment-item"
            >
              <!-- 画像はサムネイル -->
              <img
                v-if="item.previewUrl"
                :src="item.previewUrl"
                class="pre-attachment-thumb"
              />

              <!-- PDF / Office / その他は SVG アイコン -->
              <FileTypeIcon
                v-else
                :filename="item.name"
              />

              <!-- ファイル名編集用の input -->
              <input
                :value="item.baseName"
                @input="onFileNameInput($event, item)"
                class="file-name-input"
              />

              <span class="extension">
                {{ item.extension }}
              </span>

              <button class="pre-attachment-remove" @click="removeSelectedFile(item)">
                ☒
              </button>
            </div>
          </div>


          <!-- ＋ボタン（添付・テンプレートメニュー） -->
          <div class="plus-menu-wrapper">
            <button
              type="button"
              class="plus-button"
              @click.stop="togglePlusMenu"
            >
              ＋
            </button>

            <!-- メニュー表示 -->
            <div v-if="showPlusMenu" class="plus-menu">
              <div
                class="plus-menu-item"
                :class="{ active: activePlusItem === 'file' }"
                @click.stop="selectPlusItem('file')"
              >
                ファイルアップロード
              </div>

              <div
                class="plus-menu-item"
                :class="{ active: activePlusItem === 'template' }"
                @click.stop="selectPlusItem('template')"
              >
                テンプレート
              </div>
            </div>
            <div v-if="showTemplateDropdown" class="template-dropdown-menu" ref="dropdownRef">
              <ul class="template-dropdown__list" v-if="templateList.length > 0">
                <li
                  v-for="tpl in templateList"
                  :key="tpl.id"
                  class="template-dropdown__item"
                  @click.stop="selectTemplate(tpl.content)"
                >
                  <span class="template-item-title">{{ tpl.title }}</span>
                  <span v-if="tpl.isShared" class="template-badge-shared">共有</span>
                </li>
              </ul>
              <div v-else class="template-dropdown__empty">
                利用可能なテンプレートがありません
              </div>
              <div class="template-dropdown__footer" @click="goTemplateManagement">
                + テンプレートを管理・新規作成
              </div>
            </div>

          </div>

          <!-- ファイル選択用 input（非表示） -->
          <input
            ref="fileInputRef"
            type="file"
            multiple
            class="hidden-file-input"
            @change="handleFileSelected"
          />

          

          <div class="input-inner-controls">
            <!-- 文字数カウンター -->
            <span 
              class="message-char-counter" 
              :class="{ 'is-error': newMessage.length > configStore.config!.message.maxLength }"
            >
              {{ newMessage.length }} / {{ configStore.config!.message.maxLength }}
            </span>

          </div>
        </div>

        <button type="submit" class="btn btn--submit">送信</button>
      </form>

    </div>

    <!-- 右側：スレッド返信ペイン（ChannelMessageItem 用のプロパティとイベントを統合） -->
    <ChannelThreadSidebar
      v-if="activeThreadMessage && channel && channel.status === 0"
      :active-thread-message="activeThreadMessage"
      :thread-replies="threadReplies"
      :template-list="templateList"
      :editing-message="editingMessage"
      v-model:edit-content="editContent"
      :active-kebab-id="activeKebabId"
      :all-users="allUsers"
      :members="members"
      :channel="channel"
      @close="closeThread"
      @submit-reply="submitThreadReply"
      @go-template-management="goTemplateManagement"
      @toggle-reaction="toggleReaction"
      @open-emoji-picker="openEmojiPicker"
      @toggle-kebab="toggleKebab"
      @close-kebab="closeKebab"
      @open-edit="openEdit"
      @delete-message="deleteMessage"
      @save-edit="saveEdit"
      @cancel-edit="cancelEdit"
      @content-mouseover="handleContentMouseOver"
      @content-mouseout="handleContentMouseOut"
    />

    <div 
      v-if="hoveredUser && channel && channel.status === 0" 
      class="mention-profile-card" 
      :style="{ top: popupPosition.top + 'px', left: popupPosition.left + 'px' }"
      @mouseover="cancelHidePopup"
      @mouseout="hidePopup"
    >
      <div class="profile-card__header">
        <div class="profile-card__avatar" :style="{ backgroundColor: getUserColor(hoveredUser.userId) }">
          {{ hoveredUser.username.charAt(0).toUpperCase() }}
        </div>
        <div class="profile-card__info">
          <span class="profile-card__name">{{ hoveredUser.username }}</span>
          <span class="profile-card__role" :class="{ 'is-admin': hoveredUser.role === 0 }">
            {{ hoveredUser.role === 0 ? '管理者' : '一般ユーザー' }}
          </span>
        </div>
      </div>
    </div>

    <div v-if="activeEmojiMessageId !== null && channel && channel.status === 0" class="emoji-picker-overlay" @click.self="closeEmojiPicker">
      <div class="emoji-picker-container">
        <Picker
          :data="emojiIndex"
          :i18n="emojiI18n"
          @select="onEmojiSelect"
          set="apple"
          theme="light"
          locale="ja"
        />
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
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useChannelStore } from "@/stores/useChannelStore"
import { useMessageStore } from "@/stores/useMessageStore"
import { useMemberStore } from "@/stores/useMemberStore"
import { useUserStore } from "@/stores/useUserStore"
import { useAuthStore } from "@/stores/useAuthStore"
import { useMessageTemplateStore } from "@/stores/useMessageTemplateStore"
import ChannelInfoModal from "@/components/ChannelInfoModal.vue"
import ChannelMessageItem from "@/components/ChannelMessageItem.vue"
import ChannelThreadSidebar from "@/components/ChannelThreadSidebar.vue"
import AlertDialog from "@/components/AlertDialog.vue"
import { getUserColor } from "@/utils/Utils"
import FileTypeIcon from '@/components/FileTypeIcon.vue'
import { useConfigStore } from "@/stores/useConfigStore"
import { useAlertDialog } from "@/composables/useAlertDialog"

import { Picker, EmojiIndex } from "emoji-mart-vue-fast/src"
import data from "emoji-mart-vue-fast/data/apple.json"
import "emoji-mart-vue-fast/css/emoji-mart.css"

const emojiIndex = new EmojiIndex(data)

const route = useRoute()
const router = useRouter()

const channelStore = useChannelStore()
const messageStore = useMessageStore()
const memberStore = useMemberStore()
const userStore = useUserStore()
const authStore = useAuthStore()
const templateStore = useMessageTemplateStore()
const configStore = useConfigStore()

const channelId = computed(() => Number(route.params.id))
const channel = computed(() => channelStore.currentChannel)

const isMentionMode = ref(false)
const mentionStartIndex = ref<number | null>(null)
const mentionEndIndex = ref<number | null>(null)

const channelInitial = computed(() => {
  if (!channel.value || !channel.value.name) return ""
  return channel.value.name.charAt(0).toUpperCase()
})

const newMessage = ref("")
const editingMessage = ref<any>(null)
const editContent = ref("")
const isNearBottom = ref(true)
const isLoadingPast = ref(false)
const hasMorePastMessages = ref(true)

const hoveredUser = ref<any>(null)
const popupPosition = ref({ top: 0, left: 0 })
let hideTimeout: any = null

type SelectedMention = {
  userId: number
  username: string
  start: number
  end: number
}

const selectedMentions = ref<SelectedMention[]>([])
const previousMessage = ref("")

const {
  visible,
  title,
  message,
  type,
  confirm,
  cancel,
  //showSuccess,
  showWarning,
  showError
} = useAlertDialog()


// ==========================================
// フォームのレイアウト・制限設定
// ==========================================
const FORM_CONFIG = {
  MAX_LINES: 20,          // 最大表示行数
  LINE_HEIGHT: 19.6,      // 1行あたりの高さ (px)
  PADDING_AMOUNT: 16,     // 上下パディングの合計 (px)
}

// 最大高さを自動計算
const maxFormHeight = computed(() => {
  return (FORM_CONFIG.LINE_HEIGHT * FORM_CONFIG.MAX_LINES) + FORM_CONFIG.PADDING_AMOUNT
})

// テンプレート（HTML）にバインドするためのCSS変数オブジェクト
const formStyles = computed(() => ({
  '--max-textarea-height': `${maxFormHeight.value}px`,
  '--textarea-max-length': configStore.config?.message?.maxLength || 500
}))


function handleBeforeInput(event: InputEvent) {
  if (event.isComposing) {
    return
  }

  if (
    event.inputType !== "insertText" ||
    event.data !== "@"
  ) {
    return
  }

  const textarea = textareaRef.value
  if (!textarea) return

  const cursorPosition = textarea.selectionStart

  const previousChar =
    cursorPosition > 0
      ? newMessage.value.charAt(cursorPosition - 1)
      : ""

  if (previousChar && /[A-Za-z0-9_.-]/.test(previousChar)) {
    return
  }

  isMentionMode.value = true
  mentionStartIndex.value = cursorPosition
}


function handleContentMouseOver(event: MouseEvent) {
  const target = (event.target as HTMLElement).closest('.mention-pill')
  if (target) {
    if (hideTimeout) clearTimeout(hideTimeout)
    const username = target.getAttribute('data-username')
    if (!username) return

    const found = allUsers.value.find((u: any) => u.username === username) ||
                  memberStore.members.find((m: any) => m.username === username)
    if (found) {
      const rect = target.getBoundingClientRect()
      popupPosition.value = {
        top: rect.bottom + window.scrollY + 4,
        left: rect.left + window.scrollX
      }
      hoveredUser.value = found
    }
  }
}

function handleContentMouseOut(event: MouseEvent) {
  const relatedTarget = event.relatedTarget as HTMLElement
  if (
    relatedTarget && (
      relatedTarget.closest('.mention-profile-card') || 
      relatedTarget.closest('.mention-pill')
    )
  ) {
    return
  }

  if (hideTimeout) clearTimeout(hideTimeout)
  hideTimeout = setTimeout(() => {
    hoveredUser.value = null
  }, 200)
}

function cancelHidePopup() {
  if (hideTimeout) clearTimeout(hideTimeout)
}

function hidePopup(event: MouseEvent) {
  const relatedTarget = event.relatedTarget as HTMLElement
  if (relatedTarget && relatedTarget.closest('.mention-pill')) {
    return
  }

  if (hideTimeout) clearTimeout(hideTimeout)
  hideTimeout = setTimeout(() => {
    hoveredUser.value = null
  }, 200)
}

const activeEmojiMessageId = ref<number | null>(null)

function openEmojiPicker(messageId: number) {
  activeEmojiMessageId.value = messageId
}

function closeEmojiPicker() {
  activeEmojiMessageId.value = null
}

async function toggleReaction(messageId: number, emoji: string) {
  if (!authStore.user) return
  const currentUserId = authStore.user.userId

  const allRawMessages = messageStore.getMessagesByChannel(channelId.value)
  const targetMsg = allRawMessages.find((m: any) => m.id === messageId)
  if (!targetMsg) return

  if (!targetMsg.reactions) {
    targetMsg.reactions = []
  }

  const existingReaction = targetMsg.reactions.find((r: any) => r.emoji === emoji)
  const hasReacted = existingReaction && existingReaction.users.includes(currentUserId)

  try {
    if (hasReacted) {
      await messageStore.removeReaction(messageId, emoji, currentUserId)
    } else {
      await messageStore.addReaction(messageId, emoji, currentUserId)
    }
  } catch (e: any) {
    console.error("リアクションの更新に失敗しました", e)
    showWarning("リアクション", e.response?.data?.message || "リアクションの処理に失敗しました。")
  }
}

async function onEmojiSelect(emoji: any) {
  if (activeEmojiMessageId.value !== null) {
    await toggleReaction(activeEmojiMessageId.value, emoji.native)
    closeEmojiPicker()
  }
}

const emojiI18n = {
  search: '検索',
  notfound: '絵文字が見つかりません',
  categories: {
    search: '検索結果',
    recent: 'よく使う絵文字',
    smileys: '顔と感情',
    people: '人',
    nature: '動物と自然',
    foods: '食べ物と飲み物',
    activity: 'アクティビティ',
    places: '場所と時間と空',
    objects: 'オブジェクト',
    symbols: '記号',
    flags: '国旗',
    custom: 'カスタム',
  },
}

const activeThreadMessage = ref<any>(null)

const threadReplies = computed(() => {
  if (!activeThreadMessage.value) return []
  const parentId = activeThreadMessage.value.id
  const rawMessages = messageStore.getMessagesByChannel(channelId.value)
  const membersMap = new Map(memberStore.members.map((m: any) => [Number(m.userId), m.username]))
  const allUsersMap = new Map(allUsers.value.map((u: any) => [Number(u.userId), u.username]))

  return rawMessages
    .filter((m: any) => Number(m.replyToMessageId) === Number(parentId))
    .map((m: any) => {
      const msgUserId = Number(m.userId)
      return {
        ...m,
        username: m.username || membersMap.get(msgUserId) || allUsersMap.get(msgUserId) || `ユーザー_${msgUserId}`
      }
    })
})

function openThread(message: any) {
  activeThreadMessage.value = message
}

function closeThread() {
  activeThreadMessage.value = null
}

async function submitThreadReply(replyContent: string) {
  if (!authStore.user || !activeThreadMessage.value) return
  if (replyContent.length > configStore.config!.message.maxLength) {
    showWarning("メッセージ", `メッセージは${configStore.config!.message.maxLength}文字以内で入力してください。`)
    return
  }

  try {
    await messageStore.sendMessage(
      channelId.value,
      replyContent,
      activeThreadMessage.value.id
    )
  } catch (e: any) {
    showWarning("メッセージ", e.message || "返信の送信に失敗しました")
  }
}

const showMentionPopup = ref(false)
const mentionCandidates = ref<any[]>([])
const mentionDropdownRef = ref<HTMLElement | null>(null)
const allUsers = computed(() => userStore.users)

function syncSelectedMentions(oldText: string, newText: string) {
  if (oldText === newText || selectedMentions.value.length === 0) return

  let changeStart = 0

  while (
    changeStart < oldText.length &&
    changeStart < newText.length &&
    oldText[changeStart] === newText[changeStart]
  ) {
    changeStart++
  }

  let oldChangeEnd = oldText.length
  let newChangeEnd = newText.length

  while (
    oldChangeEnd > changeStart &&
    newChangeEnd > changeStart &&
    oldText[oldChangeEnd - 1] === newText[newChangeEnd - 1]
  ) {
    oldChangeEnd--
    newChangeEnd--
  }

  const removedLength = oldChangeEnd - changeStart
  const insertedLength = newChangeEnd - changeStart
  const delta = insertedLength - removedLength

  selectedMentions.value = selectedMentions.value
    .filter((mention) => {
      if (removedLength === 0) {
        const insertedInsideMention =
          changeStart > mention.start &&
          changeStart < mention.end

        return !insertedInsideMention
      }

      const overlapsMention =
        changeStart < mention.end &&
        oldChangeEnd > mention.start

      return !overlapsMention
    })
    .map((mention) => {
      if (oldChangeEnd <= mention.start) {
        return {
          ...mention,
          start: mention.start + delta,
          end: mention.end + delta
        }
      }

      return mention
    })
}

function handleInput() {
  autoResize()

  syncSelectedMentions(previousMessage.value, newMessage.value)
  previousMessage.value = newMessage.value
  
  // フォームの高さが変わった直後に最新メッセージが見えるよう一番下へスクロール
  nextTick(() => {
    scrollToBottom()
  })

  if (!isMentionMode.value) {
    return
  }

  const textarea = textareaRef.value
  if (!textarea || mentionStartIndex.value === null) {
    return
  }

  const cursorPosition = textarea.selectionStart

  if (cursorPosition <= mentionStartIndex.value) {
    closeMentionMode()
    return
  }

  const query = newMessage.value.substring(
    mentionStartIndex.value + 1,
    cursorPosition
  )

  if (!isValidMentionQuery(query)) {
    closeMentionMode()
    return
  }

  // 現在の検索文字列の終端を保持
  mentionEndIndex.value = cursorPosition

  updateMentionCandidates(query)
}

function isValidMentionQuery(query: string): boolean {
  return /^[\p{L}\p{N}\u3000 _-]*$/u.test(query)
}


function updateMentionCandidates(query: string) {
  const normalizedQuery = query.toLowerCase()

  const currentMemberIds = new Set(
    memberStore.members.map((m: any) => String(m.userId))
  )

  mentionCandidates.value = userStore.users
    .filter((user) =>
      user.isActive &&
      user.username.toLowerCase().includes(normalizedQuery)
    )
    .map((user) => ({
      ...user,
      isNotMember: !currentMemberIds.has(String(user.userId))
    }))
    .sort((a, b) =>
      Number(a.isNotMember) - Number(b.isNotMember)
    )

  showMentionPopup.value = true
}


function closeMentionMode() {
  isMentionMode.value = false
  mentionStartIndex.value = null
  mentionEndIndex.value = null
  showMentionPopup.value = false
  mentionCandidates.value = []
}


function selectMentionUser(user: any) {
  const textarea = textareaRef.value
  if (!textarea || mentionStartIndex.value === null) return

  const cursorPosition = textarea.selectionStart
  const start = mentionStartIndex.value

  const textBeforeMention = newMessage.value.substring(0, start)
  const textAfterCursor = newMessage.value.substring(cursorPosition)
  const mentionText = `@${user.username}`

  newMessage.value =
    textBeforeMention +
    mentionText +
    " " +
    textAfterCursor

  selectedMentions.value.push({
    userId: Number(user.userId),
    username: user.username,
    start,
    end: start + mentionText.length
  })

  previousMessage.value = newMessage.value

  closeMentionMode()

  nextTick(() => {
    textarea.focus()

    const nextCursorPosition = start + mentionText.length + 1
    textarea.setSelectionRange(nextCursorPosition, nextCursorPosition)

    autoResize()
  })
}

function handleMentionCursorMove() {
  if (
    !isMentionMode.value ||
    mentionStartIndex.value === null ||
    mentionEndIndex.value === null
  ) {
    return
  }

  const textarea = textareaRef.value
  if (!textarea) return

  const selectionStart = textarea.selectionStart
  const selectionEnd = textarea.selectionEnd

  const minPosition = mentionStartIndex.value
  const maxPosition = mentionEndIndex.value

  if (
    selectionStart < minPosition ||
    selectionEnd > maxPosition
  ) {
    closeMentionMode()
  }
}


const channelMessages = computed(() => {
  const rawMessages = messageStore.getMessagesByChannel(channelId.value)
  
  const membersMap = new Map(memberStore.members.map((m: any) => [Number(m.userId), m.username]))
  const allUsersMap = new Map(allUsers.value.map((u: any) => [Number(u.userId), u.username]))

  const replyCountMap = new Map<number, number>()
  rawMessages.forEach((m: any) => {
    if (m.replyToMessageId) {
      const parentId = Number(m.replyToMessageId)
      replyCountMap.set(parentId, (replyCountMap.get(parentId) || 0) + 1)
    }
  })

  return rawMessages
    .filter((m: any) => !m.replyToMessageId)
    .map((m: any) => {
      const msgUserId = Number(m.userId)
      const replyCount = replyCountMap.get(Number(m.id)) || 0
      return {
        ...m,
        username: m.username || membersMap.get(msgUserId) || allUsersMap.get(msgUserId) || `ユーザー_${msgUserId}`,
        replyCount
      }
    })
    .reverse()
})

const members = computed(() => memberStore.members)
const showInfo = ref(false)
function toggleInfo() {
  showInfo.value = !showInfo.value
}

const messageList = ref<HTMLElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)

const activeKebabId = ref<number | null>(null)
const showTemplateDropdown = ref(false)

const templateList = computed(() => templateStore.templates)

function toggleTemplateDropdown() {
  showTemplateDropdown.value = !showTemplateDropdown.value
}

function autoResize() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'

  const maxHeight = maxFormHeight.value
  const currentScrollHeight = el.scrollHeight

  if (currentScrollHeight > maxHeight) {
    el.style.height = `${maxHeight}px`
    el.style.overflowY = 'scroll'
  } else {
    el.style.height = `${currentScrollHeight}px`
    el.style.overflowY = 'hidden'
  }
}

async function selectTemplate(content: string) {
  newMessage.value = content
  selectedMentions.value = []
  previousMessage.value = content
  closeMentionMode()
  showTemplateDropdown.value = false
  await nextTick()
  autoResize()
  scrollToBottom()
}

watch(newMessage, async (newVal) => {
  if (!newVal) {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
      textareaRef.value.style.overflowY = 'hidden'
    }
    await nextTick()
    scrollToBottom()
    return
  }
  await nextTick()
  autoResize()
  scrollToBottom()
})

function goTemplateManagement() {
  showTemplateDropdown.value = false
  router.push("/templates")
}

function handleClickOutside(event: MouseEvent) {
  const target = event.target as HTMLElement

  if (
    showMentionPopup.value &&
    mentionDropdownRef.value &&
    !mentionDropdownRef.value.contains(target) &&
    target !== textareaRef.value
  ) {
    closeMentionMode()
  }

  if (
    showTemplateDropdown.value &&
    !target.closest('.template-dropdown-menu') &&
    !target.closest('.btn-template-trigger')
  ) {
    showTemplateDropdown.value = false
  }
}


function toggleKebab(messageId: number) {
  activeKebabId.value = activeKebabId.value === messageId ? null : messageId
}

function closeKebab() {
  activeKebabId.value = null
}

async function loadChannelData(targetId: number) {
  if (channelStore.channels.length === 0) {
    await channelStore.fetchChannels()
  }
  const ch = await channelStore.fetchChannelById(targetId)
  if (!ch) return

  channelStore.setCurrentChannel(ch)

  hasMorePastMessages.value = true
  isLoadingPast.value = false

  await Promise.all([
    messageStore.fetchMessages(targetId, configStore.config?.message?.pageRange || 20),
    memberStore.fetchMembers(targetId)
  ])

  // v-if="channel" が true になり、DOMが生成されたのを見届けてからイベントを登録
  nextTick(() => {
    if (messageList.value) {
      // 二重登録を防ぐため一度解除してから登録
      messageList.value.removeEventListener('scroll', handleScroll)
      messageList.value.addEventListener('scroll', handleScroll)
    }
    
    if (!route.query.highlightMessageId) {
      scrollToBottom(true) // 初回読み込み時は強制的に最下部へ
    }
  })
}

async function reloadMembers() {
  await memberStore.fetchMembers(channelId.value)
}

async function scrollToTargetMessage(messageId: number | string) {
  const targetIdStr = String(messageId)
  let targetEl: HTMLElement | null = null
  let retries = 0

  while (!targetEl && retries < 10) {
    await nextTick()
    if (messageList.value) {
      targetEl = messageList.value.querySelector(`[data-message-id="${targetIdStr}"]`) as HTMLElement
    }
    if (!targetEl) {
      await new Promise(resolve => setTimeout(resolve, 50))
      retries++
    }
  }

  if (targetEl) {
    targetEl.scrollIntoView({ behavior: "smooth", block: "center" })
    targetEl.classList.add("message-highlight")
    setTimeout(() => targetEl.classList.remove("message-highlight"), 2000)
  }
}

watch(() => channelMessages.value.length, (newLen, oldLen) => {
  if (newLen > oldLen && !route.query.highlightMessageId) {
    scrollToBottom()
  }
})


watch(() => route.params.id, async (newId) => {
  if (!newId) return

  closeThread()

  newMessage.value = ""
  previousMessage.value = ""
  selectedMentions.value = []
  closeMentionMode()

  await loadChannelData(Number(newId))
})

watch(() => route.query.highlightMessageId, async (newId) => {
  if (newId) await scrollToTargetMessage(Number(newId))
}, { immediate: true })

onMounted(async () => {
  document.addEventListener('click', handleClickOutside)

  await templateStore.fetchTemplates().catch((err) => {
    console.warn("テンプレートの取得に失敗しました:", err)
  })

  await loadChannelData(channelId.value)
  if (route.query.highlightMessageId) {
    await scrollToTargetMessage(Number(route.query.highlightMessageId))
  }

  window.addEventListener(
    "dragover",
    preventDefault
  )

  window.addEventListener(
    "drop",
    preventDefault
  )
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  if (messageList.value) {
    messageList.value.removeEventListener('scroll', handleScroll)
  }
  
  window.removeEventListener(
    "dragover",
    preventDefault
  )

  window.removeEventListener(
    "drop",
    preventDefault
  )
})

async function submitMessage() {
  if (!authStore.user) return

  if (!newMessage.value.trim() && selectedFiles.value.length === 0) {
    return
  }

  if (newMessage.value.length > configStore.config!.message.maxLength) {
    showWarning("メッセージ", `メッセージは${configStore.config!.message.maxLength}文字以内で入力してください。`)
    return
  }

  // 添付ファイルがある場合だけチェック
  if (selectedFiles.value.length > 0) {

    const usage =
      await messageStore.getStorageUsage()
      console.log("ストレージ使用率:", usage.usagePercent, "%")
    if (
      usage.usagePercent >=
      configStore.config!.storage.deleteThresholdPercent
    ) {
      await showError(
        "ストレージ使用率警告",
        "ストレージ使用率が上限に達しているため添付ファイルをアップロードできません。"
      )
      return
    }

    if (
      // usage.usagePercent >=
      // configStore.config!.storage.warnThresholdPercent
      true // ※テスト用に常に警告を出す
    ) {
      const result = await showWarning(
        "ストレージ使用率警告",
        `ストレージ使用率が${usage}%です。
      このままアップロードしますか？`
      )

      if (!result) {
        return
      }
    }
  }

  const mentionedUserIds = [
    ...new Set(
      selectedMentions.value.map(m => m.userId)
    )
  ]

  // 送信内容を退避
  const messageText = newMessage.value
  const files = [...selectedFiles.value]

  // UIをクリア
  newMessage.value = ""
  previousMessage.value = ""
  selectedMentions.value = []
  selectedFiles.value = []

  closeMentionMode()

  try {
    const messageId = await messageStore.sendMessage(
      channelId.value,
      messageText,
      null,
      mentionedUserIds
    )

    if (files.length > 0) {
      await uploadAttachments(messageId, files)
    }

    scrollToBottom(true)

  } catch (err) {
    console.error("メッセージ送信エラー:", err)

    // 復元
    newMessage.value = messageText
    selectedFiles.value = files
  }
}


async function saveEdit() {
  if (!editContent.value.trim()) return
  if (editContent.value.length > configStore.config!.message.maxLength) {
    showWarning("メッセージ", `メッセージは${configStore.config!.message.maxLength}文字以内で入力してください。`)
    return
  }
  await messageStore.updateMessage(editingMessage.value.id, editContent.value, 0)
  editingMessage.value = null
}

function openEdit(message: any) {
  editingMessage.value = message
  editContent.value = message.content
}

function cancelEdit() {
  editingMessage.value = null
}

async function deleteMessage(id: number) {
  const result = await showWarning(
    "メッセージ削除確認",
    "本当にこのメッセージを削除しますか？"
  )
  if (!result) return
  await messageStore.deleteMessage(id)
}

function scrollToBottom(force = false) {
  nextTick(() => {
    if (messageList.value) {
      // 強制フラグがある、またはユーザーが最下部付近にいる場合のみスクロールを実行
      if (force || isNearBottom.value) {
        messageList.value.scrollTop = messageList.value.scrollHeight
      }
    }
  })
}

async function leaveChannel() {
  const result = await showWarning(
    "チャンネル退出確認",
    "このチャンネルから退出しますか？"
  )
  if (!result) return
  if (!authStore.user) {
    showWarning("チャンネル退出", "ユーザー情報が取得できません。")
    return
  }

  try {
    await memberStore.leaveChannel(channelId.value, authStore.user.userId)
    showWarning("チャンネル退出", "チャンネルから退出しました。")
    router.push("/home")
  } catch (e: any) {
    showWarning("チャンネル退出", e.response?.data?.message || "チャンネルからの退出に失敗しました。")
  }
}

async function deleteChannel() {
  const result = await showWarning(
    "チャンネル削除確認",
    "本当にこのチャンネルを削除しますか？"
  )
  if (!result) return
  try {
    await channelStore.deleteChannel(channelId.value)
    showWarning("チャンネル削除", "チャンネルを削除しました。")
    router.push("/home")
  } catch (e: any) {
    showWarning("チャンネル削除", e.response?.data?.message || "チャンネルの削除に失敗しました。")
  }
}

async function editChannel() {

}

async function archiveChannel() {
  const currentCh = channelStore.currentChannel
  if (!currentCh) {
    showWarning("チャンネルアーカイブ", "チャンネル情報の取得に失敗しました。")
    return
  }

  const isArchived = currentCh.status === 1
  const actionText = isArchived ? 'アーカイブ解除' : 'アーカイブ化'
  const nextStatus = isArchived ? 0 : 1

  const result = await showWarning(
    "チャンネルアーカイブ確認",
    `このチャンネルを${actionText}しますか？`
  )
  if (!result) return

  try {
    // 💡 リクエストを送信
    await channelStore.editChannel(Number(channelId.value), currentCh.name, nextStatus)
    showWarning("チャンネルアーカイブ", `チャンネルを${actionText}しました。`)
    router.push("/home")
  } catch (e: any) {
    console.error("アーカイブエラー詳細:", e.response?.data)
    showWarning("チャンネルアーカイブ", e.response?.data?.message || `チャンネルの${actionText}に失敗しました。`)
  }
}


//マークダウン補助ボタン
type MarkdownFormat =
  | 'bold'
  | 'italic'
  | 'strike'
  | 'inlineCode'
  | 'codeBlock'
  | 'quote'
  | 'bulletList'
  | 'orderedList'
  | 'link'

function applyMarkdownFormat(format: MarkdownFormat) {
  const textarea = textareaRef.value
  if (!textarea) return

  switch (format) {
    case 'bold':
      applyInlineFormat('**')
      break

    case 'italic':
      applyInlineFormat('*')
      break

    case 'strike':
      applyInlineFormat('~~')
      break

    case 'inlineCode':
      applyInlineFormat('`')
      break

    case 'codeBlock':
      applyCodeBlock()
      break

    case 'quote':
      applyLinePrefix('> ')
      break

    case 'bulletList':
      applyLinePrefix('- ')
      break

    case 'orderedList':
      applyOrderedList()
      break

    case 'link':
      applyLink()
      break
  }
}

// 太字、斜体、取り消し線、インラインコード
function applyInlineFormat(marker: string) {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd

  const before = newMessage.value.substring(0, start)
  const selectedText = newMessage.value.substring(start, end)
  const after = newMessage.value.substring(end)

  const hasSelection = start !== end

  // 未選択時：空の雛形があるなら削除
  if (!hasSelection) {
    const markerLength = marker.length

    const beforeMarker = newMessage.value.substring(
      start - markerLength,
      start
    )

    const afterMarker = newMessage.value.substring(
      start,
      start + markerLength
    )

    const isEmptyTemplate =
      beforeMarker === marker &&
      afterMarker === marker

    if (isEmptyTemplate) {
      newMessage.value =
        newMessage.value.substring(
          0,
          start - markerLength
        ) +
        newMessage.value.substring(
          start + markerLength
        )

      nextTick(() => {
        textarea.focus()

        const cursorPosition =
          start - markerLength

        textarea.setSelectionRange(
          cursorPosition,
          cursorPosition
        )

        autoResize()
      })

      return
    }
  }

  // 選択あり
  if (hasSelection) {
    const alreadyFormatted =
      selectedText.startsWith(marker) &&
      selectedText.endsWith(marker) &&
      selectedText.length >= marker.length * 2

    if (alreadyFormatted) {
      const unformattedText = selectedText.slice(
        marker.length,
        selectedText.length - marker.length
      )

      newMessage.value =
        before +
        unformattedText +
        after

      nextTick(() => {
        textarea.focus()

        textarea.setSelectionRange(
          start,
          start + unformattedText.length
        )

        autoResize()
      })

      return
    }

    newMessage.value =
      before +
      marker +
      selectedText +
      marker +
      after

    nextTick(() => {
      textarea.focus()

      textarea.setSelectionRange(
        start,
        end + marker.length * 2
      )

      autoResize()
    })

    return
  }

  // 未選択時：雛形挿入
  newMessage.value =
    before +
    marker +
    marker +
    after

  nextTick(() => {
    textarea.focus()

    const cursorPosition =
      start + marker.length

    textarea.setSelectionRange(
      cursorPosition,
      cursorPosition
    )

    autoResize()
  })
}

// コードブロック
function applyCodeBlock() {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd

  const before = newMessage.value.substring(0, start)
  const selectedText = newMessage.value.substring(start, end)
  const after = newMessage.value.substring(end)

  // 未選択時：空コードブロックなら削除
  if (start === end) {
    const templateBefore = '```\n'
    const templateAfter = '\n```'

    const beforeText =
      newMessage.value.substring(
        start - templateBefore.length,
        start
      )

    const afterText =
      newMessage.value.substring(
        start,
        start + templateAfter.length
      )

    const isEmptyTemplate =
      beforeText === templateBefore &&
      afterText === templateAfter

    if (isEmptyTemplate) {
      newMessage.value =
        newMessage.value.substring(
          0,
          start - templateBefore.length
        ) +
        newMessage.value.substring(
          start + templateAfter.length
        )

      nextTick(() => {
        textarea.focus()

        const cursorPosition =
          start - templateBefore.length

        textarea.setSelectionRange(
          cursorPosition,
          cursorPosition
        )

        autoResize()
      })

      return
    }
  }

  // 選択あり
  if (start !== end) {
    const formatted =
      `\`\`\`\n${selectedText}\n\`\`\``

    newMessage.value =
      before +
      formatted +
      after

    nextTick(() => {
      textarea.focus()

      textarea.setSelectionRange(
        start,
        start + formatted.length
      )

      autoResize()
    })

    return
  }

  // 未選択時：雛形挿入
  const template =
    `\`\`\`\n\n\`\`\``

  newMessage.value =
    before +
    template +
    after

  nextTick(() => {
    textarea.focus()

    const cursorPosition =
      start + 4

    textarea.setSelectionRange(
      cursorPosition,
      cursorPosition
    )

    autoResize()
  })
}

// 引用、箇条書き
function applyLinePrefix(prefix: string) {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd

  const before = newMessage.value.substring(0, start)
  const selectedText = newMessage.value.substring(start, end)
  const after = newMessage.value.substring(end)

  // 未選択時：直前が雛形だけなら削除
  if (start === end) {
    const beforePrefix =
      newMessage.value.substring(
        start - prefix.length,
        start
      )

    if (beforePrefix === prefix) {
      newMessage.value =
        newMessage.value.substring(
          0,
          start - prefix.length
        ) +
        newMessage.value.substring(start)

      nextTick(() => {
        textarea.focus()

        const cursorPosition =
          start - prefix.length

        textarea.setSelectionRange(
          cursorPosition,
          cursorPosition
        )

        autoResize()
      })

      return
    }
  }

  // 選択あり
  if (start !== end) {
    const formatted = selectedText
      .split('\n')
      .map(line => prefix + line)
      .join('\n')

    newMessage.value =
      before +
      formatted +
      after

    nextTick(() => {
      textarea.focus()

      textarea.setSelectionRange(
        start,
        start + formatted.length
      )

      autoResize()
    })

    return
  }

  // 未選択時：雛形挿入
  newMessage.value =
    before +
    prefix +
    after

  nextTick(() => {
    textarea.focus()

    const cursorPosition =
      start + prefix.length

    textarea.setSelectionRange(
      cursorPosition,
      cursorPosition
    )

    autoResize()
  })
}

// 番号付きリスト
function applyOrderedList() {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd

  const before = newMessage.value.substring(0, start)
  const selectedText = newMessage.value.substring(start, end)
  const after = newMessage.value.substring(end)

  const prefix = '1. '

  // 未選択時：雛形削除
  if (start === end) {
    const beforePrefix =
      newMessage.value.substring(
        start - prefix.length,
        start
      )

    if (beforePrefix === prefix) {
      newMessage.value =
        newMessage.value.substring(
          0,
          start - prefix.length
        ) +
        newMessage.value.substring(start)

      nextTick(() => {
        textarea.focus()

        const cursorPosition =
          start - prefix.length

        textarea.setSelectionRange(
          cursorPosition,
          cursorPosition
        )

        autoResize()
      })

      return
    }
  }

  // 選択あり
  if (start !== end) {
    const formatted = selectedText
      .split('\n')
      .map((line, index) =>
        `${index + 1}. ${line}`
      )
      .join('\n')

    newMessage.value =
      before +
      formatted +
      after

    nextTick(() => {
      textarea.focus()

      textarea.setSelectionRange(
        start,
        start + formatted.length
      )

      autoResize()
    })

    return
  }

  // 未選択時
  newMessage.value =
    before +
    prefix +
    after

  nextTick(() => {
    textarea.focus()

    const cursorPosition =
      start + prefix.length

    textarea.setSelectionRange(
      cursorPosition,
      cursorPosition
    )

    autoResize()
  })
}

// リンク
function applyLink() {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd

  const before = newMessage.value.substring(0, start)
  const selectedText = newMessage.value.substring(start, end)
  const after = newMessage.value.substring(end)

  const template = '[表示文字](URL)'

  // 雛形全体がカーソル周辺にある場合は削除
  const templateStart =
    newMessage.value.lastIndexOf(
      template,
      start
    )

  if (
    templateStart !== -1 &&
    start >= templateStart &&
    end <= templateStart + template.length
  ) {
    newMessage.value =
      newMessage.value.substring(
        0,
        templateStart
      ) +
      newMessage.value.substring(
        templateStart + template.length
      )

    nextTick(() => {
      textarea.focus()

      textarea.setSelectionRange(
        templateStart,
        templateStart
      )

      autoResize()
    })

    return
  }

  // 選択あり
  if (start !== end) {
    const formatted =
      `[${selectedText}](URL)`

    newMessage.value =
      before +
      formatted +
      after

    nextTick(() => {
      textarea.focus()

      const urlStart =
        start +
        selectedText.length +
        3

      textarea.setSelectionRange(
        urlStart,
        urlStart + 3
      )

      autoResize()
    })

    return
  }

  // 未選択時：雛形追加
  newMessage.value =
    before +
    template +
    after

  nextTick(() => {
    textarea.focus()

    textarea.setSelectionRange(
      start + 1,
      start + 5
    )

    autoResize()
  })
}

// 箇条書きなどEnterを押したときの自動挿入
function handleMarkdownEnter(event: KeyboardEvent) {
  if (event.key !== 'Enter') return
  if (event.shiftKey) return

  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const value = newMessage.value

  const lineStart =
    value.lastIndexOf('\n', start - 1) + 1

  const currentLine =
    value.slice(lineStart, start)

  // -------------------------
  // 1. 空の箇条書き行なら終了
  //    例:
  //    - |
  //      - |
  // -------------------------
  if (/^\s*- $/.test(currentLine)) {
    event.preventDefault()

    newMessage.value =
      value.slice(0, lineStart) +
      value.slice(start)

    nextTick(() => {
      textarea.focus()

      textarea.setSelectionRange(
        lineStart,
        lineStart
      )

      autoResize()
    })

    return
  }

  // -------------------------
  // 2. 空の引用行なら終了
  // -------------------------
  if (/^\s*> $/.test(currentLine)) {
    event.preventDefault()

    newMessage.value =
      value.slice(0, lineStart) +
      value.slice(start)

    nextTick(() => {
      textarea.focus()

      textarea.setSelectionRange(
        lineStart,
        lineStart
      )

      autoResize()
    })

    return
  }

  // -------------------------
  // 3. 空の番号付きリストなら終了
  // -------------------------
  if (/^\s*\d+\. $/.test(currentLine)) {
    event.preventDefault()

    newMessage.value =
      value.slice(0, lineStart) +
      value.slice(start)

    nextTick(() => {
      textarea.focus()

      textarea.setSelectionRange(
        lineStart,
        lineStart
      )

      autoResize()
    })

    return
  }

  // -------------------------
  // 4. 通常の継続入力
  // -------------------------
  let nextPrefix: string | null = null

  // 箇条書き
  const bulletMatch =
    currentLine.match(/^(\s*)- /)

  if (bulletMatch) {
    const indent = bulletMatch[1]

    nextPrefix =
      `${indent}- `
  }

  // 引用
  else {
    const quoteMatch =
      currentLine.match(/^(\s*)> /)

    if (quoteMatch) {
      const indent = quoteMatch[1]

      nextPrefix =
        `${indent}> `
    }

    // 番号付きリスト
    else {
      const orderedMatch =
        currentLine.match(/^(\s*)(\d+)\. /)

      if (orderedMatch) {
        const indent = orderedMatch[1]
        const currentNumber =
          Number(orderedMatch[2])

        nextPrefix =
          `${indent}${currentNumber + 1}. `
      }
    }
  }

  if (!nextPrefix) return

  event.preventDefault()

  const before =
    value.slice(0, start)

  const after =
    value.slice(end)

  newMessage.value =
    before +
    '\n' +
    nextPrefix +
    after

  nextTick(() => {
    const cursorPosition =
      start +
      1 +
      nextPrefix.length

    textarea.focus()

    textarea.setSelectionRange(
      cursorPosition,
      cursorPosition
    )

    autoResize()
  })
}

// スクロールされたときに最下部判定を行う関数
async function handleScroll() {
  const el = messageList.value
  if (!el) return

  // 過去ログの追加読み込み（スクロールが最上部に達した時）
  if (el.scrollTop === 0 && hasMorePastMessages.value && !isLoadingPast.value) {
    // 現在画面に表示されている中で、最も古い（配列の先頭の）メッセージを取得
    const currentChannelMsgs = channelMessages.value
    if (currentChannelMsgs.length > 0) {
      isLoadingPast.value = true
      
      const oldestMsg = currentChannelMsgs[0]
      
      // スクロール位置キープのための前準備（読み込み前の総高さを記憶）
      const previousScrollHeight = el.scrollHeight

      // 過去ログを20件取得
      const serverHasMore = await messageStore.fetchPreviousMessages(channelId.value, oldestMsg.id, configStore.config!.message.pageRange || 20)
      
      // 0件だったら、これ以上過去メッセージはないと判断してストッパーをかける
      hasMorePastMessages.value = serverHasMore


      // 過去ログが上に挿入された後、画面が最上部に強制ジャンプするのを防ぎ、
      // 読んでいた元のメッセージの位置を正確にキープする
      await nextTick()
      const heightDifference = el.scrollHeight - previousScrollHeight
      el.scrollTop = heightDifference
      
      isLoadingPast.value = false
    }
  }

  if (el.scrollHeight <= el.clientHeight) {
    isNearBottom.value = true
    return
  }
  const threshold = 50
  const distanceFromBottom = el.scrollHeight - el.scrollTop - el.clientHeight
  isNearBottom.value = distanceFromBottom <= threshold
}

// ＋メニューの状態
const showPlusMenu = ref(false)
const activePlusItem = ref<string | null>(null)

// ファイル選択
const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFiles = ref<any[]>([])

function togglePlusMenu() {
  showPlusMenu.value = !showPlusMenu.value
}

function selectPlusItem(type: 'file' | 'template') {
  activePlusItem.value = type

  if (type === 'file') {
    // ファイル選択ダイアログを開く
    fileInputRef.value?.click()
  }

  if (type === 'template') {
    // 既存のテンプレート機能を開く
    toggleTemplateDropdown()
  }

  togglePlusMenu()
}

interface SelectedAttachment {
  file: File
  baseName: string
  extension: string
}


function createAttachment(
  file: File
): SelectedAttachment {

  const lastDot =
    file.name.lastIndexOf(".")

  if (lastDot === -1) {

    return {
      file,
      baseName: file.name,
      extension: ""
    }
  }

  return {
    file,
    baseName: file.name.substring(
      0,
      lastDot
    ),
    extension: file.name.substring(
      lastDot
    )
  }
}

// ファイル追加処理
function addFiles(files: File[]) {
  // -----------------------------
  // 添付数チェック（最大10個）
  // -----------------------------
  const MAX_FILE_COUNT = 10

  if (
    selectedFiles.value.length + files.length >
    MAX_FILE_COUNT
  ) {
    showWarning("ファイル添付", `添付できるファイルは最大${MAX_FILE_COUNT}個までです。`)
    return
  }

  for (const file of Array.from(files)) {
    // -----------------------------
    // ① 拡張子チェック
    // -----------------------------
    const allowedExt = ["pdf", "docx", "xlsx", "pptx", "png", "jpg", "jpeg", "webp", "gif", "md", "txt", "zip", "tar.gz"]
    let ext = file.name.toLowerCase()

    if (ext.endsWith(".tar.gz")) {
      ext = "tar.gz"
    } else {
      ext = ext.split(".").pop()!
    }

    if (!ext || !allowedExt.includes(ext)) {
      showWarning("ファイル添付", `このファイル形式（.${ext}）は添付できません。`)
      continue
    }

    // -----------------------------
    // ② サイズチェック（1ファイル）
    // -----------------------------
    const MAX_FILE_SIZE = 30 * 1024 * 1024 // 30MB
    if (file.size > MAX_FILE_SIZE) {
      showWarning("ファイル添付", "1ファイルの上限（30MB）を超えています。")
      continue
    }

    // -----------------------------
    // ③ サイズチェック（合計）
    // -----------------------------
    const MAX_TOTAL_SIZE = 150 * 1024 * 1024 // 150MB
    const currentTotal = selectedFiles.value.reduce((sum, f) => sum + f.file.size, 0)

    if (currentTotal + file.size > MAX_TOTAL_SIZE) {
      showWarning("ファイル添付", "添付ファイルの合計サイズが上限（150MB）を超えています。")
      continue
    }

    // -----------------------------
    // ④ 添付ファイルリストに追加
    // -----------------------------
    const attachment = createAttachment(file)
    selectedFiles.value.push({
      file: file,
      baseName: attachment.baseName,
      extension: attachment.extension,
      previewUrl: file.type.startsWith("image/")
        ? URL.createObjectURL(file)
        : null
    })
  }
}

function handleFileSelected(e: Event) {
  const files = (e.target as HTMLInputElement).files
  if (!files || files.length === 0) return
  
  addFiles(Array.from(files))
  
  ;(e.target as HTMLInputElement).value = ""
  showPlusMenu.value = false
  activePlusItem.value = null
}


function removeSelectedFile(target: any) {
  selectedFiles.value = selectedFiles.value.filter(f => f !== target)
}

async function uploadAttachments(
  messageId: number,
  files: typeof selectedFiles.value
) {
  for (const item of files) {
    try {
      await messageStore.uploadAttachment(
        messageId,
        item
      )
    } catch (err) {
      console.error(
        "添付ファイルアップロード失敗:",
        item.file.name,
        err
      )
      showError("ファイル添付", "添付ファイルのアップロードに失敗しました。")
    }
  }
}

const isDragging = ref(false)

function onDragOver(event: DragEvent) {
  event.preventDefault()
}

function onDragEnter(event: DragEvent) {
  event.preventDefault()

  isDragging.value = true
}

function onDragLeave(event: DragEvent) {
  event.preventDefault()

  isDragging.value = false
}

function onDrop(event: DragEvent) {

  event.preventDefault()

  isDragging.value = false

  const items =
    event.dataTransfer?.items

  if (!items) {
    return
  }

  for (const item of items) {

    const entry =
      (item as any)
        .webkitGetAsEntry?.()

    if (entry?.isDirectory) {

      showWarning(
        "フォルダは添付できません",
        "ファイルを選択してください。"
      )

      return
    }
  }

  const files =
    Array.from(
      event.dataTransfer.files
    )

  addFiles(files)
}

function preventDefault(
  event: DragEvent
) {
  event.preventDefault()
}

function sanitizeFileName(
  value: string
): string {

  return value
    .replace(
      /[\\/:*?"<>|]/g,
      ""
    )
    .replace(/\s+/g, " ")
    .trim()
}

function onFileNameInput(
  event: Event,
  attachment: SelectedAttachment
) {

  attachment.baseName =
    sanitizeFileName(
      (event.target as HTMLInputElement)
        .value
    )
}


</script>