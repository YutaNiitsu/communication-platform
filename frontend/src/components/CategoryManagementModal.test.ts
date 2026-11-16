import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import CategoryManagementModal from '@/components/CategoryManagementModal.vue'
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

describe('CategoryManagementModal.vue (テスト結果表 項番102, 103, 104)', () => {
  let mockCreateCategory: ReturnType<typeof vi.fn>
  let mockUpdateCategory: ReturnType<typeof vi.fn>
  let mockDeleteCategory: ReturnType<typeof vi.fn>

  let categoriesData: any[]
  let authUserState: any
  let isUserAdmin: boolean

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    mockCreateCategory = vi.fn().mockResolvedValue(undefined)
    mockUpdateCategory = vi.fn().mockResolvedValue(undefined)
    mockDeleteCategory = vi.fn().mockResolvedValue(undefined)

    // テストデータ:
    // 1. 自分の個人カテゴリ (userId: 1)
    // 2. 他人の個人カテゴリ (userId: 99)
    // 3. 共有カテゴリ (isShared: true)
    categoriesData = [
      { id: 1, name: 'マイカテゴリ', isShared: false, userId: 1 },
      { id: 2, name: '他人のカテゴリ', isShared: false, userId: 99 },
      { id: 3, name: '共有カテゴリ', isShared: true, userId: 99 },
    ]

    authUserState = { userId: 1, username: 'testuser', role: 'member', isActive: true }
    isUserAdmin = false

    ;(useMessageTemplateStore as any).mockReturnValue({
      get categories() { return categoriesData },
      set categories(val) { categoriesData = val },
      createCategory: mockCreateCategory,
      updateCategory: mockUpdateCategory,
      deleteCategory: mockDeleteCategory,
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

  // 1. 初期表示と権限に応じた操作ボタンの表示確認
  it('一般ユーザーの場合、自分の個人カテゴリには操作ボタンが表示され、他人の個人カテゴリや共有カテゴリには「—」が表示されること', async () => {
    const wrapper = mount(CategoryManagementModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    const rows = wrapper.findAll('tbody tr')
    expect(rows.length).toBe(3)

    // 1行目: マイカテゴリ (編集・削除ボタンあり)
    expect(rows[0].text()).toContain('マイカテゴリ')
    expect(rows[0].find('.btn-secondary').exists()).toBe(true)
    expect(rows[0].find('.btn-danger').exists()).toBe(true)

    // 2行目: 他人のカテゴリ (操作不可のため「—」)
    expect(rows[1].text()).toContain('他人のカテゴリ')
    expect(rows[1].text()).toContain('—')

    // 3行目: 共有カテゴリ (一般ユーザーのため「—」)
    expect(rows[2].text()).toContain('共有カテゴリ')
    expect(rows[2].text()).toContain('—')
  })

  // 2. 管理者権限における共有カテゴリの操作ボタン表示確認
  it('管理者の場合、共有カテゴリに対しても「編集」「削除」ボタンが表示されること', async () => {
    isUserAdmin = true // 管理者に設定

    const wrapper = mount(CategoryManagementModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    const rows = wrapper.findAll('tbody tr')
    // 3行目: 共有カテゴリ
    expect(rows[2].text()).toContain('共有カテゴリ')
    expect(rows[2].find('.btn-secondary').exists()).toBe(true)
    expect(rows[2].find('.btn-danger').exists()).toBe(true)
  })

  // 3. カテゴリ新規登録のテスト
  it('カテゴリ名を入力して「登録」をクリックすると createCategory が呼び出され、フォームがリセットされること', async () => {
    const wrapper = mount(CategoryManagementModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    const input = wrapper.find('input.form-input')
    await input.setValue('新カテゴリ')

    const registerBtn = wrapper.findAll('.btn-primary').find(b => b.text() === '登録')
    await registerBtn?.trigger('click')
    await flushPromises()

    expect(mockCreateCategory).toHaveBeenCalledWith({
      name: '新カテゴリ',
      isShared: false,
    })
    // フォームがリセットされ入力欄が空になること
    expect((input.element as HTMLInputElement).value).toBe('')
  })

  // 4. 空欄バリデーションのテスト
  it('カテゴリ名が空欄または空白のみの場合、「登録」をクリックしても createCategory が呼び出されないこと', async () => {
    const wrapper = mount(CategoryManagementModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    const input = wrapper.find('input.form-input')
    await input.setValue('   ') // 空白のみ

    const registerBtn = wrapper.findAll('.btn-primary').find(b => b.text() === '登録')
    await registerBtn?.trigger('click')
    await flushPromises()

    expect(mockCreateCategory).not.toHaveBeenCalled()
  })

  // 5. カテゴリ編集・更新のテスト 
  it('一覧の「編集」をクリックすると編集モードになり、更新時に updateCategory が呼び出されること', async () => {
    const wrapper = mount(CategoryManagementModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    const rows = wrapper.findAll('tbody tr')
    const editBtn = rows[0].find('.btn-secondary')
    await editBtn.trigger('click')
    await flushPromises()

    // ヘッダーが「カテゴリ編集」に変わり、値がセットされていること
    expect(wrapper.find('section.form-section h3').text()).toBe('カテゴリ編集')
    const input = wrapper.find('input.form-input') as any
    expect(input.element.value).toBe('マイカテゴリ')

    // 値を変更して更新
    await input.setValue('更新カテゴリ名')
    const updateBtn = wrapper.findAll('.btn-primary').find(b => b.text() === '更新')
    await updateBtn?.trigger('click')
    await flushPromises()

    expect(mockUpdateCategory).toHaveBeenCalledWith(1, {
      name: '更新カテゴリ名',
      isShared: false,
    })
  })

  // 6. カテゴリ削除のテスト
  it('「削除」をクリックして確認ダイアログでOKすると deleteCategory が呼び出されること', async () => {
    const wrapper = mount(CategoryManagementModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    const rows = wrapper.findAll('tbody tr')
    const deleteBtn = rows[0].find('.btn-danger')
    await deleteBtn.trigger('click')
    await flushPromises()

    expect(window.confirm).toHaveBeenCalled()
    expect(mockDeleteCategory).toHaveBeenCalledWith(1)
  })

  // 7. 閉じるイベントのテスト
  it('「閉じる」ボタンやオーバーレイをクリックすると close イベントが発火すること', async () => {
    const wrapper = mount(CategoryManagementModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    await flushPromises()

    const closeBtn = wrapper.find('.modal-footer .btn-secondary')
    await closeBtn.trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })
})