import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useChannelStore } from '@/stores/useChannelStore'
import { ChannelClient } from '@/clients/ChannelClient'

// ChannelClientのモック化
vi.mock('@/clients/ChannelClient', () => ({
  ChannelClient: {
    getChannels: vi.fn(),
    createChannel: vi.fn(),
    deleteChannel: vi.fn(),
    getChannelById: vi.fn(),
  },
}))

describe('ChannelStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('localstorage: 選択チャンネルのローカルストレージ保存・復元・クリアが正しく行われること', () => {
    const channelStore = useChannelStore()
    const mockChannel = { id: 1, name: 'general', status: 0, createdAt: '2026-01-01T00:00:00Z' }

    // 1. セットとローカルストレージ保存の確認
    channelStore.setCurrentChannel(mockChannel)
    expect(channelStore.currentChannel).toEqual(mockChannel)
    expect(channelStore.isSelected).toBe(true)
    expect(localStorage.getItem('currentChannel')).toBe(JSON.stringify(mockChannel))

    // 2. 状態をクリアして復元の確認
    channelStore.currentChannel = null
    expect(channelStore.currentChannel).toBeNull()

    channelStore.restoreCurrentChannel()
    expect(channelStore.currentChannel).toEqual(mockChannel)

    // 3. クリアの確認
    channelStore.clearCurrentChannel()
    expect(channelStore.currentChannel).toBeNull()
    expect(localStorage.getItem('currentChannel')).toBeNull()
  })

  it('fetchChannels: チャンネル一覧を正常に取得してストアに格納できること', async () => {
    const mockChannels = [
      { id: 1, name: 'general', status: 0, createdAt: '2026-01-01T00:00:00Z' },
      { id: 2, name: 'random', status: 0, createdAt: '2026-01-02T00:00:00Z' },
    ]
    ;(ChannelClient.getChannels as ReturnType<typeof vi.fn>).mockResolvedValueOnce(mockChannels)

    const channelStore = useChannelStore()
    await channelStore.fetchChannels()

    expect(ChannelClient.getChannels).toHaveBeenCalledTimes(1)
    expect(channelStore.channels).toEqual(mockChannels)
    expect(channelStore.hasChannels).toBe(true)
    expect(channelStore.loading).toBe(false)
  })

  it('createChannel: 新規チャンネルを作成し、一覧を再取得できること', async () => {
    const newChannel = { id: 3, name: 'new-channel', status: 0, createdAt: '2026-01-03T00:00:00Z' }
    ;(ChannelClient.createChannel as ReturnType<typeof vi.fn>).mockResolvedValueOnce(newChannel)
    ;(ChannelClient.getChannels as ReturnType<typeof vi.fn>).mockResolvedValueOnce([newChannel])

    const channelStore = useChannelStore()
    const result = await channelStore.createChannel('new-channel')

    expect(ChannelClient.createChannel).toHaveBeenCalledWith('new-channel')
    expect(ChannelClient.getChannels).toHaveBeenCalledTimes(1)
    expect(result).toEqual(newChannel)
  })

  it('deleteChannel: チャンネルを削除し、選択中だった場合はクリアされること', async () => {
    const channelStore = useChannelStore()
    const targetChannel = { id: 1, name: 'delete-me', status: 0, createdAt: '2026-01-01T00:00:00Z' }
    channelStore.channels = [targetChannel]
    channelStore.setCurrentChannel(targetChannel)

    ;(ChannelClient.deleteChannel as ReturnType<typeof vi.fn>).mockResolvedValueOnce(undefined)
    ;(ChannelClient.getChannels as ReturnType<typeof vi.fn>).mockResolvedValueOnce([])

    await channelStore.deleteChannel(1)

    expect(ChannelClient.deleteChannel).toHaveBeenCalledWith(1)
    expect(channelStore.currentChannel).toBeNull()
    expect(localStorage.getItem('currentChannel')).toBeNull()
  })

  it('applyWebSocketEvent: channel_created および channel_deleted イベントを正しく処理できること', () => {
    const channelStore = useChannelStore()
    channelStore.channels = [{ id: 1, name: 'general', status: 0, createdAt: '2026-01-01T00:00:00Z' }]

    // 1. channel_created イベントのテスト
    channelStore.applyWebSocketEvent({
      event: 'channel_created',
      channel: { id: 2, name: 'random', status: 0, createdAt: '2026-01-02T00:00:00Z' },
    })
    expect(channelStore.channels.length).toBe(2)
    expect(channelStore.channels[1].name).toBe('random')

    // 2. channel_deleted イベントのテスト
    channelStore.applyWebSocketEvent({
      event: 'channel_deleted',
      channelId: 2,
    })
    expect(channelStore.channels.length).toBe(1)
    expect(channelStore.channels.some(c => c.id === 2)).toBe(false)
  })

  it('deleteChannel (異常系): チャンネル削除APIが失敗した場合、ストアの状態が維持され例外がスローされること', async () => {
    const channelStore = useChannelStore()
    const targetChannel = { id: 1, name: 'delete-me', status: 0, createdAt: '2026-01-01T00:00:00Z' }
    channelStore.channels = [targetChannel]
    channelStore.setCurrentChannel(targetChannel)

    const mockError = new Error('Network Error')
    ;(ChannelClient.deleteChannel as ReturnType<typeof vi.fn>).mockRejectedValueOnce(mockError)

    // 例外がスローされることの検証
    await expect(channelStore.deleteChannel(1)).rejects.toThrow('Network Error')

    // 削除に失敗したため、channels や選択中の currentChannel が保持されていること
    expect(channelStore.channels.length).toBe(1)
    expect(channelStore.currentChannel).toEqual(targetChannel)
    expect(localStorage.getItem('currentChannel')).not.toBeNull()
  })
  
})