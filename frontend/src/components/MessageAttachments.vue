<template>
  <div class="message-attachments">
    <div
      v-for="att in localAttachments"
      :key="att.id"
      class="attachment-item"
      @mouseenter="hoveredId = att.id"
      @mouseleave="hoveredId = null"
    >
      <!-- 添付ファイル本体（クリックでプレビュー） -->
      <div class="attachment-content" @click="$emit('preview', att)">
        <img
          v-if="att.fileType === 'image'"
          :src="absoluteUrl(att.thumbnailUrl || att.fileUrl)"
          class="attachment-thumb"
        />
        <FileTypeIcon
          v-else
          :filename="att.originalFileName"
          class="attachment-icon"
        />
        <span class="attachment-name">{{ att.originalFileName }}</span>
      </div>
      <!-- ホバー時メニュー -->
      <div
        v-if="hoveredId === att.id"
        class="attachment-menu"
      >
        <button
          class="menu-btn"
          title="ダウンロード"
          @click.stop="$emit('download', att)"
        >
          💾
        </button>

        <button
          class="menu-btn"
          title="情報を見る"
          @click.stop="$emit('info', att)"
        >
          ℹ️
        </button>
        <button
          v-if="messageUserId === currentUserId || isAdmin"
          class="menu-btn"
          title="削除"
          @click.stop="$emit('delete', att)"
        >
          ✖
        </button>
      </div>
      
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue"
import FileTypeIcon from "./FileTypeIcon.vue"
import type { Attachment } from "@/types/Attachment"

const props = defineProps<{
  attachments: Attachment[]
  messageUserId: number
  currentUserId: number | undefined
  isAdmin: boolean
}>()

const hoveredId = ref<number | null>(null)

const API_BASE = import.meta.env.VITE_API_BASE_URL

function absoluteUrl(path: string) {
  return `${API_BASE + ":8080"}${path}`
}

// 再描画用のローカルコピー
const localAttachments = ref<Attachment[]>([])

// props.attachments の変更を確実に拾う
watch(
  () => props.attachments,
  (newVal) => {
    localAttachments.value = [...newVal]   // 新しい配列に置き換える
  },
  { deep: true, immediate: true }
)

</script>

<style scoped>
.message-attachments {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: flex-start;
}

.attachment-item {
  position: relative !important;
  display: flex;
  align-items: center;
}

.attachment-content {
  position: relative !important;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f8f8f8;
  border-radius: 6px;
  padding: 6px 10px;
  cursor: pointer;
  flex-shrink: 0;
}

.attachment-thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.attachment-icon {
  font-size: 32px;
  color: #555;
}

.attachment-name {
  color: #000;
  font-size: 14px;
  word-break: break-all;
}

.attachment-menu {
  position: absolute;
  left: calc(100% - 98px);
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  gap: 6px;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 6px;
  padding: 4px 8px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.15);
  z-index: 10;
}

.menu-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  color: #000;
  padding: 2px;
}
</style>
