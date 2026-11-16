<script setup lang="ts">
import { computed } from 'vue'

import PdfIcon from '@/assets/icons/file-types/pdf.svg'
import WordIcon from '@/assets/icons/file-types/word.svg'
import ExcelIcon from '@/assets/icons/file-types/excel.svg'
import PowerPointIcon from '@/assets/icons/file-types/powerpoint.svg'
import DefaultIcon from '@/assets/icons/file-types/file.svg'

const props = defineProps<{ filename: string }>()

const iconUrl = computed(() => {
  // 安全に拡張子を取得
  const ext = props.filename ? props.filename.split('.').pop()?.toLowerCase() : ''

  switch (ext) {
    case 'pdf':
      return PdfIcon
    case 'doc':
    case 'docx':
      return WordIcon
    case 'xls':
    case 'xlsx':
      return ExcelIcon
    case 'ppt':
    case 'pptx':
      return PowerPointIcon
    default:
      return DefaultIcon
  }
})
</script>

<template>
  <img :src="iconUrl" class="file-type-icon__svg" />
</template>

<style scoped>
.file-type-icon__svg {
  width: 60px;
  height: 60px;
  object-fit: contain;
}
</style>
