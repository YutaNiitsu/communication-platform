// src/stores/MessageStore.ts
import { defineStore } from "pinia"
import { MessageClient } from "@/clients/MessageClient"
import { ReactionClient } from "@/clients/ReactionClient"
import { AttachmentClient } from "@/clients/AttachmentClient"

export interface ReactionUser {
  userId: number
}

export interface Reaction {
  emoji: string
  users: number[] // ユーザーIDの配列
}

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

export interface Message {
  id: number
  channelId: number
  userId: number
  replyToMessageId: number | null
  content: string
  mentionedUserIds: number[]
  status: number
  createdAt: string
  updatedAt: string | null
  deletedFlag?: boolean
  reactions?: Reaction[]
  attachments?: Attachment[]
}

export const useMessageStore = defineStore("message", {
  state: () => ({
    messages: [] as Message[]
  }),

  getters: {
    getMessagesByChannel: (state) => (channelId: number) => {
      const targetChannelId = Number(channelId)
      return state.messages.filter(m => Number(m.channelId) === targetChannelId && m.status !== 2)
    },
    latestMessageId: (state) => {
      if (state.messages.length === 0) return 0
      return Math.max(...state.messages.map(m => m.id))
    },
    // --- 追加: WebSocketServiceの再接続時（sync用）に使う、指定チャンネルの最新メッセージID ---
    getLastMessageId: (state) => (channelId: number) => {
      const targetChannelId = Number(channelId)
      const channelMessages = state.messages.filter(m => Number(m.channelId) === targetChannelId)
      if (channelMessages.length === 0) return 0
      return Math.max(...channelMessages.map(m => m.id))
    }
  },

  actions: {
    // --- 添付ファイル DTO 変換（UI 互換） ---
    normalizeAttachment(att: any): Attachment {
      return {
        id: att.id,
        messageId: Number(att.message_id ?? att.messageId),
        originalFileName: att.original_file_name ?? att.originalFileName,
        storedFileName: att.stored_file_name ?? att.storedFileName,
        fileSize: att.file_size ?? att.fileSize,
        fileType: att.file_type ?? att.fileType,
        fileUrl: att.file_url ?? att.fileUrl,
        thumbnailUrl: att.thumbnail_url ?? att.thumbnailUrl ?? null,
        createdAt: att.created_at ?? att.createdAt
      }
    },

    // 過去ログを遡って取得し、配列の先頭に追加する
    // サーバーの has_more フラグ（boolean）を返す
    async fetchPreviousMessages(channelId: number, oldestMessageId: number, range: number = 20) {
      // 動作確認用に遅延を入れる（本番では不要）
      await new Promise(resolve => setTimeout(resolve, 1500));

      const res = await MessageClient.getMessages(channelId, oldestMessageId, range)
      const rawMessagesArray = (res && Array.isArray(res.messages)) ? res.messages : []
      const serverHasMore = res ? (res.hasMore ?? res.has_more ?? false) : false
      
      if (rawMessagesArray.length === 0) {
        return false
      }

      const previousMessagesWithReactions = await Promise.all(
        rawMessagesArray.map(async (item: any) => {
          const m = item.message || item
          let reactions: Reaction[] = []
          try {
            if (item.reactions && Array.isArray(item.reactions)) {
              reactions = item.reactions.map((r: any) => ({
                emoji: r.reactionType || r.emoji,
                users: r.users || []
              }))
            }
          } catch (e) {
            reactions = []
          }

          const attachments = (item.attachments || []).map((att: any) =>
            this.normalizeAttachment(att)
          )

          return {
            id: Number(m.id),
            channelId: Number(m.channelId ?? m.channel_id),
            userId: Number(m.userId ?? m.user_id),
            replyToMessageId: (m.replyToMessageId ?? m.reply_to_message_id) ? Number(m.replyToMessageId ?? m.reply_to_message_id) : null,
            content: m.content,
            mentionedUserIds: m.mentionedUserIds ?? m.mentioned_user_ids ?? [],
            status: m.status ?? 0,
            createdAt: m.createdAt ?? m.created_at,
            updatedAt: m.updatedAt ?? m.updated_at ?? null,
            reactions: reactions,
            attachments: attachments
          }
        })
      )

      // 重複を防ぎつつ、既存のメッセージ配列の「先頭（過去側）」に結合する
      const existingIds = new Set(this.messages.map(m => m.id))
      const filteredNewMsgs = previousMessagesWithReactions.filter(m => !existingIds.has(m.id))
      
      this.messages = [...this.messages, ...filteredNewMsgs]
      
      return serverHasMore
    },

    async fetchMessages(channelId: number, range: number = 20) {
      // 最新のrange件だけ取得
      const res = await MessageClient.getMessages(channelId, undefined, range)

      // APIレスポンスから「messages」配列を安全に取り出す（無い場合は空配列にする）
      const rawMessagesArray = (res && Array.isArray(res.messages)) ? res.messages : [];

      const messagesWithReactions = await Promise.all(
        rawMessagesArray.map(async (item: any) => {
          // item.message からメッセージのメインデータを抽出
          const m = item.message || item; 

          let reactions: Reaction[] = []
          try {
            // APIレスポンス内の reactions を優先して使い、無ければ個別でAPIを叩く
            if (item.reactions && Array.isArray(item.reactions)) {
              reactions = item.reactions.map((r: any) => ({
                emoji: r.reactionType || r.emoji,
                users: r.users || []
              }))
            } else {
              const reactionRes = await ReactionClient.getReactions(m.id)
              reactions = (reactionRes || []).map((r: any) => ({
                emoji: r.reactionType || r.emoji,
                users: r.users || []
              }))
            }
          } catch (e) {
            reactions = []
          }

          const attachments = (item.attachments || []).map((att: any) =>
            this.normalizeAttachment(att)
          )

          return {
            id: Number(m.id),
            channelId: Number(m.channelId ?? m.channel_id),
            userId: Number(m.userId ?? m.user_id),
            replyToMessageId: (m.replyToMessageId ?? m.reply_to_message_id) 
              ? Number(m.replyToMessageId ?? m.reply_to_message_id) 
              : null,
            content: m.content,
            mentionedUserIds: m.mentionedUserIds ?? m.mentioned_user_ids ?? [],
            status: m.status ?? 0,
            createdAt: m.createdAt ?? m.created_at,
            updatedAt: m.updatedAt ?? m.updated_at ?? null,
            reactions: reactions,
            attachments: attachments
          }
        })
      )

      this.messages = messagesWithReactions
    },


    async sendMessage(channelId: number, content: string, replyToMessageId?: number | null,mentionedUserIds: number[] = []) {
      const res = await MessageClient.postMessage(channelId, content, replyToMessageId,mentionedUserIds)
      const m = res && res.message ? res.message : res
      if (!m || !m.id) return
      
      // 💡 修正: res.id ではなく、確実に抽出した m.id（数値型にキャスト）で重複チェックを行う
      const targetId = Number(m.id)
      const exists = this.messages.some(msg => Number(msg.id) === targetId)
      
      if (!exists) {
        const extractedMentions = res && Array.isArray(res.mentions)
          ? res.mentions.map((men: any) => Number(men.mentioned_user_id ?? men.userId ?? men.user_id))
          : mentionedUserIds.map((id: any) => Number(id))
          
        this.messages.unshift({
          id: targetId, // 💡 キャスト済みのIDを使用
          channelId: Number(m.channelId ?? m.channel_id ?? channelId),
          userId: Number(m.userId ?? m.user_id),
          replyToMessageId: (m.replyToMessageId ?? m.reply_to_message_id) 
            ? Number(m.replyToMessageId ?? m.reply_to_message_id) 
            : null,
          content: m.content,
          mentionedUserIds: extractedMentions,
          status: m.status ?? 0,
          // 💡 修正: WebSocket側の日時計算（getTime）と100%シンクロさせるため、ISOString 形式などで確実に揃える
          createdAt: m.createdAt ?? m.created_at ?? new Date().toISOString(),
          updatedAt: m.updatedAt ?? m.updated_at ?? null,
          reactions: [],
          attachments: []
        })
      }

      return m.id
    },


    async updateMessage(messageId: number, content: string, status: number = 0) {
      const res = await MessageClient.updateMessage(messageId, content, status)
      const updated = res.message || res
      const targetId = updated.id || messageId

      const target = this.messages.find(m => m.id === targetId)
      if (target) {
        target.content = updated.content ?? content
        target.status = updated.status ?? status
        target.updatedAt = updated.updatedAt ?? updated.updated_at ?? new Date().toISOString()
      }
    },

    async deleteMessage(messageId: number) {
      const res = await MessageClient.deleteMessage(messageId)
      const targetId = res.id || messageId
      const target = this.messages.find(m => m.id === targetId)
      if (target) {
        target.status = 2
        if (res.updatedAt || res.updated_at) {
          target.updatedAt = res.updatedAt ?? res.updated_at
        }
      }
    },

    async addReaction(messageId: number, emoji: string, currentUserId: number) {
      try {
        await ReactionClient.addReaction(messageId, emoji)
        this.updateLocalReaction(messageId, emoji, currentUserId, 'add')
      } catch (e) {
        console.error("Failed to add reaction:", e)
        throw e
      }
    },

    async removeReaction(messageId: number, emoji: string, currentUserId: number) {
      try {
        await ReactionClient.removeReaction(messageId, emoji)
        this.updateLocalReaction(messageId, emoji, currentUserId, 'remove')
      } catch (e) {
        console.error("Failed to remove reaction:", e)
        throw e
      }
    },

    updateLocalReaction(messageId: number, emoji: string, currentUserId: number, action: 'add' | 'remove') {
      const targetMsg = this.messages.find(m => m.id === messageId)
      if (!targetMsg) return

      if (!targetMsg.reactions) {
        targetMsg.reactions = []
      }

      const targetEmoji = String(emoji).trim()
      const numericUserId = Number(currentUserId)

      if (action === 'add') {
        const sameEmojiReactions = targetMsg.reactions.filter(r => String(r.emoji).trim() === targetEmoji)

        if (sameEmojiReactions.length > 0) {
          const primaryReaction = sameEmojiReactions[0]

          const allUsers = new Set<number>()
          sameEmojiReactions.forEach(r => {
            r.users.forEach(id => allUsers.add(Number(id)))
          })
          allUsers.add(numericUserId)

          primaryReaction.users = Array.from(allUsers)

          targetMsg.reactions = targetMsg.reactions.filter(r => String(r.emoji).trim() !== targetEmoji || r === primaryReaction)
        } else {
          targetMsg.reactions.push({
            emoji: targetEmoji,
            users: [numericUserId]
          })
        }
      } else {
        targetMsg.reactions.forEach(r => {
          if (String(r.emoji).trim() === targetEmoji) {
            r.users = r.users.filter(id => Number(id) !== numericUserId)
          }
        })
        targetMsg.reactions = targetMsg.reactions.filter(r => r.users.length > 0)
      }
    },

    async uploadAttachment(messageId: number, file: File) {
      const res = await AttachmentClient.upload(messageId, file)

      // --- 動作確認用に遅延を入れる（本番では不要） ---
      await new Promise(resolve => setTimeout(resolve, 1000));

      const att = res.attachment || res
      const targetMsg = this.messages.find(m => m.id === Number(att.message_id))
      if (!targetMsg) return

      if (!targetMsg.attachments) {
        targetMsg.attachments = []
      }

      // --- DTO 変換統一 ---
      const normalized = this.normalizeAttachment(att)

      targetMsg.attachments.push(normalized)

      // --- Pinia 再描画保証 ---
      targetMsg.attachments = [...targetMsg.attachments]
      this.messages = [...this.messages]
    },

    async getStorageUsage() {
      const res = await AttachmentClient.getStorageUsage()
      return res
    },

    async deleteAttachment(attachmentId: number) {
      const res = await AttachmentClient.delete(attachmentId)
      try {
        // 削除成功後、ローカルのメッセージ配列から該当の添付ファイルを削除する
        this.messages.forEach(msg => {
          if (msg.attachments) {
            msg.attachments = msg.attachments.filter(att => att.id !== attachmentId)
          }
        })
      } catch (error) {
        console.error("Failed to delete attachment:", error)
        throw error
      }
      return res
    },

    applyWebSocketEvent(event: any) {
      if (!event) return

      console.log("WebSocket event received:", event)

      const eventType = event.event || event.action
      const payload = event.message || event

      // --- 追加: 再接続時の差分同期結果（sync_result）のハンドリング ---
      if (eventType === "sync_result") {
        const syncMessages = event.messages || payload.messages || []
        console.log("WebSocket sync_result received:", syncMessages)

        for (const item of syncMessages) {
          const m = item.message
          if (!m || !m.id) continue

          const exists = this.messages.some(msg => msg.id === m.id)

          // リアクションデータの整形
          const reactions: Reaction[] = (item.reactions || []).map((r: any) => ({
            emoji: r.emoji || r.reactionType,
            users: r.users || []
          }))

          if (!exists) {
            this.messages.push({
              id: m.id,
              channelId: Number(m.channelId ?? m.channel_id),
              userId: Number(m.userId ?? m.user_id),
              replyToMessageId: Number(m.replyToMessageId ?? m.reply_to_message_id) || null,
              content: m.content,
              mentionedUserIds:
                m.mentionedUserIds ??
                m.mentioned_user_ids ??
                [],
              status: m.status ?? 0,
              createdAt: m.createdAt ?? m.created_at,
              updatedAt: m.updatedAt ?? m.updated_at ?? null,
              reactions: reactions,
              attachments: []
            })
          }
        }
        return
      }

      if (eventType === "message_created" || event.action === "message_created") {
        const m = payload && payload.message ? payload.message : (event.message || payload || event)
        if (!m || !m.id) return

        const id = Number(m.id)
        const channelId = Number(m.channelId ?? m.channel_id ?? event.channelId)
        const userId = Number(m.userId ?? m.user_id ?? event.userId)
        const content = m.content

        // IDによる標準の重複チェック
        let exists = this.messages.some(msg => msg.id === id)

        // 自分が送信した直後のWebSocketイベントの場合、
        // タイミングのズレでIDチェックをすり抜けることがあるため、内容と時間（3秒以内）で厳格に検知する
        if (!exists) {
          const now = new Date(m.createdAt ?? m.created_at ?? new Date()).getTime()
          
          exists = this.messages.some(msg => {
            const msgTime = new Date(msg.createdAt).getTime()
            return msg.channelId === channelId &&
                   msg.userId === userId &&
                   msg.content === content &&
                   Math.abs(now - msgTime) < 3000 // 3秒以内の同一内容の投稿は同一とみなす
          })
        }

        // 完全に新しいメッセージ（他人の投稿など）である場合のみ、配列の先頭に追加する
        if (!exists) {
          this.messages.unshift({
            id: id,
            channelId: channelId,
            userId: userId,
            replyToMessageId: (m.replyToMessageId ?? m.reply_to_message_id)
              ? Number(m.replyToMessageId ?? m.reply_to_message_id)
              : null,
            content: content,
            mentionedUserIds: (m.mentionedUserIds ?? m.mentioned_user_ids ?? []).map((uid: any) => Number(uid)),
            status: m.status ?? 0,
            createdAt: m.createdAt ?? m.created_at ?? new Date().toISOString(),
            updatedAt: m.updatedAt ?? m.updated_at ?? null,
            reactions: [],
            attachments: []
          })
        }
      }


      if (eventType === "message_updated" || event.action === "message_updated") {
        const targetId = payload.id ?? event.id
        const target = this.messages.find(m => m.id === targetId)
        if (target) {
          if (payload.content !== undefined) target.content = payload.content
          if (payload.status !== undefined) target.status = payload.status
          target.updatedAt = payload.updatedAt ?? event.updatedAt ?? payload.updated_at ?? new Date().toISOString()
        }
      }

      if (eventType === "message_deleted" || event.action === "message_deleted") {
        const targetId = payload.id ?? event.id
        const target = this.messages.find(m => m.id === targetId)
        if (target) {
          target.status = 2
          if (payload.updatedAt || event.updatedAt || payload.updated_at) {
            target.updatedAt = payload.updatedAt ?? event.updatedAt ?? payload.updated_at
          }
        }
      }

      if (eventType === "reaction_added" || eventType === "reaction_removed") {
        const messageId = payload.messageId ?? payload.message_id ?? event.message_id
        const emoji = payload.reactionType ?? payload.reaction_type ?? payload.emoji ?? event.reaction_type ?? event.emoji
        const userId = payload.userId ?? payload.user_id ?? event.user_id

        if (messageId !== undefined && emoji && userId !== undefined) {
          const actionType = eventType === "reaction_added" ? "add" : "remove"
          this.updateLocalReaction(Number(messageId), String(emoji), Number(userId), actionType)
        }
      }

      if (eventType === "attachment_added") {
        const att = event.attachment
        if (!att || !att.id) return

        console.log("WebSocket attachment_added received:", att)

        const targetMsg = this.messages.find(m => m.id === Number(att.message_id))
        if (!targetMsg) return

        if (!targetMsg.attachments) {
          targetMsg.attachments = []
        }

        // --- DTO 変換統一 ---
        const normalized = this.normalizeAttachment(att)

        targetMsg.attachments.push(normalized)

        

        // --- Pinia 再描画保証 ---
        targetMsg.attachments = [...targetMsg.attachments]
        this.messages = [...this.messages]
      }

      if (eventType === "attachment_removed") {
        const msgId = Number(event.message_id)
        const attId = event.attachment_id

        const targetMsg = this.messages.find(m => m.id === msgId)
        if (!targetMsg || !targetMsg.attachments) return

        targetMsg.attachments = targetMsg.attachments.filter(a => a.id !== attId)

        // --- 再描画保証 ---
        targetMsg.attachments = [...targetMsg.attachments]
        this.messages = [...this.messages]
      }


    }
  }
})