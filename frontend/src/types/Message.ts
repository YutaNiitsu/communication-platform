import type { Attachment } from "@/types/Attachment"

export interface ReactionUser {
  userId: number
}

export interface Reaction {
  emoji: string
  users: number[] // ユーザーIDの配列
}

export interface Message {
  id: number
  channelId: number
  userId: number
  replyToMessageId: number | null
  requestId: string
  content: string
  mentionedUserIds: number[]
  status: number
  createdAt: string
  updatedAt: string | null
  deletedFlag?: boolean
  reactions?: Reaction[]
  attachments?: Attachment[]
}