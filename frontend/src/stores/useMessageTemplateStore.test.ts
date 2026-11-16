import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMessageTemplateStore } from '@/stores/useMessageTemplateStore'
import { useAuthStore } from '@/stores/useAuthStore'
import { MessageTemplateClient } from '@/clients/MessageTemplateClient'

vi.mock('@/clients/MessageTemplateClient', () => ({
  MessageTemplateClient: {
    getTemplates: vi.fn(),
    getCategories: vi.fn(),
    createTemplate: vi.fn(),
    updateTemplate: vi.fn(),
    deleteTemplate: vi.fn(),
    createCategory: vi.fn(),
    updateCategory: vi.fn(),
    deleteCategory: vi.fn(),
  },
}))

vi.mock('@/stores/useAuthStore', () => ({
  useAuthStore: vi.fn(),
}))

describe('MessageTemplateStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    
    // デフォルトの認証ユーザーモック
    vi.mocked(useAuthStore).mockReturnValue({
      user: { userId: 1, username: 'testuser', role: 0, isActive: true }
    } as any)
  })

  // 1. fetchTemplates & Categories (取得・重複排除マージ確認)
  describe('fetchTemplates & fetchCategories', () => {
    it('共通データと個人データが取得され、ID基準で重複なくマージされること', async () => {
      // 修正：sharedTemplates にも userId を持たせる（または as any で型チェックを合わせる）
      const sharedTemplates = [
        { id: 1, title: 'Shared 1', content: 'Content 1', categoryId: 1, isShared: true, userId: 0, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' }
      ]
      const personalTemplates = [
        { id: 1, title: 'Shared 1', content: 'Content 1', categoryId: 1, isShared: true, userId: 1, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' },
        { id: 2, title: 'Personal 2', content: 'Content 2', categoryId: 1, isShared: false, userId: 1, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' },
        { id: 3, title: 'Other User', content: 'Content 3', categoryId: 1, isShared: false, userId: 99, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' },
      ]

      vi.mocked(MessageTemplateClient.getTemplates)
        .mockResolvedValueOnce({ data: sharedTemplates } as any)
        .mockResolvedValueOnce({ data: personalTemplates } as any)

      const store = useMessageTemplateStore()
      await store.fetchTemplates()

      expect(store.templates.length).toBe(2)
      expect(store.templates).toEqual([
        { id: 1, title: 'Shared 1', content: 'Content 1', categoryId: 1, isShared: true, userId: 1, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' },
        { id: 2, title: 'Personal 2', content: 'Content 2', categoryId: 1, isShared: false, userId: 1, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' },
      ])
    })

    it('template api error: テンプレート取得APIが失敗した際に例外をキャッチし、安全に空配列にフォールバックすること', async () => {
      vi.mocked(MessageTemplateClient.getTemplates).mockRejectedValueOnce(new Error('Network Error'))

      const store = useMessageTemplateStore()
      await store.fetchTemplates()

      expect(store.templates).toEqual([])
      expect(store.loading).toBe(false)
    })
  })

  // 2. hasDuplicateTitle (タイトル重複チェック)
  describe('hasDuplicateTitle', () => {
    it('同一スコープ内での重複タイトルを正確に判定できること', () => {
      const store = useMessageTemplateStore()
      store.templates = [
        { id: 1, title: 'Report', content: 'C1', categoryId: 1, isShared: true, userId: 0, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' },
        { id: 2, title: 'Memo', content: 'C2', categoryId: 1, isShared: false, userId: 1, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' },
      ]

      // 共有テンプレートで同名が存在する場合
      expect(store.hasDuplicateTitle('Report', true)).toBe(true)
      // 共有テンプレートで別名の場合
      expect(store.hasDuplicateTitle('New Report', true)).toBe(false)

      // 個人テンプレートで同ユーザー・同名の場合
      expect(store.hasDuplicateTitle('Memo', false, undefined, 1)).toBe(true)
      // 個人テンプレートで別ユーザーの場合
      expect(store.hasDuplicateTitle('Memo', false, undefined, 2)).toBe(false)
    })
  })

  // 3. CRUDアクション & 異常系テスト
  describe('CRUD Actions (正常系 & 異常系)', () => {
    it('createTemplate: テンプレート作成後に一覧が再取得されること', async () => {
      vi.mocked(MessageTemplateClient.createTemplate).mockResolvedValueOnce({} as any)
      vi.mocked(MessageTemplateClient.getTemplates).mockResolvedValue({ data: [] } as any)

      const store = useMessageTemplateStore()
      await store.createTemplate({ title: 'New', content: 'Content', isShared: false, categoryId: 1 })

      expect(MessageTemplateClient.createTemplate).toHaveBeenCalledTimes(1)
      expect(MessageTemplateClient.getTemplates).toHaveBeenCalled()
    })

    it('createTemplate (異常系): 作成APIが失敗した場合に例外がスローされ、処理が中断されること', async () => {
      const store = useMessageTemplateStore()
      const mockError = new Error('Bad Request')
      vi.mocked(MessageTemplateClient.createTemplate).mockRejectedValueOnce(mockError)

      await expect(
        store.createTemplate({ title: 'New', content: 'Content', isShared: false, categoryId: 1 })
      ).rejects.toThrow('Bad Request')
    })

    it('deleteTemplate: テンプレート削除時にAPIが呼ばれ、ローカルストアから即時除外されること', async () => {
      const store = useMessageTemplateStore()
      store.templates = [
        { id: 1, title: 'Target', content: 'Content', categoryId: 1, isShared: false, userId: 1, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' }
      ]

      vi.mocked(MessageTemplateClient.deleteTemplate).mockResolvedValueOnce({} as any)

      await store.deleteTemplate(1)

      expect(MessageTemplateClient.deleteTemplate).toHaveBeenCalledWith(1)
      expect(store.templates).toHaveLength(0)
    })

    it('deleteTemplate (異常系): 削除APIが失敗した場合に例外がスローされ、ストア状態が保護されること', async () => {
      const store = useMessageTemplateStore()
      const target = { id: 1, title: 'Keep Me', content: 'Content', categoryId: 1, isShared: false, userId: 1, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' }
      store.templates = [target]

      const mockError = new Error('Forbidden')
      vi.mocked(MessageTemplateClient.deleteTemplate).mockRejectedValueOnce(mockError)

      await expect(store.deleteTemplate(1)).rejects.toThrow('Forbidden')
      
      // 削除に失敗しているため、ストアから消えていないこと
      expect(store.templates).toHaveLength(1)
      expect(store.templates[0].id).toBe(1)
    })
  })
})