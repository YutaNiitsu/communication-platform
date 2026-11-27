<!-- src/layouts/AppLayout.vue -->
<template>
  <div class="app-layout-container" @click="handleDocumentClick">
    <!-- 共通ヘッダー -->
    <header class="app-header">
      <div class="app-header__left">
        <div class="app-header__logo" @click="router.push('/home')">
          NSC
        </div>
        <nav class="app-header__nav">
          <button class="btn btn--link" @click="router.push('/home')">ホーム</button>
          <button class="btn btn--link" @click="router.push('/search')">検索</button>
        </nav>
      </div>

      <!-- 右上のユーザーアイコン & プルダウンメニュー -->
      <div class="user-menu-container" ref="menuContainerRef" @click="toggleUserMenu">
        <div 
          class="user-avatar-icon" 
          :style="{ backgroundColor: authStore.user ? getUserColor(authStore.user.userId) : '' }"
        >
          {{ authStore.user?.username ? authStore.user.username.charAt(0).toUpperCase() : 'U' }}
        </div>

        <div v-if="showUserMenu" class="user-dropdown">
          <!-- 管理者の場合のみ「ユーザー管理」を表示 -->
          <div v-if="authStore.isAdmin" class="user-dropdown__item" @click="goUserList">
            ユーザー管理
          </div>
          <!-- 全ユーザー共通で表示 -->
          <div class="user-dropdown__item" @click="goTemplateList">
            テンプレート
          </div>
          <div class="user-dropdown__item" @click="logout">
            ログアウト
          </div>
        </div>
      </div>
    </header>

    <!-- 各画面がここに差し込まれる -->
    <main class="app-main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue"
import { useRouter } from "vue-router"
import { useAuthStore } from "@/stores/useAuthStore"
import { getUserColor } from "@/utils/Color"

const router = useRouter()
const authStore = useAuthStore()

const showUserMenu = ref(false)
const menuContainerRef = ref<HTMLElement | null>(null)

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function goUserList() {
  showUserMenu.value = false
  router.push("/users")
}

function goTemplateList() {
  showUserMenu.value = false
  router.push("/templates")
}

async function logout() {
  showUserMenu.value = false
  try {
    await authStore.logout()
    router.push("/login")
  } catch (e) {
    console.error("ログアウトに失敗しました", e)
  }
}

function handleDocumentClick(event: MouseEvent) {
  if (showUserMenu.value && menuContainerRef.value) {
    const target = event.target as Node
    if (!menuContainerRef.value.contains(target)) {
      showUserMenu.value = false
    }
  }
}
</script>