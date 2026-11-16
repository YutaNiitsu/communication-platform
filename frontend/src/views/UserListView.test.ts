import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import UserListView from '@/views/UserListView.vue'
import { useUserStore } from '@/stores/useUserStore'

// vue-router のモック
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}))

// userStore のモック化
vi.mock('@/stores/useUserStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useUserStore')>()
  return {
    ...actual,
    useUserStore: vi.fn(),
  }
})

describe('UserListView.vue (テスト結果表 項番99)', () => {
  let mockFetchAllUsers: ReturnType<typeof vi.fn>
  let mockDeleteUser: ReturnType<typeof vi.fn>
  let usersData: any[]

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    mockFetchAllUsers = vi.fn().mockResolvedValue(undefined)
    mockDeleteUser = vi.fn().mockResolvedValue(undefined)

    // テストデータ: 管理者(role: 0, 有効), 一般ユーザー(role: 1, 無効)
    usersData = [
      { userId: 1, username: 'admin_user', role: 0, isActive: true },
      { userId: 2, username: 'member_user', role: 1, isActive: false },
    ]

    ;(useUserStore as any).mockReturnValue({
      get users() { return usersData },
      set users(val) { usersData = val },
      fetchAllUsers: mockFetchAllUsers,
      deleteUser: mockDeleteUser,
    })

    vi.spyOn(window, 'confirm').mockReturnValue(true)
    vi.spyOn(window, 'alert').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // 1. マウント時のデータ取得と権限・ステータス表示の検証 (項番99)
  it('マウント時に userStore.fetchAllUsers が呼び出され、権限や有効/無効ステータスが正しく日本語表記で表示されること', async () => {
    const wrapper = mount(UserListView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    // fetchAllUsers が呼ばれていること
    expect(mockFetchAllUsers).toHaveBeenCalledTimes(1)

    // ユーザーカードが2件描画されていること
    const cards = wrapper.findAll('.user-card')
    expect(cards.length).toBe(2)

    // 1人目 (管理者 / 有効) の表示確認
    expect(cards[0].text()).toContain('admin_user')
    expect(cards[0].text()).toContain('管理者')
    expect(cards[0].text()).toContain('有効')

    // 2人目 (一般ユーザー / 無効) の表示確認
    expect(cards[1].text()).toContain('member_user')
    expect(cards[1].text()).toContain('一般ユーザー')
    expect(cards[1].text()).toContain('無効')
  })

  // 2. ユーザー新規登録画面への遷移確認
  it('「+ ユーザー新規登録」ボタンをクリックすると /users/create へ遷移すること', async () => {
    const wrapper = mount(UserListView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    const registerBtn = wrapper.find('button.btn-primary')
    expect(registerBtn.exists()).toBe(true)
    await registerBtn.trigger('click')

    expect(mockPush).toHaveBeenCalledWith('/users/create')
  })

  // 3. ユーザー更新画面への遷移確認
  it('各行の「更新」ボタンをクリックすると該当ユーザーのIDを含んだ編集画面へ遷移すること', async () => {
    const wrapper = mount(UserListView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    const cards = wrapper.findAll('.user-card')
    const updateBtn = cards[0].find('.btn-text-action')
    expect(updateBtn.exists()).toBe(true)
    await updateBtn.trigger('click')

    expect(mockPush).toHaveBeenCalledWith('/users/1/edit')
  })

  // 4. ユーザー削除確認と削除後の再フェッチ確認 
  it('「削除」ボタンをクリックし、確認ダイアログでOKを選択すると deleteUser が実行され、一覧が再取得されること', async () => {
    const wrapper = mount(UserListView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    // 呼び出し回数をリセット（マウント時の分）
    mockFetchAllUsers.mockClear()

    const cards = wrapper.findAll('.user-card')
    const deleteBtn = cards[0].find('.btn-danger')
    expect(deleteBtn.exists()).toBe(true)

    await deleteBtn.trigger('click')
    await flushPromises()

    // window.confirm が呼ばれたこと
    expect(window.confirm).toHaveBeenCalled()
    // deleteUser が該当ID (1) で実行されたこと
    expect(mockDeleteUser).toHaveBeenCalledWith(1)
    // 削除完了後に一覧が再取得されていること
    expect(mockFetchAllUsers).toHaveBeenCalledTimes(1)
  })
})