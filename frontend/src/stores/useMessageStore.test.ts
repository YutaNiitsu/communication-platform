import { beforeEach, describe, expect, it, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMessageStore } from '@/stores/useMessageStore'
import { MessageClient } from '@/clients/MessageClient'

// 💡 1. MessageClient の getMessages メソッドをモック化
vi.mock('@/clients/MessageClient', () => ({
  MessageClient: {
    getMessages: vi.fn()
  }
}))

describe('MessageStore - 単体テスト', () => {
  beforeEach(() => {
    // 各テストの前に Pinia を初期化
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  // ==========================================
  // 🔹 fetchMessages アクションのテスト
  // ==========================================
  describe('fetchMessages', () => {
    it('最新のメッセージを指定件数（デフォルト20件）正しく取得し、型変換して state に格納できること', async () => {
      const store = useMessageStore()
      const mockChannelId = 10

      // 擬似的なサーバーレスポンス（スネークケース混在）
      const mockApiResponse = {
        messages: [
          {
            message: {
              id: '101', // 文字列で返ってくるケースを想定
              channel_id: '10',
              user_id: 2,
              content: '最新メッセージ1',
              status: 0,
              created_at: '2026-10-05T12:00:00Z'
            },
            reactions: []
          },
          {
            message: {
              id: 102,
              channelId: 10,
              userId: 3,
              content: '最新メッセージ2',
              status: 0,
              createdAt: '2026-10-05T12:01:00Z'
            },
            reactions: []
          }
        ],
        has_more: true
      }

      // モックの戻り値を設定
      vi.mocked(MessageClient.getMessages).mockResolvedValue(mockApiResponse)

      // アクションの実行（引数に range=20 を指定）
      await store.fetchMessages(mockChannelId, 20)

      // 検証1: APIクライアントが正しく、引数（channelId, messageId=undefined, range=20）で呼ばれたか
      expect(MessageClient.getMessages).toHaveBeenCalledWith(mockChannelId, undefined, 20)

      // 検証2: ストアの messages 配列にデータが正しくマッピングされて格納されているか
      expect(store.messages).toHaveLength(2)
      
      // 文字列で届いた ID 関連が確実に数値型（Number）にキャストされていることを検証
      expect(store.messages[0].id).toBe(101)
      expect(store.messages[0].channelId).toBe(10)
      expect(store.messages[0].userId).toBe(2)
      expect(store.messages[0].content).toBe('最新メッセージ1')
      
      expect(store.messages[1].id).toBe(102)
    })
  })

  // ==========================================
  // 🔹 fetchPreviousMessages アクションのテスト
  // ==========================================
  describe('fetchPreviousMessages', () => {
    it('過去ログを取得した際、既存のメッセージを壊さず「末尾（後ろ側）」にマージし、サーバーの has_more を正しく返すこと', async () => {
      const store = useMessageStore()
      const mockChannelId = 10
      const mockOldestMessageId = 101

      // あらかじめストアに「最新のメッセージ」が1件存在する状態を作る
      store.messages = [
        {
          id: 102,
          channelId: 10,
          userId: 3,
          replyToMessageId: null,
          content: '既存の最新メッセージ',
          mentionedUserIds: [],
          requestId: 'req-xyz',
          status: 0,
          createdAt: '2026-10-05T12:01:00Z',
          updatedAt: null,
          reactions: []
        }
      ]

      // サーバーから返ってくる「さらに古い過去メッセージ」のデータ
      const mockApiResponse = {
        messages: [
          {
            message: {
              id: 100,
              channel_id: 10,
              user_id: 2,
              content: '過去ログメッセージ',
              status: 0,
              created_at: '2026-10-05T11:00:00Z'
            },
            reactions: []
          }
        ],
        has_more: true // さらに過去ログが存在するというフラグ
      }

      vi.mocked(MessageClient.getMessages).mockResolvedValue(mockApiResponse)

      // アクションの実行（最も古いメッセージIDと取得件数を渡す）
      const resultHasMore = await store.fetchPreviousMessages(mockChannelId, mockOldestMessageId, 20)

      // 検証1: APIが正しい引数（channelId, oldestMessageId, range=20）で呼ばれたか
      expect(MessageClient.getMessages).toHaveBeenCalledWith(mockChannelId, mockOldestMessageId, 20)

      // 検証2: 戻り値としてサーバーの has_more フラグ（true）が返ってきているか
      expect(resultHasMore).toBe(true)

      // 検証3: 配列の結合順序の検証。既存データが「先頭」、新しい過去ログが「末尾」に並んでいること
      // （※画面側の computed で最後に全体を .reverse() するため、ストア内ではこの順序が正解）
      expect(store.messages).toHaveLength(2)
      expect(store.messages[0].id).toBe(102) // 先頭は既存のメッセージ
      expect(store.messages[1].id).toBe(100) // 末尾に追加された過去のメッセージ
    })

    it('過去ログが0件、または has_more が false の時、適切に false を返すこと', async () => {
      const store = useMessageStore()
      
      // 過去ログがもう存在しない場合のサーバーレスポンス
      const mockApiResponse = {
        messages: [],
        has_more: false
      }

      vi.mocked(MessageClient.getMessages).mockResolvedValue(mockApiResponse)

      const resultHasMore = await store.fetchPreviousMessages(10, 100, 20)

      // 検証: 戻り値が false であること
      expect(resultHasMore).toBe(false)
    })

    it('過去ログ取得時、すでにストアに存在するメッセージとIDが重複している場合は除外してマージすること（一意性の担保）', async () => {
      const store = useMessageStore()

      // すでにID: 100 のメッセージがストアにいる状態
      store.messages = [
        {
          id: 100,
          channelId: 10,
          userId: 2,
          replyToMessageId: null,
          content: '既存のメッセージ',
          mentionedUserIds: [],
          requestId: 'req-abc',
          status: 0,
          createdAt: '2026-10-05T11:00:00Z',
          updatedAt: null,
          reactions: []
        }
      ]

      // サーバーから重複した ID: 100 が返ってきたケースを想定
      const mockApiResponse = {
        messages: [
          {
            message: {
              id: 100,
              channel_id: 10,
              user_id: 2,
              content: '重複メッセージ（中身が古いなど）',
              status: 0,
              created_at: '2026-10-05T11:00:00Z'
            },
            reactions: []
          }
        ],
        has_more: false
      }

      vi.mocked(MessageClient.getMessages).mockResolvedValue(mockApiResponse)

      await store.fetchPreviousMessages(10, 100, 20)

      // 検証: 重複が排除され、ストア内のメッセージ数が1件のままであること
      expect(store.messages).toHaveLength(1)
      expect(store.messages[0].content).toBe('既存のメッセージ') // 既存データが守られていること
    })
  })
})
