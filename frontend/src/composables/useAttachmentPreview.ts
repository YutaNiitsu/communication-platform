import { ref } from "vue"
import type { Attachment } from "@/types/Attachment"

export function useAttachmentPreview() {

  const previewAttachment =
    ref<Attachment | null>(null)

  function openPreview(
    attachment: Attachment
  ) {
    previewAttachment.value =
      attachment
  }

  function closePreview() {
    previewAttachment.value = null
  }

  return {
    previewAttachment,
    openPreview,
    closePreview
  }
}