import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import LoginView from '@/views/LoginView.vue'
import LoginForm from '@/components/LoginForm.vue'
import { useAuthStore } from '@/stores/useAuthStore'

// useAuthStore のモック
vi.mock('@/stores/useAuthStore', () => ({
  useAuthStore: vi.fn(),
}))

// vue-router のモック
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}))

describe('LoginView.vue', () => {
  let mockAuthLogin: ReturnType<typeof vi.fn>
  let mockLoginError: any

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    // authStore の各プロパティ・メソッドのモックを準備
    mockAuthLogin = vi.fn()
    mockLoginError = ref<string | null>(null)

    ;(useAuthStore as any).mockReturnValue({
      login: mockAuthLogin,
      get loginError() {
        return mockLoginError.value
      },
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // 1. 正常系: ログイン成功時に /home へ遷移すること
  it('認証成功時は authStore.login が呼ばれ、 /home へ遷移すること', async () => {
    mockAuthLogin.mockResolvedValueOnce(undefined)
    mockLoginError.value = null

    const wrapper = mount(LoginView, {
      global: {
        plugins: [createPinia()],
        stubs: {
          LoginForm: true,
        },
      },
    })

    const loginForm = wrapper.findComponent(LoginForm)
    await loginForm.vm.$emit('login', 'testuser', 'password123')

    await flushPromises()

    expect(mockAuthLogin).toHaveBeenCalledWith('testuser', 'password123')
    expect(mockPush).toHaveBeenCalledWith('/home')
    expect(wrapper.find('.login__error').exists()).toBe(false)
  })

  // 2. 異常系: ログイン失敗時にストアの loginError を通じてエラーメッセージが画面に表示されること
  it('ストアに loginError が設定された場合、 .login__error を通じてエラーメッセージが画面に表示されること', async () => {
    const errorMessageText = 'ユーザー名またはパスワードが正しくありません。'
    
    // ログイン処理失敗時に、ストア側で loginError がセットされる挙動を再現
    mockAuthLogin.mockImplementationOnce(async () => {
      mockLoginError.value = errorMessageText
      throw new Error('Unauthorized')
    })

    const wrapper = mount(LoginView, {
      global: {
        plugins: [createPinia()],
        stubs: {
          LoginForm: true,
        },
      },
    })

    const loginForm = wrapper.findComponent(LoginForm)
    await loginForm.vm.$emit('login', 'wronguser', 'wrongpass')

    await flushPromises()

    // .login__error 要素が存在し、ストアの loginError の内容が表示されていることを確認
    const errorP = wrapper.find('.login__error')
    expect(errorP.exists()).toBe(true)
    expect(errorP.text()).toBe(errorMessageText)
    expect(mockPush).not.toHaveBeenCalled()
  })
})