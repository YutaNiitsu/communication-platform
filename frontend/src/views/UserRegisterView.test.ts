import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import UserRegisterView from '@/views/UserRegisterView.vue'
import { useUserStore } from '@/stores/useUserStore'

// vue-router のモック設定（route.params や push / back を制御）
let mockRouteParams = {}
const mockPush = vi.fn()
const mockBack = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: mockRouteParams,
  }),
  useRouter: () => ({
    push: mockPush,
    back: mockBack,
  }),
}))

// useUserStore のモック化
vi.mock('@/stores/useUserStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useUserStore')>()
  return {
    ...actual,
    useUserStore: vi.fn(),
  }
})

describe('UserRegisterView.vue (テスト結果表 項番100, 101)', () => {
  let mockFetchUserById: ReturnType<typeof vi.fn>
  let mockCreateUser: ReturnType<typeof vi.fn>
  let mockUpdateUser: ReturnType<typeof vi.fn>

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    mockFetchUserById = vi.fn().mockResolvedValue({
      userId: 1,
      username: 'target_user',
      role: 0,
      isActive: true,
    })
    mockCreateUser = vi.fn().mockResolvedValue(undefined)
    mockUpdateUser = vi.fn().mockResolvedValue(undefined)

    ;(useUserStore as any).mockReturnValue({
      fetchUserById: mockFetchUserById,
      createUser: mockCreateUser,
      updateUser: mockUpdateUser,
    })

    vi.spyOn(window, 'alert').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
    mockRouteParams = {}
  })

  // 1. 新規登録モードの初期表示と登録処理テスト (項番100)
  it('IDがない場合、新規登録モードとなり、フォーム送信時に createUser が呼ばれて一覧へ遷移すること', async () => {
    mockRouteParams = {} // 新規作成（idなし）

    const wrapper = mount(UserRegisterView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    // タイトルが「ユーザー登録」になっていること
    expect(wrapper.find('.page-title').text()).toBe('ユーザー登録')

    // フォーム入力
    await wrapper.find('input[type="text"]').setValue('new_user')
    await wrapper.find('input.password-input').setValue('password123')

    // フォーム送信
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(mockCreateUser).toHaveBeenCalledWith({
      username: 'new_user',
      password: 'password123',
      role: 1, // デフォルト一般ユーザー
      isActive: true,
    })
    expect(window.alert).toHaveBeenCalledWith('ユーザーを登録しました')
    expect(mockPush).toHaveBeenCalledWith('/users')
  })

  // 2. 編集モードの初期データ埋め込みと更新処理テスト (項番100)
  it('IDが存在する場合、編集モードとなり、既存データが埋め込まれ、送信時に updateUser が呼ばれること', async () => {
    mockRouteParams = { id: '1' } // 編集モード

    const wrapper = mount(UserRegisterView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    // タイトルが「ユーザー更新」になっており、フェッチされたデータが反映されていること
    expect(wrapper.find('.page-title').text()).toBe('ユーザー更新')
    expect(mockFetchUserById).toHaveBeenCalledWith(1)

    // ユーザー名入力欄に既存値が入っているか
    const usernameInput = wrapper.find('input[type="text"]') as any
    expect(usernameInput.element.value).toBe('target_user')

    // 更新送信
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(mockUpdateUser).toHaveBeenCalledWith(1, expect.objectContaining({
      username: 'target_user',
      role: 0,
      isActive: true,
    }))
    expect(window.alert).toHaveBeenCalledWith('ユーザーを更新しました')
    expect(mockPush).toHaveBeenCalledWith('/users')
  })

  // 3. パスワード表示切替ボタンのテスト
  it('「表示」ボタンをクリックするとパスワード入力欄のクラス（表示切替）が切り替わること', async () => {
    mockRouteParams = {}
    const wrapper = mount(UserRegisterView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    const passwordInput = wrapper.find('input.password-input')
    const toggleBtn = wrapper.find('button.password-toggle-btn')

    expect(passwordInput.classes()).not.toContain('show-text')
    expect(toggleBtn.text()).toBe('表示')

    // トグルボタン押下
    await toggleBtn.trigger('click')
    expect(passwordInput.classes()).toContain('show-text')
    expect(toggleBtn.text()).toBe('隠す')
  })

  // 4. エラーハンドリングのテスト
  it('API呼び出しでエラーが発生した場合、エラーログが出力され「処理に失敗しました」のアラートが表示されること', async () => {
    mockRouteParams = {}
    mockCreateUser.mockRejectedValueOnce(new Error('API Error'))

    const wrapper = mount(UserRegisterView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    await wrapper.find('input[type="text"]').setValue('error_user')
    await wrapper.find('input.password-input').setValue('password123')

    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(window.alert).toHaveBeenCalledWith('処理に失敗しました')
    // 失敗時は画面遷移しないこと
    expect(mockPush).not.toHaveBeenCalled()
  })

  // 5. キャンセルボタンのテスト
  it('キャンセルボタンをクリックすると router.back() が実行されること', async () => {
    mockRouteParams = {}
    const wrapper = mount(UserRegisterView, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    const cancelBtn = wrapper.find('button.btn-secondary')
    await cancelBtn.trigger('click')

    expect(mockBack).toHaveBeenCalledTimes(1)
  })
})