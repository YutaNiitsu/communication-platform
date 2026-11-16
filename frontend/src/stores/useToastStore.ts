import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useToastStore = defineStore('toast', () => {
  const message = ref<string | null>(null)
  const type = ref<'success' | 'error' | 'info'>('error')
  let timer: number | null = null

  const showToast = (msg: string, toastType: 'success' | 'error' | 'info' = 'error', duration = 3000) => {
    if (timer) {
      clearTimeout(timer)
    }
    message.value = msg
    type.value = toastType

    timer = window.setTimeout(() => {
      message.value = null
      timer = null
    }, duration)
  }

  const clearToast = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
    message.value = null
  }

  return {
    message,
    type,
    showToast,
    clearToast,
  }
})