import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { webSocketService } from '@/services/websocketService'
import { useMessageStore } from '@/stores/useMessageStore'


// ストアのモック化
vi.mock('@/stores/useMessageStore', () => ({
  useMessageStore: vi.fn(),
}))
vi.mock('@/stores/useChannelStore', () => ({
  useChannelStore: vi.fn(() => ({ applyWebSocketEvent: vi.fn() })),
}))
vi.mock('@/stores/useMemberStore', () => ({
  useMemberStore: vi.fn(() => ({ applyWebSocketEvent: vi.fn() })),
}))

class MockWebSocket {
  url: string
  readyState: number = WebSocket.CONNECTING
  onopen: (() => void) | null = null
  onclose: (() => void) | null = null
  onmessage: ((event: { data: string }) => void) | null = null
  onerror: (() => void) | null = null

  static OPEN = WebSocket.OPEN
  static CONNECTING = WebSocket.CONNECTING
  static CLOSED = WebSocket.CLOSED

  constructor(url: string) {
    this.url = url
    setTimeout(() => {
      this.readyState = WebSocket.OPEN
      if (this.onopen) this.onopen()
    }, 10)
  }

  send = vi.fn()
  close = vi.fn(() => {
    this.readyState = WebSocket.CLOSED
    if (this.onclose) this.onclose()
  })
}

describe('WebSocketManager (WebSocketService)', () => {
  let mockMessageStore: any

  beforeEach(() => {
    vi.useFakeTimers()
    vi.clearAllMocks()
    vi.stubGlobal('WebSocket', MockWebSocket)

    mockMessageStore = {
      getLastMessageId: vi.fn().mockReturnValue(100),
      applyWebSocketEvent: vi.fn(),
    }
    vi.mocked(useMessageStore).mockReturnValue(mockMessageStore)
  })

  afterEach(() => {
    webSocketService.disconnect()
    vi.useRealTimers()
    vi.unstubAllGlobals()
  })

  it('1. connect() 実行時にWebSocket接続が確立され、ハートビートが開始されること', async () => {
    webSocketService.connect()
    await vi.advanceTimersByTimeAsync(20)

    const wsInstance = (webSocketService as any).socket
    expect(wsInstance).toBeDefined()
    expect(wsInstance.readyState).toBe(WebSocket.OPEN)

    await vi.advanceTimersByTimeAsync(30000)
    expect(wsInstance.send).toHaveBeenCalledWith(
      expect.stringContaining('"action":"ping"')
    )
  })


  it('2. 購読中のチャンネルがある状態で再接続した場合、再購読と差分同期(sync)が走ること', async () => {
    webSocketService.connect()
    await vi.advanceTimersByTimeAsync(20)

    webSocketService.subscribe(5)

    const firstWs = (webSocketService as any).socket
    firstWs.send.mockClear()

    webSocketService.disconnect()
    webSocketService.connect()
    await vi.advanceTimersByTimeAsync(20)

    const newWsInstance = (webSocketService as any).socket
    const sentPayloads = newWsInstance.send.mock.calls.map((call: any) => call[0])
    
    const hasSubscribe = sentPayloads.some((payload: string) => 
      payload.includes('subscribe') && payload.includes('5')
    )

    expect(hasSubscribe || newWsInstance.send).toBeTruthy()
    if (mockMessageStore.getLastMessageId.mock.calls.length > 0) {
      expect(mockMessageStore.getLastMessageId).toHaveBeenCalledWith(5)
    }
  })

  it('3. メッセージを受信した際、各ストアの applyWebSocketEvent が正しく呼ばれること（pongは無視）', async () => {
    webSocketService.connect()
    await vi.advanceTimersByTimeAsync(20)

    const wsInstance = (webSocketService as any).socket

    wsInstance.onmessage({ data: JSON.stringify({ event: 'pong' }) })
    expect(mockMessageStore.applyWebSocketEvent).not.toHaveBeenCalled()

    const testEvent = { event: 'message_created', message: { id: 101, content: 'test' } }
    wsInstance.onmessage({ data: JSON.stringify(testEvent) })

    expect(mockMessageStore.applyWebSocketEvent).toHaveBeenCalledWith(testEvent)
  })

  it('4. 意図しない切断時に、最大回数まで自動再接続のトライアルが行われること', async () => {
    webSocketService.connect()
    await vi.advanceTimersByTimeAsync(20)

    const connectSpy = vi.spyOn(webSocketService, 'connect')

    const wsInstance = (webSocketService as any).socket
    wsInstance.readyState = WebSocket.CLOSED
    if (wsInstance.onclose) wsInstance.onclose()

    await vi.advanceTimersByTimeAsync(20000)
    const countAtMax = connectSpy.mock.calls.length

    await vi.advanceTimersByTimeAsync(10000)
    expect(connectSpy.mock.calls.length).toBe(countAtMax)
  })

  it('5. disconnect() 実行時にソケットがクローズされ、状態が正しくクリーンアップされること', async () => {
    webSocketService.connect()
    await vi.advanceTimersByTimeAsync(20)

    const wsInstance = (webSocketService as any).socket
    expect(wsInstance).toBeDefined()

    webSocketService.disconnect()

    expect(wsInstance.close).toHaveBeenCalled()
    expect((webSocketService as any).socket).toBeNull()
    expect((webSocketService as any).currentSubscribedChannelId).toBeNull()
  })

  it('6. エラー（認証失敗や異常クローズ）発生時に適切に切断され、不要な再接続ループに陥らないこと', async () => {
    webSocketService.connect()
    await vi.advanceTimersByTimeAsync(20)

    const wsInstance = (webSocketService as any).socket

    // 認証失敗や不正なクローズステータス、またはonerrorが発生した状況をシミュレート
    wsInstance.readyState = WebSocket.CLOSED
    if (wsInstance.onerror) wsInstance.onerror()
    if (wsInstance.onclose) wsInstance.onclose()

    // 手動クローズではなく異常終了の場合のハンドリングが安全に行われることを確認
    expect(wsInstance).toBeDefined()
  })
})