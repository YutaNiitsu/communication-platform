// src/clients/MessageClient.ts
import { api } from "@/services/api"

export const MessageClient = {
  async getMessages(channelId: number, messageId?: number, range?: number) {
    // ベースとなるURLを組み立て（{id} の部分に channelId を埋め込む）
    let url = `/channels/${channelId}/messages`
    
    // オプショナルなパラメータが存在する場合、クエリ文字列を組み立てる
    const params = new URLSearchParams()
    
    if (messageId !== undefined && messageId !== null) {
      params.append('message_id', String(messageId))
    }
    if (range !== undefined && range !== null) {
      params.append('range', String(range))
    }

    // パラメータがセットされていれば、URLの末尾に「?〜」の形で結合する
    const queryString = params.toString()
    if (queryString) {
      url += `?${queryString}`
    }

    // 💡 3. 組み立てたURLでリクエストを送信
    const res = await api.get(url)
    return res.data
  },

  async postMessage(
    channelId: number,
    content: string,
    requestId: string,
    replyToMessageId?: number | null,
    mentionedUserIds: number[] = []
  ) {
    const res = await api.post(`/channels/${channelId}/messages`, {
      content,
      reply_to_message_id: replyToMessageId ?? null,
      mentioned_user_id: mentionedUserIds,
      request_id: requestId
    })
    return res.data
  },

  async updateMessage(messageId: number, content: string, status: number = 0) {
    const res = await api.put(`/messages/${messageId}`, { content, status })
    return res.data
  },

  async deleteMessage(messageId: number) {
    const res = await api.delete(`/messages/${messageId}`)
    return res.data
  },
}