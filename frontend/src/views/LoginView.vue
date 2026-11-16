<!-- src/views/LoginView.vue -->
<template>
  <div class="container login__container">
    <h1>ログイン</h1>

    <LoginForm @login="handleLogin" />

    <p v-if="authStore.loginError" class="login__error">{{ authStore.loginError }}</p>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router"
import { useAuthStore } from "@/stores/useAuthStore"
import LoginForm from "@/components/LoginForm.vue"

const router = useRouter()
const authStore = useAuthStore()

async function handleLogin(username: string, password: string) {
  try {
    await authStore.login(username, password)
    console.log('1. ログイン成功後のuser:', authStore.user)
    await router.push("/home")
    console.log('2. 遷移完了後のパス:', router.currentRoute.value.path)
  } catch {
    // エラーメッセージの設定は Pinia ストア（useAuthStore）側で完結しているため、
    // ここでは catch で再代入する必要はありません。
  }
}
</script>