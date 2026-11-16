import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import TemplateListView from '@/views/TemplateListView.vue'
import { useMessageTemplateStore } from '@/stores/useMessageTemplateStore'
import { useAuthStore } from '@/stores/useAuthStore'

// ストアのモック化
vi.mock('@/stores/useMessageTemplateStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useMessageTemplateStore')>()
  return {
    ...actual,
    useMessageTemplateStore: vi.fn(),
  }
})

vi.mock('@/stores/useAuthStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useAuthStore')>()
  return {
    ...actual,
    useAuthStore: vi.fn(),
  }
})

describe('TemplateListView.vue (テスト結果表 項番97, 98)', () => {
  let mockFetchTemplates: ReturnType<typeof vi.fn>
  let mockFetchCategories: ReturnType<typeof vi.fn>
  let mockCreateTemplate: ReturnType<typeof vi.fn>
  let mockUpdateTemplate: ReturnType<typeof vi.fn>
  let mockDeleteTemplate: ReturnType<typeof vi.fn>

  let templatesData: any[]
  let categoriesData: any[]
  let authUserState: any
  let isUserAdmin: boolean

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    mockFetchTemplates = vi.fn().mockResolvedValue(undefined)
    mockFetchCategories = vi.fn().mockResolvedValue(undefined)
    mockCreateTemplate = vi.fn().mockResolvedValue(undefined)
    mockUpdateTemplate = vi.fn().mockResolvedValue(undefined)
    mockDeleteTemplate = vi.fn().mockResolvedValue(undefined)

    // テストデータ: 個人(userId=1), 他人の共有(userId=99)
    templatesData = [
      { id: 1, title: '個人テンプレート1', content: '定型文の内容A', categoryId: 10, isShared: false, userId: 1 },
      { id: 2, title: '共有テンプレート1', content: '定型文の内容B', categoryId: 20, isShared: true, userId: 99 },
    ]
    categoriesData = [
      { id: 10, name: '業務連絡' },
      { id: 20, name: '挨拶' },
    ]
    authUserState = { userId: 1, username: 'testuser', role: 'member', isActive: true }
    isUserAdmin = false

    ;(useMessageTemplateStore as any).mockReturnValue({
      get templates() { return templatesData },
      set templates(val) { templatesData = val },
      get categories() { return categoriesData },
      set categories(val) { categoriesData = val },
      fetchTemplates: mockFetchTemplates,
      fetchCategories: mockFetchCategories,
      createTemplate: mockCreateTemplate,
      updateTemplate: mockUpdateTemplate,
      deleteTemplate: mockDeleteTemplate,
    })

    ;(useAuthStore as any).mockReturnValue({
      get user() { return authUserState },
      get isAdmin() { return isUserAdmin },
    })

    vi.spyOn(window, 'confirm').mockReturnValue(true)
    vi.spyOn(window, 'alert').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // 1. 初期マウントと一覧・フィルター機能の検証 
  it('マウント時にテンプレート/カテゴリが取得され、個人/共有フィルター、カテゴリ、キーワードで正しく絞り込まれること', async () => {
    const wrapper = mount(TemplateListView, {
      global: {
        plugins: [createPinia()],
        stubs: { CategoryManagementModal: true, TemplateFormModal: true },
      },
    })

    await flushPromises()

    expect(mockFetchTemplates).toHaveBeenCalledTimes(1)
    expect(mockFetchCategories).toHaveBeenCalledTimes(1)

    // 初期状態は両方表示 (2件)
    expect(wrapper.findAll('.template-card').length).toBe(2)

    // チェックボックス操作: 共有テンプレートを非表示にする
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    await checkboxes[1].setValue(false) // filterShared = false
    expect(wrapper.findAll('.template-card').length).toBe(1)
    expect(wrapper.find('.template-card').text()).toContain('個人テンプレート1')

    // キーワード検索のテスト
    await checkboxes[1].setValue(true)
    const keywordInput = wrapper.find('input.form-input')
    await keywordInput.setValue('内容B')
    expect(wrapper.findAll('.template-card').length).toBe(1)
    expect(wrapper.find('.template-card').text()).toContain('共有テンプレート1')
  })

  // 2. 権限に応じた削除ボタンの表示制御テスト
  it('一般ユーザーの場合、自分の個人テンプレートには削除ボタンが表示され、他人の共有テンプレートには削除ボタンが表示されないこと', async () => {
    const wrapper = mount(TemplateListView, {
      global: {
        plugins: [createPinia()],
        stubs: { CategoryManagementModal: true, TemplateFormModal: true },
      },
    })

    await flushPromises()

    const cards = wrapper.findAll('.template-card')
    
    // 1番目（個人、作成者本人） -> 削除ボタンあり
    const deleteBtnCard1 = cards[0].find('.btn--danger')
    expect(deleteBtnCard1.exists()).toBe(true)

    // 2番目（共有、他人が作成・一般ユーザー） -> 削除ボタンなし（権限制御）
    const deleteBtnCard2 = cards[1].find('.btn--danger')
    expect(deleteBtnCard2.exists()).toBe(false)
  })

  // 3. 管理者権限による共有テンプレートの削除テスト 
  it('管理者の場合、共有テンプレートに対しても削除ボタンが表示され、正常に削除を実行できること', async () => {
    isUserAdmin = true // 管理者に昇格

    const wrapper = mount(TemplateListView, {
      global: {
        plugins: [createPinia()],
        stubs: { CategoryManagementModal: true, TemplateFormModal: true },
      },
    })

    await flushPromises()

    const cards = wrapper.findAll('.template-card')
    const deleteBtnCard2 = cards[1].find('.btn--danger')
    expect(deleteBtnCard2.exists()).toBe(true)

    // 削除実行
    await deleteBtnCard2.trigger('click')
    await flushPromises()

    expect(mockDeleteTemplate).toHaveBeenCalledWith(2)
  })

  // 4. 編集モーダルの権限・モード切替テスト
  it('一般ユーザーが他人の共有テンプレートの編集ボタンを押した場合、閲覧専用（view）モードでフォームモーダルが開くこと', async () => {
    const wrapper = mount(TemplateListView, {
      global: {
        plugins: [createPinia()],
        stubs: { CategoryManagementModal: true, TemplateFormModal: false },
      },
    })

    await flushPromises()

    const cards = wrapper.findAll('.template-card')
    // 2番目（共有テンプレート、他人のもの）の「編集」ボタンをクリック
    const editBtns = cards[1].findAll('.btn--secondary')
    await editBtns[0].trigger('click')
    await flushPromises()

    const formModal = wrapper.findComponent({ name: 'TemplateFormModal' })
    expect(formModal.exists()).toBe(true)
    // 閲覧専用モード（view）として渡されていることを検証
    expect(formModal.props('mode')).toBe('view')
  })
})