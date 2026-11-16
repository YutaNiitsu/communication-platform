import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ReactionClient } from '@/clients/ReactionClient'
import { api } from '@/services/api'

// apiモジュールをモック化
vi.mock('@/services/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('ReactionClient', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('addReaction: 指定したメッセージにリアクションを追加できること', async () => {
    const mockResponse = { success: true }
    vi.mocked(api.post).mockResolvedValueOnce({ data: mockResponse })

    const result = await ReactionClient.addReaction(100, '👍')

    expect(api.post).toHaveBeenCalledWith('/messages/100/reactions', {
      reaction_type: '👍',
    })
    expect(result).toEqual(mockResponse)
  })

  it('removeReaction: 指定したメッセージからリアクションを削除できること', async () => {
    const mockResponse = { success: true }
    vi.mocked(api.delete).mockResolvedValueOnce({ data: mockResponse })

    const result = await ReactionClient.removeReaction(100, '👍')

    expect(api.delete).toHaveBeenCalledWith('/messages/100/reactions', {
      data: { reaction_type: '👍' },
    })
    expect(result).toEqual(mockResponse)
  })

  it('getReactions: 指定したメッセージのリアクション一覧を取得できること', async () => {
    const mockReactions = [
      { reactionType: '👍', count: 2, users: [1, 2] },
    ]
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockReactions })

    const result = await ReactionClient.getReactions(100)

    expect(api.get).toHaveBeenCalledWith('/messages/100/reactions')
    expect(result).toEqual(mockReactions)
  })
})