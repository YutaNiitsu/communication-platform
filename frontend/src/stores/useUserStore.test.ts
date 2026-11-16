import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/useUserStore'
import { UserClient } from '@/clients/UserClient'

vi.mock('@/clients/UserClient', () => ({
  UserClient: {
    getAllUsers: vi.fn(),
    getUserById: vi.fn(),
    createUser: vi.fn(),
    updateUser: vi.fn(),
    deleteUser: vi.fn(),
  },
}))

describe('useUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  // 1. fetchAllUsers (正常系)
  it('fetchAllUsers: APIから取得したユーザー一覧が適切にマッピングされ state に保存されること', async () => {
    const rawUsers = [
      { userId: 1, username: 'Alice', role: 1, isActive: true },
      { userId: 2, username: 'Bob', role: 0 }, // isActive 未指定（デフォルト true になるべき）
    ]
    ;(UserClient.getAllUsers as any).mockResolvedValueOnce({ data: rawUsers })

    const store = useUserStore()
    const result = await store.fetchAllUsers()

    expect(UserClient.getAllUsers).toHaveBeenCalledTimes(1)
    expect(store.users).toEqual([
      { userId: 1, username: 'Alice', role: 1, isActive: true },
      { userId: 2, username: 'Bob', role: 0, isActive: true },
    ])
    expect(result).toEqual(store.users)
  })

  // 2. fetchAllUsers (異常系)
  it('fetchAllUsers: API失敗時にエラーをキャッチし、警告を出力して既存の users を保持すること', async () => {
    const store = useUserStore()
    store.users = [{ userId: 99, username: 'Existing', role: 0, isActive: true }]

    const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
    ;(UserClient.getAllUsers as any).mockRejectedValueOnce(new Error('Network Error'))

    const result = await store.fetchAllUsers()

    expect(consoleWarnSpy).toHaveBeenCalled()
    expect(store.users.length).toBe(1)
    expect(result).toEqual(store.users)
    consoleWarnSpy.mockRestore()
  })

  // 3. fetchUserById (キャッシュヒット)
  it('fetchUserById: ストア内にユーザーが存在する場合、APIを呼ばずにキャッシュから返すこと', async () => {
    const store = useUserStore()
    store.users = [{ userId: 1, username: 'CachedUser', role: 0, isActive: true }]

    const user = await store.fetchUserById(1)

    expect(UserClient.getUserById).not.toHaveBeenCalled()
    expect(user).toEqual({ userId: 1, username: 'CachedUser', role: 0, isActive: true })
  })

  // 4. fetchUserById (API取得 & キャッシュ追加)
  it('fetchUserById: ストアにない場合、APIから取得してストアに追加し返すこと', async () => {
    ;(UserClient.getUserById as any).mockResolvedValueOnce({
      data: { userId: 5, username: 'FetchedUser', role: 1, isActive: true }
    })

    const store = useUserStore()
    const user = await store.fetchUserById(5)

    expect(UserClient.getUserById).toHaveBeenCalledWith(5)
    expect(store.users).toContainEqual(user)
    expect(user).toEqual({ userId: 5, username: 'FetchedUser', role: 1, isActive: true })
  })

  // 5. fetchUserById (API失敗時フォールバック)
  it('fetchUserById: API取得に失敗した場合、デフォルトのユーザーオブジェクトを返却すること', async () => {
    ;(UserClient.getUserById as any).mockRejectedValueOnce(new Error('Not Found'))
    const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})

    const store = useUserStore()
    const user = await store.fetchUserById(999)

    expect(user).toEqual({
      userId: 999,
      username: 'User_999',
      role: 0,
      isActive: true,
    })
    consoleWarnSpy.mockRestore()
  })

  // 6. 各種 CRUD アクションの確認 (createUser, updateUser, deleteUser)
  it('createUser, updateUser, deleteUser: それぞれ対応する Client メソッドが正しく呼ばれること', async () => {
    const store = useUserStore()

    const createPayload = { username: 'NewUser', role: 0, isActive: true }
    await store.createUser(createPayload)
    expect(UserClient.createUser).toHaveBeenCalledWith(createPayload)

    const updatePayload = { username: 'UpdatedName' }
    await store.updateUser(1, updatePayload)
    expect(UserClient.updateUser).toHaveBeenCalledWith(1, updatePayload)

    await store.deleteUser(1)
    expect(UserClient.deleteUser).toHaveBeenCalledWith(1)
  })
})