// src/clients/ChannelClient.test.ts
import { describe, it, expect, vi, beforeEach } from "vitest"
import { ChannelClient } from "@/clients/ChannelClient"
import { api } from "@/services/api"

vi.mock("@/services/api", () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
  },
}))

describe("ChannelClient", () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it("チャンネル一覧取得APIの呼び出し確認", async () => {
    const mockData = [{ id: 1, name: "general" }]
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockData })

    const result = await ChannelClient.getChannels()

    expect(api.get).toHaveBeenCalledTimes(1)
    expect(api.get).toHaveBeenCalledWith("/channels")
    expect(result).toEqual(mockData)
  })

  it("チャンネル詳細取得APIの呼び出し確認", async () => {
    const mockData = { id: 1, name: "general" }
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockData })

    const result = await ChannelClient.getChannelById(1)

    expect(api.get).toHaveBeenCalledTimes(1)
    expect(api.get).toHaveBeenCalledWith("/channels/1")
    expect(result).toEqual(mockData)
  })

  it("チャンネル作成APIの呼び出し確認", async () => {
    const mockData = { id: 2, name: "新チャンネル" }
    vi.mocked(api.post).mockResolvedValueOnce({ data: mockData })

    const result = await ChannelClient.createChannel("新チャンネル")

    expect(api.post).toHaveBeenCalledTimes(1)
    expect(api.post).toHaveBeenCalledWith("/channels", { name: "新チャンネル" })
    expect(result).toEqual(mockData)
  })

  it("チャンネル削除APIの呼び出し確認（管理者）", async () => {
    vi.mocked(api.delete).mockResolvedValueOnce({ data: {} })

    await ChannelClient.deleteChannel(1)

    expect(api.delete).toHaveBeenCalledTimes(1)
    expect(api.delete).toHaveBeenCalledWith("/channels/1")
  })

  it("参加者一覧取得APIの呼び出し確認", async () => {
    const mockData = [{ id: 10, username: "testuser" }]
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockData })

    const result = await ChannelClient.getMembers(1)

    expect(api.get).toHaveBeenCalledTimes(1)
    expect(api.get).toHaveBeenCalledWith("/channels/1/members")
    expect(result).toEqual(mockData)
  })

  it("チャンネル参加APIの呼び出し確認", async () => {
    vi.mocked(api.post).mockResolvedValueOnce({ data: {} })

    await ChannelClient.joinChannel(1, 10)

    expect(api.post).toHaveBeenCalledTimes(1)
    expect(api.post).toHaveBeenCalledWith("/channels/1/join", { user_id: 10 })
  })

  it("チャンネル退出APIの呼び出し確認", async () => {
    vi.mocked(api.post).mockResolvedValueOnce({ data: {} })

    await ChannelClient.leaveChannel(1, 10)

    expect(api.post).toHaveBeenCalledTimes(1)
    expect(api.post).toHaveBeenCalledWith("/channels/1/leave", { user_id: 10 })
  })

  it("サーバーエラー時の例外スロー確認", async () => {
    const error = new Error("Internal Server Error")
    vi.mocked(api.get).mockRejectedValueOnce(error)

    await expect(ChannelClient.getChannels()).rejects.toThrow("Internal Server Error")
  })
})