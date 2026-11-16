import { describe, it, expect, vi, beforeEach } from "vitest"
import { MessageTemplateClient } from "@/clients/MessageTemplateClient"
import { api } from "@/services/api"

// apiモジュールをモック化
vi.mock("@/services/api", () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe("MessageTemplateClient", () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe("カテゴリ関連 (Message Template Categories)", () => {
    it("getCategories: テンプレートカテゴリ一覧取得APIの呼び出し確認", async () => {
      const mockCategories = [
        { id: 1, userId: 1, name: "一般", isShared: true, createdAt: "2026-01-01", updatedAt: "2026-01-01" },
      ]
      vi.mocked(api.get).mockResolvedValueOnce({ data: mockCategories })

      const result = await MessageTemplateClient.getCategories(true)

      expect(api.get).toHaveBeenCalledTimes(1)
      expect(api.get).toHaveBeenCalledWith("/message_template_categories", { params: { is_shared: true } })
      expect(result).toEqual({ data: mockCategories })
    })

    it("createCategory: テンプレートカテゴリ作成APIの呼び出し確認", async () => {
      const payload = { name: "新規カテゴリ", isShared: false }
      const mockResponse = { id: 2, userId: 1, name: "新規カテゴリ", isShared: false, createdAt: "2026-01-01", updatedAt: "2026-01-01" }
      vi.mocked(api.post).mockResolvedValueOnce({ data: mockResponse })

      const result = await MessageTemplateClient.createCategory(payload)

      expect(api.post).toHaveBeenCalledTimes(1)
      expect(api.post).toHaveBeenCalledWith("/message_template_categories", {
        name: "新規カテゴリ",
        is_shared: false,
      })
      expect(result).toEqual({ data: mockResponse })
    })

    it("updateCategory: テンプレートカテゴリ更新APIの呼び出し確認", async () => {
      const payload = { name: "更新カテゴリ", isShared: true }
      const mockResponse = { id: 1, userId: 1, name: "更新カテゴリ", isShared: true, createdAt: "2026-01-01", updatedAt: "2026-01-02" }
      vi.mocked(api.put).mockResolvedValueOnce({ data: mockResponse })

      const result = await MessageTemplateClient.updateCategory(1, payload)

      expect(api.put).toHaveBeenCalledTimes(1)
      expect(api.put).toHaveBeenCalledWith("/message_template_categories/1", {
        name: "更新カテゴリ",
        is_shared: true,
      })
      expect(result).toEqual({ data: mockResponse })
    })

    it("deleteCategory: テンプレートカテゴリ削除APIの呼び出し確認", async () => {
      vi.mocked(api.delete).mockResolvedValueOnce({ data: { success: true } })

      const result = await MessageTemplateClient.deleteCategory(1)

      expect(api.delete).toHaveBeenCalledTimes(1)
      expect(api.delete).toHaveBeenCalledWith("/message_template_categories/1")
      expect(result).toEqual({ data: { success: true } })
    })
  })

  describe("テンプレート関連 (Message Templates)", () => {
    it("getTemplates: メッセージテンプレート一覧取得APIの呼び出し確認", async () => {
      const mockTemplates = [
        { id: 1, userId: 1, title: "挨拶", content: "お疲れ様です。", categoryId: 1, isShared: true, createdAt: "2026-01-01", updatedAt: "2026-01-01" },
      ]
      vi.mocked(api.get).mockResolvedValueOnce({ data: mockTemplates })

      const result = await MessageTemplateClient.getTemplates()

      expect(api.get).toHaveBeenCalledTimes(1)
      expect(api.get).toHaveBeenCalledWith("/message_templates", { params: {} })
      expect(result).toEqual({ data: mockTemplates })
    })

    it("createTemplate: メッセージテンプレート作成APIの呼び出し確認", async () => {
      const payload = { title: "質問", content: "質問があります。", categoryId: 1, isShared: false }
      const mockResponse = { id: 10, userId: 1, ...payload, createdAt: "2026-01-01", updatedAt: "2026-01-01" }
      vi.mocked(api.post).mockResolvedValueOnce({ data: mockResponse })

      const result = await MessageTemplateClient.createTemplate(payload)

      expect(api.post).toHaveBeenCalledTimes(1)
      expect(api.post).toHaveBeenCalledWith("/message_templates", {
        title: "質問",
        content: "質問があります。",
        category_id: 1,
        is_shared: false,
      })
      expect(result).toEqual({ data: mockResponse })
    })

    it("updateTemplate: メッセージテンプレート更新APIの呼び出し確認", async () => {
      const payload = { title: "修正挨拶", content: "いつもお世話になっております。", categoryId: 1, isShared: true }
      const mockResponse = { id: 1, userId: 1, ...payload, createdAt: "2026-01-01", updatedAt: "2026-01-02" }
      vi.mocked(api.put).mockResolvedValueOnce({ data: mockResponse })

      const result = await MessageTemplateClient.updateTemplate(1, payload)

      expect(api.put).toHaveBeenCalledTimes(1)
      expect(api.put).toHaveBeenCalledWith("/message_templates/1", {
        title: "修正挨拶",
        content: "いつもお世話になっております。",
        category_id: 1,
        is_shared: true,
      })
      expect(result).toEqual({ data: mockResponse })
    })

    it("deleteTemplate: メッセージテンプレート削除APIの呼び出し確認", async () => {
      vi.mocked(api.delete).mockResolvedValueOnce({ data: { success: true } })

      const result = await MessageTemplateClient.deleteTemplate(1)

      expect(api.delete).toHaveBeenCalledTimes(1)
      expect(api.delete).toHaveBeenCalledWith("/message_templates/1")
      expect(result).toEqual({ data: { success: true } })
    })
  })
})