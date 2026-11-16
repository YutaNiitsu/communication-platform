import axios from 'axios'
import camelcaseKeys from 'camelcase-keys'
import { useToastStore } from '@/stores/useToastStore'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/',
  withCredentials: true,
  timeout: 10000,
})

api.interceptors.response.use(
  (response) => {
    if (response.data) {
      response.data = camelcaseKeys(response.data, { deep: true })
    }
    return response
  },
  (error) => {
    const toastStore = useToastStore()

    if (error.response) {
      const status = error.response.status
      // 💡 どのエンドポイントに対するリクエストだったかを取得
      const requestUrl = error.config?.url || ''

      // 💡 フォーム画面側（ログイン・ユーザー登録など）で独自にエラー表示（赤字など）を
      // 行うため、グローバルのトーストやセッション切れイベントの対象外とする判定
      const isLoginRequest = requestUrl.includes('/auth/login')
      const isRegisterRequest = requestUrl.includes('/users') // ※バックエンドの登録API仕様に合わせて調整してください
      const isFormSubmitRequest = isLoginRequest || isRegisterRequest

      switch (status) {
        case 401:
          if (!isLoginRequest) {
            // 通常の画面でのセッション切れのみトーストとイベントを発火
            console.warn('認証エラー (401): セッションが切れました。')
            window.dispatchEvent(new CustomEvent('auth:unauthorized'))
          }
          break

        case 403:
          toastStore.showToast('この操作を行う権限がありません。', 'error')
          break

        case 409:
            toastStore.showToast('すでに使用されているデータが存在します。', 'error')
          break

        case 422:
          // 422 Unprocessable Entity (バリデーションエラー等)
          if (!isFormSubmitRequest) {
            const validationMsg = error.response.data?.error || error.response.data?.message || '入力内容に誤りがあります。'
            toastStore.showToast(validationMsg, 'error')
          }
          break

        case 500:
          toastStore.showToast('サーバーエラーが発生しました。しばらく経ってから再度お試しください。', 'error')
          break

        default:
          // バックエンドから返ってきたカスタムメッセージがある場合はそれを優先
          const customMsg = error.response.data?.error || error.response.data?.message || '予期せぬエラーが発生しました。'
          
          // フォーム送信系（ログイン・登録等）以外の時だけグローバルトーストを表示
          if (!isFormSubmitRequest) {
            toastStore.showToast(customMsg, 'error')
          }
      }
    } else {
      toastStore.showToast('ネットワーク接続を確認してください。', 'error')
    }

    return Promise.reject(error)
  }
)