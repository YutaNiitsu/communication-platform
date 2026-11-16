import { describe, it, expect, vi, beforeEach } from "vitest"
import { AuthClient } from "@/clients/AuthClient" 
import { api } from "@/services/api"

vi.mock("@/services/api", () => ({
  api: {
    post: vi.fn(),
    get: vi.fn(),
  },
}))

describe("AuthClient", () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe("login", () => {
    it("正常ログインAPIの呼び出し確認", async () => {
      const mockResponse = { data: { token: "dummy-token" } }
      vi.mocked(api.post).mockResolvedValueOnce(mockResponse)

      const result = await AuthClient.login("testuser", "password123")

      expect(api.post).toHaveBeenCalledTimes(1)
      expect(api.post).toHaveBeenCalledWith("/auth/login", {
        username: "testuser",
        password: "password123",
      })
      expect(result).toEqual(mockResponse)
    })
  })

  describe("me", () => {
    it("ログインユーザー情報取得APIの呼び出し確認", async () => {
      const mockUser = { data: { id: 1, username: "testuser" } }
      vi.mocked(api.get).mockResolvedValueOnce(mockUser)

      const result = await AuthClient.me()

      expect(api.get).toHaveBeenCalledTimes(1)
      expect(api.get).toHaveBeenCalledWith("/auth/me")
      expect(result).toEqual(mockUser)
    })
  })

  describe("logout", () => {
    it("ログアウトAPIの呼び出し確認", async () => {
      const mockResponse = { data: { message: "OK" } }
      vi.mocked(api.post).mockResolvedValueOnce(mockResponse)

      const result = await AuthClient.logout()

      expect(api.post).toHaveBeenCalledTimes(1)
      expect(api.post).toHaveBeenCalledWith("/auth/logout")
      expect(result).toEqual(mockResponse)
    })
  })

  describe("ログイン失敗 (401)", () => {
    it("不正な認証情報によるAPIエラーのハンドリング確認", async () => {
      const error = new Error("Unauthorized")
      vi.mocked(api.post).mockRejectedValueOnce(error)

      await expect(AuthClient.login("wronguser", "wrongpassword")).rejects.toThrow("Unauthorized")
      expect(api.post).toHaveBeenCalledWith("/auth/login", {
        username: "wronguser",
        password: "wrongpassword",
      })
    })
  })
})