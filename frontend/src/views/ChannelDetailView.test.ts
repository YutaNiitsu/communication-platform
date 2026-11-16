import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ChannelDetail from '@/views/ChannelDetailView.vue'

// Vue Router のモック
vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: { id: '1' },
    query: {},
  }),
  useRouter: () => ({
    push: vi.fn(),
  }),
}))

// emoji-mart-vue-fast のモック
vi.mock('emoji-mart-vue-fast/src', () => ({
  Picker: {
    name: 'Picker',
    template: '<div class="mock-emoji-picker" />',
  },
  EmojiIndex: class {
    constructor() {}
  },
}))

describe('ChannelDetailView (ChannelDetail.vue)', () => {
  let pinia: any

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()
    vi.spyOn(window, 'alert').mockImplementation(() => {})
  })

  const mountComponent = () => {
    return mount(ChannelDetail, {
      global: {
        plugins: [pinia],
        stubs: {
          ChannelInfoModal: {
            name: 'ChannelInfoModal',
            template: '<div class="mock-channel-info-modal" />',
          },
          Picker: true,
        },
      },
    })
  }

  // 共通のストア初期モック設定
  const setupStoreMocks = async () => {
    const { useChannelStore } = await import('@/stores/useChannelStore')
    const { useMessageStore } = await import('@/stores/useMessageStore')
    const { useMemberStore } = await import('@/stores/useMemberStore')
    const { useUserStore } = await import('@/stores/useUserStore')
    const { useAuthStore } = await import('@/stores/useAuthStore')
    const { useMessageTemplateStore } = await import('@/stores/useMessageTemplateStore')

    const channelStore = useChannelStore()
    const messageStore = useMessageStore()
    const memberStore = useMemberStore()
    const userStore = useUserStore()
    const authStore = useAuthStore()
    const templateStore = useMessageTemplateStore()

    // デフォルトの共通モック（非同期でネットワークが走らないように空配列等を返す）
    vi.spyOn(channelStore, 'fetchChannels').mockResolvedValue([] as any)
    vi.spyOn(channelStore, 'fetchChannelById').mockResolvedValue({ id: 1, name: 'general', createdAt: '2026-01-01T00:00:00Z' } as any)
    vi.spyOn(messageStore, 'fetchMessages').mockResolvedValue([] as any)
    vi.spyOn(memberStore, 'fetchMembers').mockResolvedValue([] as any)
    vi.spyOn(userStore, 'fetchAllUsers').mockResolvedValue([] as any)
    vi.spyOn(templateStore, 'fetchTemplates').mockResolvedValue([] as any)

    return { channelStore, messageStore, memberStore, userStore, authStore, templateStore }
  }

  // 1. チャンネル詳細・メッセージ一覧・各種操作の正常系（結果表 項番3）
  it('チャンネル詳細およびメッセージ一覧が描画され、メッセージ送信等の操作がストア経由で正常に動作すること', async () => {
    const { channelStore, messageStore, userStore, authStore } = await setupStoreMocks()

    authStore.user = { userId: 1, username: 'Alice', role: 0, isActive: true }
    channelStore.currentChannel = { id: 1, name: 'general', status: 0, createdAt: '2026-01-01T00:00:00Z' }

    vi.spyOn(channelStore, 'fetchChannelById').mockResolvedValue(channelStore.currentChannel as any)
    vi.spyOn(userStore, 'fetchAllUsers').mockResolvedValue([{ userId: 1, username: 'Alice', role: 0, isActive: true }] as any)
    vi.spyOn(messageStore, 'sendMessage').mockResolvedValue({} as any)

    const wrapper = mountComponent()
    await flushPromises()

    // 描画確認
    expect(wrapper.find('.channel-name').text()).toBe('general')

    // メッセージ送信テスト
    const textarea = wrapper.find('textarea')
    await textarea.setValue('テストメッセージ送信')

    const form = wrapper.find('.channel-detail__form')
    await form.trigger('submit.prevent')

    // ★ 引数の userId (1) を削除し、sendMessage の定義（channelId, content, replyToMessageId）に合わせる
    expect(messageStore.sendMessage).toHaveBeenCalledWith(1, 'テストメッセージ送信')
  })

  // 2. メッセージ超過・空送信時のガード確認
  it('1000文字を超えるメッセージを入力して送信しようとした場合、アラートが表示され送信処理がブロックされること', async () => {
    const { authStore, messageStore } = await setupStoreMocks()

    authStore.user = { userId: 1, username: 'Alice', role: 0, isActive: true }
    const alertSpy = vi.spyOn(window, 'alert')
    vi.spyOn(messageStore, 'sendMessage').mockResolvedValue({} as any)

    const wrapper = mountComponent()
    await flushPromises()

    const longText = 'a'.repeat(1001)
    const textarea = wrapper.find('textarea')
    await textarea.setValue(longText)

    const form = wrapper.find('.channel-detail__form')
    await form.trigger('submit.prevent')

    expect(alertSpy).toHaveBeenCalledWith('メッセージは1000文字以内で入力してください。')
    expect(messageStore.sendMessage).not.toHaveBeenCalled()
  })

  // 3. リアクション・スレッドペイン等の総合動作確認
  it('スレッド返信ボタンをクリックすると、activeThreadMessageにセットされスレッドサイドバーが開閉すること', async () => {
    const { authStore, messageStore } = await setupStoreMocks()

    authStore.user = { userId: 1, username: 'Alice', role: 0, isActive: true }
    
    const mockMessages = [
      { 
        id: 10, 
        channelId: 1, 
        userId: 1, 
        username: 'Alice', 
        content: '親メッセージ', 
        status: 0,
        createdAt: '2026-09-04T12:00:00Z', 
        updatedAt: null,
        replyToMessageId: null, // ★ 追加
        reactions: [],
        mentionedUserIds: [] 
      },
      { 
        id: 11, 
        channelId: 1, 
        userId: 2, 
        username: 'Bob', 
        content: 'スレッドへの返信', 
        status: 0,
        createdAt: '2026-09-04T12:05:00Z', 
        updatedAt: null,
        replyToMessageId: 10,
        reactions: [],
        mentionedUserIds: []
      }
    ]

    vi.spyOn(messageStore, 'fetchMessages').mockResolvedValue(mockMessages as any)
    messageStore.messages = mockMessages

    const wrapper = mountComponent()
    await flushPromises()

    // スレッド返信ボタンをクリック
    const threadBtn = wrapper.find('.thread-summary-btn')
    expect(threadBtn.exists()).toBe(true)
    await threadBtn.trigger('click')

    // スレッドサイドバーが表示されること
    expect(wrapper.find('.thread-sidebar').exists()).toBe(true)
    expect(wrapper.find('.thread-title').text()).toBe('スレッド')

    // スレッドを閉じる
    const closeBtn = wrapper.find('.thread-close-btn')
    await closeBtn.trigger('click')
    expect(wrapper.find('.thread-sidebar').exists()).toBe(false)
  })
})