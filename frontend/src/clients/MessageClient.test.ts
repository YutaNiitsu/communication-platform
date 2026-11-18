import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MessageClient } from '@/clients/MessageClient'
import { api } from '@/services/api'

// apiモジュールをモック化
vi.mock('@/services/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('MessageClient', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getMessages: 指定したチャンネルのメッセージ一覧を取得できること', async () => {
    const mockMessages = [{ id: 1, content: 'こんにちは' }]
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockMessages })

    const result = await MessageClient.getMessages(1)

    expect(api.get).toHaveBeenCalledWith('/channels/1/messages')
    expect(result).toEqual(mockMessages)
  })

  it('postMessage: メッセージが正しく投稿されること', async () => {
    const mockResponse = { id: 10, content: 'テスト' }
    vi.mocked(api.post).mockResolvedValueOnce({ data: mockResponse })

    // ★ 引数の userId (10) を削除し、正しい引数構成 (channelId, content, replyToMessageId) に修正
    const result = await MessageClient.postMessage(1, 'テスト', 'req1', null)

    expect(api.post).toHaveBeenCalledWith('/channels/1/messages', {
      content: 'テスト',
      reply_to_message_id: null,
      request_id: 'req1',
    })
    expect(result).toEqual(mockResponse)
  })

  it('updateMessage: メッセージが正しく更新されること', async () => {
    const mockResponse = { id: 100, content: '更新', status: 0 }
    vi.mocked(api.put).mockResolvedValueOnce({ data: mockResponse })

    const result = await MessageClient.updateMessage(100, '更新', 0)

    expect(api.put).toHaveBeenCalledWith('/messages/100', {
      content: '更新',
      status: 0,
    })
    expect(result).toEqual(mockResponse)
  })

  it('deleteMessage: メッセージが正しく削除されること', async () => {
    vi.mocked(api.delete).mockResolvedValueOnce({ data: { success: true } })

    const result = await MessageClient.deleteMessage(100)

    expect(api.delete).toHaveBeenCalledWith('/messages/100')
    expect(result).toEqual({ success: true })
  })

  describe("異常系: エラーハンドリング", () => {
    it("403 Forbidden または 404 Not Found などのエラーが適切にスローされること", async () => {
      const error = new Error("Not Found")
      vi.mocked(api.delete).mockRejectedValueOnce(error)

      await expect(MessageClient.deleteMessage(999)).rejects.toThrow("Not Found")
      expect(api.delete).toHaveBeenCalledWith("/messages/999")
    })
  })
})