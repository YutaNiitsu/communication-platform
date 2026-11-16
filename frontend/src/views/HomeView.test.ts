import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import HomeView from '@/views/HomeView.vue'
import { useChannelStore } from '@/stores/useChannelStore'
import { webSocketService } from '@/services/websocketService'

// ChannelClient のモック
vi.mock('@/clients/channelClient', () => ({
  ChannelClient: {
    getChannels: vi.fn().mockResolvedValue([
      { id: 1, name: 'General', createdAt: '2026-09-01T00:00:00Z' },
      { id: 2, name: 'Random', createdAt: '2026-09-02T00:00:00Z' },
    ]),
  },
}))

// websocketService のモック
vi.mock('@/services/websocketService', () => ({
  webSocketService: {
    connect: vi.fn(),
    disconnect: vi.fn(),
    subscribe: vi.fn(),
    unsubscribe: vi.fn(),
  },
}))

// vue-router のリアクティブなモック制御
const mockPath = ref('/home')
const mockParams = ref<{ id?: string }>({})
const mockReplace = vi.fn()
const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => ({
    get path() { return mockPath.value },
    get params() { return mockParams.value },
  }),
  useRouter: () => ({
    replace: mockReplace,
    push: mockPush,
  }),
}))

describe('HomeView.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    mockPath.value = '/home'
    mockParams.value = {}
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // 1. マウント時の初期化とWebSocket接続・自動リダイレクトの確認
  it('マウント時にWebSocketが接続され、チャンネル一覧が取得され、ルートが /home の場合は最初のチャンネルへリダイレクトされること', async () => {
    mount(HomeView, {
      global: {
        plugins: [createPinia()],
      },
    })

    expect(webSocketService.connect).toHaveBeenCalledTimes(1)
    
    await flushPromises()

    const channelStore = useChannelStore()
    expect(channelStore.channels.length).toBe(2)

    // /home パスでチャンネルが存在する場合、最初のチャンネル (/home/1) へ replace されること
    expect(mockReplace).toHaveBeenCalledWith('/home/1')
  })

  // 2. サイドバーでのチャンネル描画とクリックによるルーティング確認
  it('サイドバーにチャンネル一覧が正しく表示され、クリックすると該当チャンネルのルートへ遷移すること', async () => {
    const wrapper = mount(HomeView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    const items = wrapper.findAll('.channel-item')
    expect(items.length).toBe(2)
    expect(items[0].text()).toContain('General')
    expect(items[1].text()).toContain('Random')

    // 2番目のチャンネルをクリック
    await items[1].trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/home/2')
  })

  // 3. チャンネル作成ボタンの動作確認
  it('最下部の「チャンネル作成」ボタンをクリックすると、作成用モーダルルートへ遷移すること', async () => {
    const wrapper = mount(HomeView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    const createBtn = wrapper.find('.sidebar__create-btn')
    expect(createBtn.exists()).toBe(true)

    await createBtn.trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/home/channels/create')
  })

  // 4. WebSocketの購読切り替え（watch の検証）
  it('チャンネルID（route.params.id）が切り替わった際、古いチャンネルのunsubscribeと新しいチャンネルのsubscribeが実行されること', async () => {
    // 初期状態でチャンネル1が選択されている状態を作る
    mockPath.value = '/home/1'
    mockParams.value = { id: '1' }

    mount(HomeView, {
      global: {
        plugins: [createPinia()],
      },
    })
    await flushPromises()

    // 初期マウント時の購読確認
    expect(webSocketService.subscribe).toHaveBeenCalledWith(1)

    // チャンネルを 1 から 2 へ切り替える
    mockPath.value = '/home/2'
    mockParams.value = { id: '2' }
    
    // Vueのwatchの非同期処理が完了するのを待つ
    await flushPromises()

    expect(webSocketService.unsubscribe).toHaveBeenCalledWith(1)
    expect(webSocketService.subscribe).toHaveBeenCalledWith(2)
  })

  // 5. アンマウント時のWebSocket切断確認
  it('コンポーネントがアンマウントされたとき、WebSocketが正常に切断されること', async () => {
    const wrapper = mount(HomeView, {
      global: {
        plugins: [createPinia()],
      },
    })
    await flushPromises()

    wrapper.unmount()
    expect(webSocketService.disconnect).toHaveBeenCalledTimes(1)
  })
})