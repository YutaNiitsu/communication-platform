import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

export function createWsClient() {
  const client = new Client({
    reconnectDelay: 5000,
  })

  // 接続先はまだ設定しない（バックエンド未完成のため）
  client.webSocketFactory = () => {
    return new SockJS('http://localhost:8080/ws') as any
  }

  client.onConnect = () => {
    console.log('WebSocket connected')
  }

  client.onStompError = (frame) => {
    console.error('STOMP Error:', frame)
  }

  return client
}
