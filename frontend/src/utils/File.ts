import type { Attachment } from "@/types/Attachment"
import { useMessageStore } from "@/stores/useMessageStore"
import { useAlertDialog } from "@/composables/useAlertDialog"

const messageStore = useMessageStore()
const alertDialog = useAlertDialog()
const { showSuccess, showWarning, showError } = alertDialog
const API_BASE = import.meta.env.VITE_API_BASE_URL

export function downloadFile(att: Attachment) {
  const link = document.createElement("a")
  link.href = `${API_BASE + ":8080"}${"/files/download/" + att.id}`
  link.download = att.originalFileName
  link.click()
}

export async function deleteFile(
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