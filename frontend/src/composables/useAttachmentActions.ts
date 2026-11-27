// composables/useAttachmentActions.ts
import type { Attachment } from "@/types/Attachment"
import { useMessageStore } from "@/stores/useMessageStore"
import { useAlertDialog } from "@/composables/useAlertDialog"

export function useAttachmentActions() {

  const messageStore =
    useMessageStore()

  const {
    showSuccess,
    showWarning,
    showError
  } = useAlertDialog()

  const API_BASE = import.meta.env.VITE_API_BASE_URL

  async function deleteFile(
    att: Attachment
  ) {
    const result =
        await showWarning(
        "ファイル削除",
        `本当に「${att.originalFileName}」を削除しますか？`
        )

    if (!result) {
        return
    }

    try {

        await messageStore
        .deleteAttachment(att.id)

        await showSuccess(
        "ファイル削除成功",
        `「${att.originalFileName}」を削除しました。`
        )

    } catch {

        await showError(
        "ファイル削除エラー",
        `「${att.originalFileName}」の削除に失敗しました。`
        )
    }
  }

  function downloadFile(
    att: Attachment
  ) {
    const link = document.createElement("a")
    link.href = `${API_BASE + ":8080"}${"/files/download/" + att.id}`
    link.download = att.originalFileName
    link.click()
  }

  return {
    deleteFile,
    downloadFile
  }
}