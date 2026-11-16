import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/useAuthStore'
import { AuthClient } from '@/clients/AuthClient'

// ChannelClient と同様に名前付きエクスポートとしてモックを定義
vi.mock('@/clients/AuthClient', () => ({
  AuthClient: {
    login: vi.fn(),
    logout: vi.fn(),
    me: vi.fn(),
  },
}))

describe('AuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('login: ログイン処理が成功し、ユーザー情報が state.user に格納されること', async () => {
    const mockUser = { userId: 1, username: 'testuser', role: 1, isActive: true }
    
    ;(AuthClient.login as ReturnType<typeof vi.fn>).mockResolvedValueOnce(undefined)
    ;(AuthClient.me as ReturnType<typeof vi.fn>).mockResolvedValueOnce({ data: mockUser })

    const authStore = useAuthStore()
    await authStore.login('testuser', 'password123')

    expect(AuthClient.login).toHaveBeenCalledWith('testuser', 'password123')
    expect(AuthClient.me).toHaveBeenCalled()
    expect(authStore.user).toEqual(mockUser)
    expect(authStore.loginError).toBeNull() // 成功時はエラーがクリアされていること
  })

  it('login (異常系): ログイン失敗時（401等）、user が null のままで loginError にメッセージが格納されること', async () => {
    const mockError = {
      response: {
        status: 401,
        data: { error: 'パスワードが違います' }
      }
    }
    ;(AuthClient.login as ReturnType<typeof vi.fn>).mockRejectedValueOnce(mockError)

    const authStore = useAuthStore()
    
    // 例外が再スローされることを確認
    await expect(authStore.login('testuser', 'wrong-password')).rejects.toEqual(mockError)

    expect(authStore.user).toBeNull()
    expect(authStore.loginError).toBe('ユーザー名またはパスワードが正しくありません。')
  })

  it('logout: ログアウト処理が成功し、state.user が null にリセットされること', async () => {
    const authStore = useAuthStore()
    authStore.user = { userId: 1, username: 'testuser', role: 1, isActive: true }

    ;(AuthClient.logout as ReturnType<typeof vi.fn>).mockResolvedValueOnce(undefined)

    await authStore.logout()

    expect(AuthClient.logout).toHaveBeenCalled()
    expect(authStore.user).toBeNull()
  })

  it('restoreSession: セッション復元が成功し、ユーザー情報が state.user に設定されること', async () => {
    const mockUser = { userId: 2, username: 'restoreuser', role: 1, isActive: true }
    ;(AuthClient.me as ReturnType<typeof vi.fn>).mockResolvedValueOnce({ data: mockUser })

    const authStore = useAuthStore()
    await authStore.restoreSession()

    expect(AuthClient.me).toHaveBeenCalled()
    expect(authStore.user).toEqual(mockUser)
  })

  it('restoreSession (異常系): セッション復元に失敗した際、state.user が null になること', async () => {
    ;(AuthClient.me as ReturnType<typeof vi.fn>).mockRejectedValueOnce(new Error('Unauthorized'))

    const authStore = useAuthStore()
    await authStore.restoreSession()

    expect(authStore.user).toBeNull()
  })
})