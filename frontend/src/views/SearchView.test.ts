import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import SearchView from '@/views/SearchView.vue'
import { useSearchStore } from '@/stores/useSearchStore'
import { useChannelStore } from '@/stores/useChannelStore'
import { useUserStore } from '@/stores/useUserStore'
import { useAuthStore } from '@/stores/useAuthStore'

// 各ストアをモック化して確実にアクションをスパイできるようにする
vi.mock('@/stores/useSearchStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useSearchStore')>()
  return {
    ...actual,
    useSearchStore: vi.fn(),
  }
})

vi.mock('@/stores/useChannelStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useChannelStore')>()
  return {
    ...actual,
    useChannelStore: vi.fn(),
  }
})

vi.mock('@/stores/useUserStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useUserStore')>()
  return {
    ...actual,
    useUserStore: vi.fn(),
  }
})

vi.mock('@/stores/useAuthStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useAuthStore')>()
  return {
    ...actual,
    useAuthStore: vi.fn(),
  }
})

// vue-router のモック
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}))

describe('SearchView.vue', () => {
  let mockFetchChannels: ReturnType<typeof vi.fn>
  let mockFetchAllUsers: ReturnType<typeof vi.fn>
  let mockSearch: ReturnType<typeof vi.fn>
  
  let channelsData: any[]
  let usersData: any[]
  let searchResultsData: any[]
  let searchErrorState: string | null
  let authUserState: any

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    mockFetchChannels = vi.fn().mockResolvedValue(undefined)
    mockFetchAllUsers = vi.fn().mockResolvedValue(undefined)
    mockSearch = vi.fn().mockImplementation(async () => {
      // 検索実行時のモック挙動
    })

    channelsData = []
    usersData = []
    searchResultsData = []
    searchErrorState = null
    authUserState = { userId: 10, username: 'testuser', role: 'member', isActive: true }

    // 各ストアのモック戻り値を設定
    ;(useChannelStore as any).mockReturnValue({
      get channels() { return channelsData },
      set channels(val) { channelsData = val },
      fetchChannels: mockFetchChannels,
    })

    ;(useUserStore as any).mockReturnValue({
      get users() { return usersData },
      set users(val) { usersData = val },
      fetchAllUsers: mockFetchAllUsers,
    })

    ;(useSearchStore as any).mockReturnValue({
      get results() { return searchResultsData },
      set results(val) { searchResultsData = val },
      get error() { return searchErrorState },
      set error(val) { searchErrorState = val },
      get hasResults() { return searchResultsData.length > 0 },
      loading: false,
      setQuery: vi.fn(),
      setChannelIdFilter: vi.fn(),
      setUserFilter: vi.fn(),
      setDateRange: vi.fn(),
      search: mockSearch,
    })

    ;(useAuthStore as any).mockReturnValue({
      get user() { return authUserState },
      set user(val) { authUserState = val },
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // 1. 初期マウント確認
  it('マウント時に channelStore.fetchChannels と userStore.fetchAllUsers が呼び出されること', async () => {
    mount(SearchView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    expect(mockFetchChannels).toHaveBeenCalledTimes(1)
    expect(mockFetchAllUsers).toHaveBeenCalledTimes(1)
  })

  // 2. 正常系: 複数条件による検索実行と、結果クリック時のジャンプ確認
  it('複数条件で検索が実行され、結果項目をクリックするとハイライト付きで該当チャンネルへジャンプすること', async () => {
    channelsData = [{ id: 1, name: 'general' }]
    usersData = [{ userId: 2, username: 'alice' }]

    mockSearch.mockImplementation(async () => {
      searchResultsData = [
        {
          id: 100,
          channelId: 1,
          userId: 2,
          content: 'テストメッセージ',
          createdAt: '2026-09-07T12:00:00',
        },
      ]
    })

    const wrapper = mount(SearchView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    // 検索条件の入力
    await wrapper.find('input[placeholder="キーワードを入力"]').setValue('テスト')
    await wrapper.find('input[list="channel-options"]').setValue('general')
    await wrapper.find('input[list="user-options"]').setValue('alice')
    await wrapper.find('input[type="date"]').setValue('2026-09-01')

    // フォーム送信
    await wrapper.find('form.search__form').trigger('submit.prevent')
    await flushPromises()

    expect(mockSearch).toHaveBeenCalledWith(10)

    // 検索結果項目をクリック
    const item = wrapper.find('.search__result-item')
    expect(item.exists()).toBe(true)
    await item.trigger('click')

    // ハイライト付きで該当チャンネルへジャンプしていることの検証
    expect(mockPush).toHaveBeenCalledWith({
      path: '/home/1',
      query: { highlightMessageId: '100' },
    })
  })

  // 3. 異常系: API通信エラー発生時のエラーメッセージ表示確認
  it('検索処理でエラーが発生した場合、searchStore.error に格納されたエラーメッセージが画面に表示されること', async () => {
    const errorMsg = '検索処理に失敗しました'
    mockSearch.mockImplementation(async () => {
      searchErrorState = errorMsg
    })

    const wrapper = mount(SearchView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    // 検索実行
    await wrapper.find('form.search__form').trigger('submit.prevent')
    await flushPromises()

    // エラーメッセージが画面上に適切に表示されていることの検証
    const errorElement = wrapper.find('.error-message')
    expect(errorElement.exists()).toBe(true)
    expect(errorElement.text()).toBe(errorMsg)
  })
})