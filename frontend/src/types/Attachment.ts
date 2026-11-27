export interface Attachment {
  id: number
  messageId: number
  originalFileName: string
  storedFileName: string
  fileSize: number
  fileType: string
  fileUrl: string
  thumbnailUrl: string | null
  createdAt: string
}