import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMemberStore } from '@/stores/useMemberStore'
import { ChannelClient } from '@/clients/ChannelClient'
import { useChannelStore } from '@/stores/useChannelStore'

vi.mock('@/clients/ChannelClient', () => ({
  ChannelClient: {
    getMembers: vi.fn(),
    joinChannel: vi.fn(),
    leaveChannel: vi.fn(),
  },
}))

describe('MemberStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  // 1. fetchMembers
  it('fetchMembers: 参加者一覧取得APIの呼び出しとストア状態（members, loading）が正しく更新されること', async () => {
    const mockMembers = [
      { userId: 1, username: 'user1', role: 0, isActive: true, joinedAt: '2026-01-01T00:00:00Z' }
    ]
    ;(ChannelClient.getMembers as ReturnType<typeof vi.fn>).mockResolvedValueOnce(mockMembers)

    const memberStore = useMemberStore()
    const promise = memberStore.fetchMembers(10)

    expect(memberStore.loading).toBe(true)
    await promise

    expect(ChannelClient.getMembers).toHaveBeenCalledWith(10)
    expect(memberStore.members).toEqual(mockMembers)
    expect(memberStore.loading).toBe(false)
  })

  // 2. joinChannel / leaveChannel
  it('joinChannel / leaveChannel: チャンネル参加・退出APIが呼び出され、一覧が自動再取得されること', async () => {
    const memberStore = useMemberStore()

    // joinChannel のテスト
    ;(ChannelClient.joinChannel as ReturnType<typeof vi.fn>).mockResolvedValueOnce(undefined)
    ;(ChannelClient.getMembers as ReturnType<typeof vi.fn>).mockResolvedValueOnce([
      { userId: 1, username: 'user1', role: 1, isActive: true, joinedAt: '2026-01-01T00:00:00Z' }
    ])

    await memberStore.joinChannel(10, 1)
    expect(ChannelClient.joinChannel).toHaveBeenCalledWith(10, 1)
    expect(ChannelClient.getMembers).toHaveBeenCalledWith(10)

    // leaveChannel のテスト
    ;(ChannelClient.leaveChannel as ReturnType<typeof vi.fn>).mockResolvedValueOnce(undefined)
    ;(ChannelClient.getMembers as ReturnType<typeof vi.fn>).mockResolvedValueOnce([])

    await memberStore.leaveChannel(10, 1)
    expect(ChannelClient.leaveChannel).toHaveBeenCalledWith(10, 1)
    expect(ChannelClient.getMembers).toHaveBeenCalledWith(10)
    expect(memberStore.members).toEqual([])
  })

  // 3. addMember
  it('addMember: 他ユーザー招待時に joinChannel が実行された後、fetchMembers が自動実行されること', async () => {
    const mockMembers = [
      { userId: 2, username: 'user2', role: 1, isActive: true, joinedAt: '2026-01-01T00:00:00Z' }
    ]
    ;(ChannelClient.joinChannel as ReturnType<typeof vi.fn>).mockResolvedValueOnce(undefined)
    ;(ChannelClient.getMembers as ReturnType<typeof vi.fn>).mockResolvedValueOnce(mockMembers)

    const memberStore = useMemberStore()
    await memberStore.addMember(10, 2)

    expect(ChannelClient.joinChannel).toHaveBeenCalledWith(10, 2)
    expect(ChannelClient.getMembers).toHaveBeenCalledWith(10)
    expect(memberStore.members).toEqual(mockMembers)
  })

  // 4. member operation error
  it('member operation error: メンバー参加・追加API失敗時にエラーがスローされ、loadingがfalseに戻ること', async () => {
    ;(ChannelClient.joinChannel as ReturnType<typeof vi.fn>).mockRejectedValueOnce(new Error('Join Failed'))

    const memberStore = useMemberStore()

    await expect(memberStore.addMember(10, 2)).rejects.toThrow('Join Failed')
    expect(memberStore.loading).toBe(false)
  })

  it('applyWebSocketEvent: 現在開いているチャンネル宛ての channel_joined / channel_left イベントを受信した際にメンバー一覧が自動更新されること', async () => {
    const channelStore = useChannelStore()
    // 現在開いているチャンネルを ID: 10 に設定
    channelStore.currentChannel = { id: 10, name: 'general' } as any

    const memberStore = useMemberStore()
    const updatedMockMembers = [
      { userId: 1, username: 'user1', role: 1, isActive: true, joinedAt: '2026-01-01T00:00:00Z' },
      { userId: 2, username: 'user2', role: 0, isActive: true, joinedAt: '2026-01-01T00:00:00Z' }
    ]
    ;(ChannelClient.getMembers as ReturnType<typeof vi.fn>).mockResolvedValueOnce(updatedMockMembers)

    // channel_joined イベントをシミュレート
    await memberStore.applyWebSocketEvent({ event: 'channel_joined', channelId: 10 })

    expect(ChannelClient.getMembers).toHaveBeenCalledWith(10)
    expect(memberStore.members).toEqual(updatedMockMembers)
  })
})