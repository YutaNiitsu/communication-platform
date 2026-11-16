import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import TemplateFormModal from '@/components/TemplateFormModal.vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { useMessageTemplateStore } from '@/stores/useMessageTemplateStore'

// ストアのモック化
vi.mock('@/stores/useAuthStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useAuthStore')>()
  return { ...actual, useAuthStore: vi.fn() }
})

vi.mock('@/stores/useMessageTemplateStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/useMessageTemplateStore')>()
  return { ...actual, useMessageTemplateStore: vi.fn() }
})

describe('TemplateFormModal.vue (テスト結果表 項番113, 114, 116)', () => {
  let mockHasDuplicateTitle: ReturnType<typeof vi.fn>
  let categoriesData: any[]
  let authUserState: any
  let isUserAdmin: boolean

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    mockHasDuplicateTitle = vi.fn().mockReturnValue(false)
    categoriesData = [
      { id: 1, name: '日報', isShared: true },
      { id: 2, name: '連絡事項', isShared: false },
    ]
    authUserState = { userId: 1, username: 'testuser' }
    isUserAdmin = false

    ;(useAuthStore as any).mockReturnValue({
      get user() { return authUserState },
      get isAdmin() { return isUserAdmin },
    })

    ;(useMessageTemplateStore as any).mockReturnValue({
      get categories() { return categoriesData },
      hasDuplicateTitle: mockHasDuplicateTitle,
    })

    vi.spyOn(window, 'alert').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // 1. view モード（閲覧専用）のコントロール制限確認 (項番113)
  it('view モードの場合、入力項目が readonly になり、ラジオボタン・セレクトが disabled になること', async () => {
    const wrapper = mount(TemplateFormModal, {
      props: {
        mode: 'view',
        initialData: {
          id: 1,
          title: '閲覧用タイトル',
          categoryId: 1,
          content: '閲覧用本文',
          isShared: true,
          userId: 1,
        },
      },
      global: { plugins: [createPinia()] },
    })

    await flushPromises()

    // タイトルが閲覧専用モード表記になっていること
    expect(wrapper.find('.modal-title').text()).toContain('テンプレート編集（閲覧専用モード）')

    // 各入力項目が readonly / disabled であること
    expect(wrapper.find('input.form-input').attributes('readonly')).toBeDefined()
    expect(wrapper.find('textarea.form-textarea').attributes('readonly')).toBeDefined()
    expect(wrapper.find('select.form-select').attributes('disabled')).toBeDefined()

    // 保存ボタンが存在せず、「閉じる」のみであること
    const primaryBtn = wrapper.find('.btn--primary')
    expect(primaryBtn.exists()).toBe(false)
  })

  // 2. create モードでの初期値と新規作成保存の確認 -
  it('create モードでタイトルと本文を入力して保存すると、重複チェックを経て save イベントが発火すること', async () => {
    const wrapper = mount(TemplateFormModal, {
      props: {
        mode: 'create',
      },
      global: { plugins: [createPinia()] },
    })

    await flushPromises()

    // 入力
    await wrapper.find('input.form-input').setValue('新規テンプレート')
    await wrapper.find('textarea.form-textarea').setValue('本文内容です')

    // 保存ボタンをクリック
    const primaryBtn = wrapper.find('.btn--primary')
    await primaryBtn.trigger('click')
    await flushPromises()

    expect(mockHasDuplicateTitle).toHaveBeenCalledWith('新規テンプレート', false, undefined, 1)
    expect(wrapper.emitted('save')).toBeTruthy()
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  // 3. タイトル重複チェック異常系の確認 
  it('すでに同名のテンプレートが存在する場合、重複エラーのアラートが表示され保存が中断されること', async () => {
    mockHasDuplicateTitle.mockReturnValueOnce(true) // 重複あり

    const wrapper = mount(TemplateFormModal, {
      props: {
        mode: 'create',
      },
      global: { plugins: [createPinia()] },
    })

    await flushPromises()

    await wrapper.find('input.form-input').setValue('既存タイトル')
    await wrapper.find('textarea.form-textarea').setValue('本文')

    await wrapper.find('.btn--primary').trigger('click')
    await flushPromises()

    expect(window.alert).toHaveBeenCalledWith('すでに同じタイトルの「個人テンプレート」があなたのアカウントに存在します。')
    expect(wrapper.emitted('save')).toBeUndefined() // 保存イベントが発火しないこと
    expect(wrapper.emitted('close')).toBeUndefined()
  })

  // 4. 必須項目の未入力バリデーション確認 
  it('タイトルまたは本文が空欄の場合、アラートが表示され保存が中断されること', async () => {
    const wrapper = mount(TemplateFormModal, {
      props: {
        mode: 'create',
      },
      global: { plugins: [createPinia()] },
    })

    await flushPromises()

    // タイトル空のまま保存
    await wrapper.find('.btn--primary').trigger('click')
    await flushPromises()

    expect(window.alert).toHaveBeenCalledWith('タイトルと本文を入力してください')
    expect(wrapper.emitted('save')).toBeUndefined()
  })

  // 5. 文字数カウント表示の確認
  it('本文の文字数制限と現在の文字数が正しく表示されること', async () => {
    const wrapper = mount(TemplateFormModal, {
      props: {
        mode: 'create',
        initialData: {
          title: 'テスト',
          categoryId: 1,
          content: 'あいうえお',
          isShared: false,
        },
      },
      global: { plugins: [createPinia()] },
    })

    await flushPromises()

    expect(wrapper.find('.count').text()).toContain('現在の文字数 : 5 / 500')
  })
})