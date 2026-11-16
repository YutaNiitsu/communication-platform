import { describe, it, expect, vi, beforeEach } from 'vitest'
import { SearchClient } from '@/clients/SearchClient'
import { api } from '@/services/api'

// apiモジュールをモック化
vi.mock('@/services/api', () => ({
  api: {
    get: vi.fn(),
  },
}))

describe('SearchClient', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('search: 指定したパラメータ（キーワードやユーザーID等）でメッセージ検索APIを呼び出せること', async () => {
    const mockSearchResults = [
      { id: 1, channelId: 1, userId: 10, content: 'テストメッセージ', createdAt: '2026-01-01T00:00:00Z' }
    ]
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockSearchResults })

    const params = {
      keyword: 'テスト',
      requestUserId: 10,
    }

    const result = await SearchClient.search(params)

    expect(api.get).toHaveBeenCalledWith('/search', {
      params: {
        keyword: 'テスト',
        channel_id: undefined,
        user_id: undefined,
        from: undefined,
        to: undefined,
        request_user_id: 10,
      },
    })
    expect(result).toEqual(mockSearchResults)
  })

  it('search (異常系): 検索API通信エラー時に例外が正しくスローされること', async () => {
    const mockError = new Error('Network Error')
    vi.mocked(api.get).mockRejectedValueOnce(mockError)

    const params = {
      keyword: 'エラーテスト',
      requestUserId: 10,
    }

    await expect(SearchClient.search(params)).rejects.toThrow('Network Error')
    expect(api.get).toHaveBeenCalledTimes(1)
  })
})