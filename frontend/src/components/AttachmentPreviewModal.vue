<template>
  <div class="preview-overlay" @click="close">
    <div class="preview-modal" @click.stop>
      <header class="preview-header">
        <span>{{ attachment.originalFileName }}</span>
        <button class="close-btn" @click="close">×</button>
      </header>

      <div class="preview-body">

        <!-- 画像プレビュー -->
        <img
          v-if="attachment.fileType === 'image'"
          :src="absoluteUrl(attachment.id)"
          class="preview-image"
        />

        <!-- PDF プレビュー -->
        <embed
          v-else-if="attachment.fileType === 'pdf'"
          :src="absoluteUrl(attachment.id)"
          class="preview-pdf"
        ></embed>

        <!-- Word プレビュー -->
        <div v-else-if="attachment.fileType === 'word'" ref="docxContainer"></div>

        <!-- Excel プレビュー -->
        <div v-else-if="attachment.fileType === 'excel'" ref="excelContainer"></div>

        <!-- その他 -->
        <div v-else class="preview-generic">
          <FileTypeIcon :filename="attachment.originalFileName" />
          <p>このファイル形式はプレビューできません。</p>
          <a :href="absoluteUrl(attachment.id)" download>ダウンロードする</a>
        </div>
        
      </div>
      <button
        class="preview-download-btn"
        @click="downloadFile(attachment)"
        title="ダウンロード"
      >
        💾
      </button>
    </div>
  </div>
  
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { useAttachmentActions } from "@/composables/useAttachmentActions"
import * as docx from "docx-preview"
import * as XLSX from "xlsx"

const props = defineProps<{ attachment: any }>()
const emit = defineEmits(["close"])

const { downloadFile } = useAttachmentActions()

const API_BASE = import.meta.env.VITE_API_BASE_URL

function absoluteUrl(id: number) {
  return `${API_BASE + ":8080"}${"/files/download/" + id}`
}

const docxContainer = ref<HTMLElement | null>(null)
const excelContainer = ref<HTMLElement | null>(null)

onMounted(async () => {
  const url = absoluteUrl(props.attachment.id)

  // Word
  if (props.attachment.fileType === "word") {
    const res = await fetch(url, {
      credentials: "include"
    })
    const blob = await res.blob()
    docx.renderAsync(blob, docxContainer.value!)
  }

  // Excel
  if (props.attachment.fileType === "excel") {
    const res = await fetch(url, {
      credentials: "include"
    })
    const arrayBuffer = await res.arrayBuffer()
    const workbook = XLSX.read(arrayBuffer)
    const sheetName = workbook.SheetNames[0]
    const sheet = workbook.Sheets[sheetName]
    const html = XLSX.utils.sheet_to_html(sheet)
    excelContainer.value!.innerHTML = html
  }
})

function close() {
  emit("close")
}

// function downloadAttachment() {
//   const url = absoluteUrl(props.attachment.fileUrl)
//   const a = document.createElement("a")
//   a.href = url
//   a.download = props.attachment.originalFileName
//   a.click()
// }

</script>

<style scoped>
.preview-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.preview-modal {
  background: #fff;
  border-radius: 8px;
  width: 80%;
  max-width: 900px;
  max-height: 90%;
  display: flex;
  flex-direction: column;
  position: relative;
}

.preview-header {
  padding: 12px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #ddd;
}

.preview-body {
  padding: 16px;
  overflow: auto;
}

.preview-image {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
}

.preview-pdf {
  width: 100%;
  height: 80vh;
}

/* 後から挿入されたtable要素全体にスタイルを強制適用 */
.preview-body :deep(table) {
  border-collapse: collapse;
  font-size: 14px;
  color: #222222 !important;       /* 文字色を濃いグレーに固定 */
  background-color: #ffffff !important; /* 背景色を白に固定 */
  table-layout: fixed;             /* Excel本来の列幅（colタグのwidth）を有効にする */
  width: max-content;              /* コンテンツの幅に合わせてテーブルを広げる */
  min-width: 100%;
}

.preview-body :deep(table) td,
.preview-body :deep(table) th {
  border: 1px solid #ccc !important;
  padding: 6px 8px;
  white-space: nowrap;
  font-weight: 500; 
  color: #222222 !important;       /* セル単体でも文字色を保証 */
  background-color: #ffffff !important; /* セル単体でも背景色を保証 */
  vertical-align: middle;
}

.preview-body :deep(table) tr {
  height: auto;
  line-height: 1.4;
}

.preview-body table td {
  vertical-align: middle;
}

.preview-download-btn {
  position: absolute;
  right: 16px;
  bottom: 16px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #ffffff;
  border: 1px solid #ccc;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 9999;
}

.preview-download-btn:hover {
  background: #f0f0f0;
}


</style>
