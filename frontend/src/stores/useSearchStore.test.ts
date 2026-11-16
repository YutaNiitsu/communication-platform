import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useSearchStore } from '@/stores/useSearchStore'
import { SearchClient } from '@/clients/SearchClient'

vi.mock('@/clients/SearchClient', () => ({
  SearchClient: {
    search: vi.fn(),
  },
}))

describe('SearchStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  // 1. state / getters (初期状態および hasResults ゲッターの確認)
  it('state / getters: 初期状態が正しく、hasResults ゲッターが results の有無に応じて正確に真偽値を返すこと', () => {
    const store = useSearchStore()
    expect(store.query).toBe('')
    expect(store.results).toEqual([])
    expect(store.hasResults).toBe(false)

    store.results = [{ id: 1, content: 'result' }]
    expect(store.hasResults).toBe(true)
  })

  // 2. setDateRange (日付範囲フィルターの設定および空文字補正)
  it('setDateRange: 日付範囲の空白や空文字が正しく null に補正され、有効な値がセットされること', () => {
    const store = useSearchStore()
    
    store.setDateRange('  2026-08-01  ', '   ')
    expect(store.dateFrom).toBe('2026-08-01')
    expect(store.dateTo).toBeNull()

    store.setDateRange('', '2026-08-31')
    expect(store.dateFrom).toBeNull()
    expect(store.dateTo).toBe('2026-08-31')
  })

  // 3. search (正常系: ローディングと結果格納の確認)
  it('search: 検索処理開始時に loading が true になり、成功時に results が格納され loading が false に戻ること', async () => {
    const mockResults = [
      { id: 10, channelId: 1, userId: 2, content: 'テストメッセージ', createdAt: '2026-01-01T00:00:00Z' }
    ]
    ;(SearchClient.search as ReturnType<typeof vi.fn>).mockResolvedValueOnce(mockResults)

    const store = useSearchStore()
    store.setQuery('テスト')

    const promise = store.search(1)
    expect(store.loading).toBe(true)
    expect(store.error).toBeNull()

    await promise

    expect(SearchClient.search).toHaveBeenCalledTimes(1)
    expect(store.results).toEqual(mockResults)
    expect(store.loading).toBe(false)
  })

  // 4. search (エラー系: 例外ハンドリングとエラーメッセージ設定の確認)
  it('search (異常系): API失敗時にエラーがキャッチされ、error メッセージが設定され results が空になり loading が解除されること', async () => {
    ;(SearchClient.search as ReturnType<typeof vi.fn>).mockRejectedValueOnce(new Error('API Error'))

    const store = useSearchStore()
    store.results = [{ id: 1 }] // 初期状態で何かデータが入っているとする

    await store.search(1)

    expect(store.error).toBe('検索処理中にエラーが発生しました。')
    expect(store.results).toEqual([])
    expect(store.loading).toBe(false)
  })

  // 5. search (フィルター条件の網羅確認)
  it('search (フィルター条件): キーワード、チャンネル、ユーザー、日付範囲などの全フィルター条件が正しくパラメータに渡ること', async () => {
    ;(SearchClient.search as ReturnType<typeof vi.fn>).mockResolvedValueOnce([])

    const store = useSearchStore()
    store.setQuery('キーワード')
    store.setChannelIdFilter(5)
    store.setUserFilter(10)
    store.setDateRange('2026-08-01', '2026-08-15')

    await store.search(99)

    expect(SearchClient.search).toHaveBeenCalledWith({
      keyword: 'キーワード',
      channelId: 5,
      userId: 10,
      from: '2026-08-01',
      to: '2026-08-15',
      requestUserId: 99,
    })
  })
})