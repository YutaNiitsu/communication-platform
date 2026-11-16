import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ChannelListView from '@/views/ChannelListView.vue'

// チャンネル一覧のモック
vi.mock('@/clients/channelClient', () => ({
  ChannelClient: {
    getChannels: vi.fn().mockResolvedValue([
      { id: 1, name: 'General', createdAt: '2026-09-01T00:00:00Z' },
      { id: 2, name: 'Random', createdAt: '2026-09-02T00:00:00Z' },
    ]),
  },
}))

// vue-router のモック
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: {},
    path: '/home',
  }),
  useRouter: () => ({
    push: mockPush,
  }),
}))

describe('ChannelListView.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('チャンネル一覧が正しく表示され、チャンネル選択や作成ボタンで正しいルートへ遷移すること', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)

    const wrapper = mount(ChannelListView, {
      global: {
        plugins: [pinia],
      },
    })
    await flushPromises()

    // 1. チャンネル一覧の表示確認 (.channel-item)
    const items = wrapper.findAll('.channel-item')
    expect(items.length).toBe(2)
    expect(items[0].text()).toContain('General')
    expect(items[1].text()).toContain('Random')

    // 2. チャンネル項目をクリック -> /home/1 へ遷移すること
    await items[0].trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/home/1')

    // 3. チャンネル作成ボタンをクリック -> /home/channels/create へ遷移すること
    const createBtn = wrapper.find('.sidebar__create-btn')
    expect(createBtn.exists()).toBe(true)
    await createBtn.trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/home/channels/create')
  })
})