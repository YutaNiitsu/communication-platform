import { ref } from "vue"
import type { Attachment } from "@/types/Attachment"

export function useAttachmentInfo() {

  const selectedAttachment =
    ref<Attachment | null>(null)

  const showInfoDialog =
    ref(false)

  function showInfo(
    attachment: Attachment
  ) {
    selectedAttachment.value =
      attachment

    showInfoDialog.value =
      true
  }

  function close() {
    showInfoDialog.value =
      false
  }

  return {
    selectedAttachment,
    showInfoDialog,
    showInfo,
    close
  }
}