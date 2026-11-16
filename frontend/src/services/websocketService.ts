// src/services/WebSocketService.ts
import { useMessageStore } from "@/stores/useMessageStore" 
import { useChannelStore } from "@/stores/useChannelStore"
import { useMemberStore } from "@/stores/useMemberStore"

class WebSocketManager {
  private socket: WebSocket | null = null
  private reconnectInterval: number = 3000
  private isManuallyClosed: boolean = false
  private pingTimer: number | null = null
  private currentSubscribedChannelId: number | null = null

  // 再接続回数に関する設定
  private reconnectAttempts: number = 0
  private maxReconnectAttempts: number = 5 // 最大リトライ回数

  public connect() {
    if (this.socket && (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)) {
      return
    }

    this.isManuallyClosed = false
    
    const wsUrl = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080/ws/chat'
    this.socket = new WebSocket(wsUrl)

    this.socket.onopen = () => {
      console.log('WebSocket接続確立')
      
      // 接続成功したらリトライ回数をリセット
      this.reconnectAttempts = 0
      
      this.startHeartbeat()

      if (this.currentSubscribedChannelId !== null) {
        // 1. チャンネルの再購読
        this.subscribe(this.currentSubscribedChannelId)

        // 2. 切断中の差分データ同期（バックエンドの SyncService と連携）
        const messageStore = useMessageStore()
        
        // ストアから現在保持している最後のメッセージIDを取得（未取得・0件の場合は 0 を指定）
        const lastMessageId = messageStore.getLastMessageId 
          ? messageStore.getLastMessageId(this.currentSubscribedChannelId) 
          : 0

        this.sync([this.currentSubscribedChannelId], lastMessageId ?? 0)
      }
    }

    this.socket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.event === 'pong') return

        console.log("WebSocket受信データ:", data)

        const messageStore = useMessageStore()
        messageStore.applyWebSocketEvent(data)

        const channelStore = useChannelStore()
        channelStore.applyWebSocketEvent(data)

        const memberStore = useMemberStore()
        memberStore.applyWebSocketEvent(data)
      } catch (e) {
        console.error('WebSocket message parse error:', e)
      }
    }

    this.socket.onclose = () => {
      this.stopHeartbeat()
      
      if (!this.isManuallyClosed) {
        // 上限に達していなければ再接続、達していれば停止
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++
          console.warn(`WebSocket切断。再接続を試みます... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
          setTimeout(() => this.connect(), this.reconnectInterval)
        } else {
          console.error('WebSocketの再接続回数が上限（5回）に達しました。接続を諦めます。')
        }
      }
    }
  }

  private startHeartbeat() {
    this.stopHeartbeat()
    this.pingTimer = window.setInterval(() => {
      this.send({ action: 'ping', timestamp: Date.now() })
    }, 30000)
  }

  private stopHeartbeat() {
    if (this.pingTimer !== null) {
      clearInterval(this.pingTimer)
      this.pingTimer = null
    }
  }

  public send(payload: object) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(payload))
    }
  }

  public subscribe(channelId: number) {
    this.currentSubscribedChannelId = channelId
    this.send({ action: 'subscribe', channel_id: channelId })
  }

  public unsubscribe(channelId: number) {
    if (this.currentSubscribedChannelId === channelId) {
      this.currentSubscribedChannelId = null
    }
    this.send({ action: 'unsubscribe', channel_id: channelId })
  }

  /**
   * バックエンドの SyncService と連携する同期アクション
   */
  public sync(channelIds: number[], lastMessageId: number) {
    this.send({
      action: 'sync',
      channelIds: channelIds,       
      lastMessageId: lastMessageId  
    })
  }

  public disconnect() {
    this.isManuallyClosed = true
    this.reconnectAttempts = 0
    this.stopHeartbeat()
    this.currentSubscribedChannelId = null
    this.socket?.close()
    this.socket = null
  }
}

export const webSocketService = new WebSocketManager()