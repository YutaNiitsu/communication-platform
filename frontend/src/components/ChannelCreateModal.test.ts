import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ChannelCreateModal from '@/components/ChannelCreateModal.vue' 
import { useChannelStore } from '@/stores/useChannelStore'
import { useUserStore } from '@/stores/useUserStore'
import { useAuthStore } from '@/stores/useAuthStore'
import { useMemberStore } from '@/stores/useMemberStore'

// モックの設定
const mockBack = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    back: mockBack,
  }),
}))

vi.mock('@/stores/useChannelStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useChannelStore')>()
  return { ...actual, useChannelStore: vi.fn() }
})

vi.mock('@/stores/useUserStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useUserStore')>()
  return { ...actual, useUserStore: vi.fn() }
})

vi.mock('@/stores/useAuthStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useAuthStore')>()
  return { ...actual, useAuthStore: vi.fn() }
})

vi.mock('@/stores/useMemberStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useMemberStore')>()
  return { ...actual, useMemberStore: vi.fn() }
})

describe('ChannelCreateModal.vue (テスト結果表 項番105, 106, 108)', () => {
  let mockFetchAllUsers: ReturnType<typeof vi.fn>
  let mockCreateChannel: ReturnType<typeof vi.fn>
  let mockAddMember: ReturnType<typeof vi.fn>

  const sampleUsers = [
    { userId: 1, username: 'admin_user', role: 0 }, // 管理者
    { userId: 2, username: 'normal_user', role: 1 }, // 一般ユーザー
  ]

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.useFakeTimers()

    mockFetchAllUsers = vi.fn().mockResolvedValue(sampleUsers)
    mockCreateChannel = vi.fn().mockResolvedValue({ id: 10, name: 'general' })
    mockAddMember = vi.fn().mockResolvedValue(undefined)

    ;(useUserStore as any).mockReturnValue({ fetchAllUsers: mockFetchAllUsers })
    ;(useChannelStore as any).mockReturnValue({ createChannel: mockCreateChannel })
    ;(useAuthStore as any).mockReturnValue({ user: { userId: 1, username: 'admin_user' } })
    ;(useMemberStore as any).mockReturnValue({ addMember: mockAddMember })
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.restoreAllMocks()
  })

  // 1. 初期表示 & ユーザー一覧の確認 
  it('初期表示時に userStore.fetchAllUsers が呼ばれ、全ユーザーがロール名（管理者/一般ユーザー）とともに一覧表示されること', async () => {
    const wrapper = mount(ChannelCreateModal, {
      global: { plugins: [createPinia()] },
    })

    await flushPromises()

    expect(mockFetchAllUsers).toHaveBeenCalledTimes(1)

    const userItems = wrapper.findAll('.user-item')
    expect(userItems.length).toBe(2)

    expect(userItems[0].text()).toContain('admin_user')
    expect(userItems[0].text()).toContain('管理者')

    expect(userItems[1].text()).toContain('normal_user')
    expect(userItems[1].text()).toContain('一般ユーザー')
  })

// 2. チャンネル作成 & 自動参加の確認
  it('チャンネル名を入力しユーザーを選択して作成すると、チャンネル作成後に作成者および選択されたユーザーが自動参加し、画面が戻ること', async () => {
    // チャンネル名に応じて動的に名前を返すようにモックを設定
    mockCreateChannel.mockImplementation(async (name: string) => ({ id: 10, name }))

    const wrapper = mount(ChannelCreateModal, {
      global: { plugins: [createPinia()] },
    })

    await flushPromises()

    // チャンネル名入力
    await wrapper.find('input[type="text"]').setValue('development')

    // ユーザーを選択
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    await checkboxes[0].setValue(true) // ID: 1 (作成者)
    await checkboxes[1].setValue(true) // ID: 2 (選択ユーザー)

    // 作成実行
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(mockCreateChannel).toHaveBeenCalledWith('development')
    expect(mockAddMember).toHaveBeenCalledWith(10, 1) // 作成者の自動参加
    expect(mockAddMember).toHaveBeenCalledWith(10, 2) // 選択ユーザーの参加

    expect(wrapper.find('.success-message').text()).toContain('チャンネル「development」を作成しました！')

    vi.advanceTimersByTime(800)
    expect(mockBack).toHaveBeenCalledTimes(1)
  })

  // 3. 異常系：API通信エラーのハンドリング確認
  it('チャンネル作成APIが失敗した場合、エラーメッセージが画面上のエラー領域に正しく表示されること', async () => {
    mockCreateChannel.mockRejectedValueOnce(new Error('チャンネルの作成に失敗しました'))

    const wrapper = mount(ChannelCreateModal, {
      global: { plugins: [createPinia()] },
    })

    await flushPromises()

    await wrapper.find('input[type="text"]').setValue('error-channel')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    const errorMessage = wrapper.find('.error-message')
    expect(errorMessage.exists()).toBe(true)
    expect(errorMessage.text()).toBe('チャンネルの作成に失敗しました')

    expect(mockBack).not.toHaveBeenCalled()
  })
})