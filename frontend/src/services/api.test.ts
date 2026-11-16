import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { api } from '@/services/api'
import { useToastStore } from '@/stores/useToastStore'

// Axios 自体をモック化
vi.mock('axios', async (importOriginal) => {
  const actual: any = await importOriginal()
  return {
    ...actual,
    default: {
      ...actual.default,
      create: vi.fn(() => actual.default.create()),
    },
  }
})

describe('Axios Interceptors Error Handling', () => {
  let toastStore: ReturnType<typeof useToastStore>
  let dispatchEventSpy: any

  beforeEach(() => {
    setActivePinia(createPinia())
    toastStore = useToastStore()
    
    // toastStore の showToast をスパイ（モック化）
    vi.spyOn(toastStore, 'showToast')

    // window.dispatchEvent をスパイ
    dispatchEventSpy = vi.spyOn(window, 'dispatchEvent')

    vi.clearAllMocks()
  })

  it('500エラー時、サーバーエラーのトーストが表示されること', async () => {
    const errorHandler = (api.interceptors.response as any).handlers[0].rejected

    const mockError = {
      response: {
        status: 500,
        data: {},
      },
      config: { url: '/api/some-endpoint' },
    }

    await expect(errorHandler(mockError)).rejects.toEqual(mockError)

    expect(toastStore.showToast).toHaveBeenCalledWith(
      'サーバーエラーが発生しました。しばらく経ってから再度お試しください。',
      'error'
    )
  })

  it('403エラー時、権限エラーのトーストが表示されること', async () => {
    const errorHandler = (api.interceptors.response as any).handlers[0].rejected

    const mockError = {
      response: {
        status: 403,
        data: {},
      },
      config: { url: '/api/admin' },
    }

    await expect(errorHandler(mockError)).rejects.toEqual(mockError)

    expect(toastStore.showToast).toHaveBeenCalledWith(
      'この操作を行う権限がありません。',
      'error'
    )
  })

  it('401エラー（通常画面）時、セッション切れイベントが発火しトーストが出ないこと（App.vue側で処理するため）', async () => {
    const errorHandler = (api.interceptors.response as any).handlers[0].rejected

    const mockError = {
      response: {
        status: 401,
        data: {},
      },
      config: { url: '/api/user/profile' },
    }

    await expect(errorHandler(mockError)).rejects.toEqual(mockError)

    expect(dispatchEventSpy).toHaveBeenCalledWith(expect.any(CustomEvent))
    expect(toastStore.showToast).not.toHaveBeenCalled()
  })

  it('401エラー（ログインAPI）時、グローバルなセッション切れイベントもトーストも発火しないこと', async () => {
    const errorHandler = (api.interceptors.response as any).handlers[0].rejected

    const mockError = {
      response: {
        status: 401,
        data: { error: 'パスワードが違います' },
      },
      config: { url: '/auth/login' }, 
    }

    await expect(errorHandler(mockError)).rejects.toEqual(mockError)

    expect(dispatchEventSpy).not.toHaveBeenCalled()
    expect(toastStore.showToast).not.toHaveBeenCalled()
  })

  it('409エラー（ユーザー登録・更新API含む）時、重複エラーのトーストが表示されること', async () => {
    const errorHandler = (api.interceptors.response as any).handlers[0].rejected

    const mockError = {
      response: {
        status: 409,
        data: {},
      },
      config: { url: '/users' }, // 登録・更新系のURLでもトーストが出ることを確認
    }

    await expect(errorHandler(mockError)).rejects.toEqual(mockError)

    expect(toastStore.showToast).toHaveBeenCalledWith(
      'すでに使用されているデータが存在します。',
      'error'
    )
  })

  it('422エラー（通常画面）時、バリデーションエラーのトーストが表示されること', async () => {
    const errorHandler = (api.interceptors.response as any).handlers[0].rejected

    const mockError = {
      response: {
        status: 422,
        data: { message: '入力形式が正しくありません。' },
      },
      config: { url: '/api/posts' },
    }

    await expect(errorHandler(mockError)).rejects.toEqual(mockError)

    expect(toastStore.showToast).toHaveBeenCalledWith(
      '入力形式が正しくありません。',
      'error'
    )
  })

  it('422エラー（フォーム送信時）時、フォーム側で処理するためトーストが表示されないこと', async () => {
    const errorHandler = (api.interceptors.response as any).handlers[0].rejected

    const mockError = {
      response: {
        status: 422,
        data: { message: 'パスワードが短すぎます。' },
      },
      config: { url: '/users' },
    }

    await expect(errorHandler(mockError)).rejects.toEqual(mockError)

    expect(toastStore.showToast).not.toHaveBeenCalled()
  })
})