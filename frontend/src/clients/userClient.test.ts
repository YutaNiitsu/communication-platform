import { describe, it, expect, vi, beforeEach } from 'vitest'
import { UserClient } from '@/clients/UserClient'
import { api } from '@/services/api'

// apiモジュールをモック化
vi.mock('@/services/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('UserClient', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getUserById: 指定したユーザーIDの詳細データを取得できること', async () => {
    const mockUser = { id: 1, username: 'testuser', role: 1, isActive: true }
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockUser })

    const result = await UserClient.getUserById(1)

    expect(api.get).toHaveBeenCalledWith('/users/1')
    expect(result).toEqual({ data: mockUser })
  })

  it('getAllUsers: 全ユーザーの一覧データを取得できること', async () => {
    const mockUsers = [{ id: 1, username: 'user1' }, { id: 2, username: 'user2' }]
    vi.mocked(api.get).mockResolvedValueOnce({ data: mockUsers })

    const result = await UserClient.getAllUsers()

    expect(api.get).toHaveBeenCalledWith('/users')
    expect(result).toEqual({ data: mockUsers })
  })

  it('createUser: キャメルケースからスネークケースに変換して新規登録APIを呼び出せること', async () => {
    const mockResponse = { id: 3, username: 'newuser', role: 1, is_active: true }
    vi.mocked(api.post).mockResolvedValueOnce({ data: mockResponse })

    const payload = {
      username: 'newuser',
      password: 'password123',
      role: 1,
      isActive: true,
    }

    const result = await UserClient.createUser(payload)

    expect(api.post).toHaveBeenCalledWith('/users', {
      username: 'newuser',
      password: 'password123',
      role: 1,
      is_active: true,
    })
    expect(result).toEqual({ data: mockResponse })
  })

  it('updateUser: 変更のあるプロパティのみを抽出して更新APIを呼び出せること', async () => {
    const mockResponse = { id: 1, role: 2 }
    vi.mocked(api.put).mockResolvedValueOnce({ data: mockResponse })

    const payload = {
      role: 2, // roleのみ変更
    }

    const result = await UserClient.updateUser(1, payload)

    expect(api.put).toHaveBeenCalledWith('/users/1', {
      role: 2,
    })
    expect(result).toEqual({ data: mockResponse })
  })

  it('deleteUser: 指定したユーザーを削除するAPIを呼び出せること', async () => {
    vi.mocked(api.delete).mockResolvedValueOnce({ data: { success: true } })

    const result = await UserClient.deleteUser(1)

    expect(api.delete).toHaveBeenCalledWith('/users/1')
    expect(result).toEqual({ data: { success: true } })
  })

  it('createUser (異常系/409重複エラー): 既存ユーザー名等の重複登録時にエラーが捕捉できること', async () => {
    const errorResponse = {
      response: {
        status: 409,
        data: { message: 'User already exists' },
      },
    }
    vi.mocked(api.post).mockRejectedValueOnce(errorResponse)

    const payload = {
      username: 'existinguser',
      role: 1,
      isActive: true,
    }

    await expect(UserClient.createUser(payload)).rejects.toEqual(errorResponse)
    expect(api.post).toHaveBeenCalledTimes(1)
  })
})