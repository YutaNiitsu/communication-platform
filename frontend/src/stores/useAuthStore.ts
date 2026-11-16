import { defineStore } from "pinia"
import { AuthClient } from "../clients/AuthClient"

const authChannel = new BroadcastChannel('auth_channel')

export interface User {
  userId: number
  username: string
  role: number
  isActive: boolean
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    user: null as User | null,
    loginError: null as string | null, // 👈 ログイン失敗時のメッセージ用ステートを追加
  }),

  getters: {
    isAdmin(state) {
      return state.user?.role === 0
    },
    isLoggedIn(state) {
      return state.user !== null
    }
  },

  actions: {
    async login(username: string, password: string) {
      // ログイン試行時に前回のエラーをクリア
      this.loginError = null

      try {
        await AuthClient.login(username, password)
        const me = await AuthClient.me()
        const data = me.data
        this.user = {
          userId: data.userId,
          username: data.username,
          role: data.role,
          isActive: data.isActive ?? true
        }
        authChannel.postMessage({ type: 'LOGIN', userId: this.user.userId })
      } catch (error: any) {
        // 👈 失敗時にエラーメッセージをストアに保持する
        this.user = null
        
        // ステータスコードやバックエンドからのメッセージに応じて分岐
        if (error.response?.status === 401) {
          this.loginError = "ユーザー名またはパスワードが正しくありません。"
        } else {
          this.loginError = error.response?.data?.message || "ログインに失敗しました。しばらく経ってから再度お試しください。"
        }
        
        throw error // 呼び出し元（ログイン画面）でハンドリングしたい場合のために再スロー
      }
    },

    async logout() {
      await AuthClient.logout()
      this.clearUser()
      authChannel.postMessage({ type: 'LOGOUT' })
    },

    clearUser() {
      this.user = null
      this.loginError = null // ログアウト時にもエラーをクリア
    },

    async restoreSession() {
      try {
        const me = await AuthClient.me()
        const data = me.data
        this.user = {
          userId: data.userId,
          username: data.username,
          role: data.role,
          isActive: data.isActive ?? true
        }
      } catch {
        this.clearUser()
        authChannel.postMessage({ type: 'LOGOUT' })
      }
    },
  }
})
