<!-- src/views/UserListView.vue -->
<template>
  <div class="user-management-main">
    <div class="page-header-row">
      <h2 class="page-title">ユーザー一覧画面</h2>
      <button class="btn-primary" @click="openRegisterModal">+ ユーザー新規登録</button>
    </div>

    <!-- ユーザー一覧リスト -->
    <div class="user-list">
      <div v-for="u in userStore.users" :key="u.userId" class="user-card">
        <!-- ユーザー名列 -->
        <div class="user-info-group col-username">
          <span class="user-label">ユーザー名：</span>
          <span class="user-value username-text">{{ u.username }}</span>
        </div>

        <!-- 権限列（数値から日本語表示に変換: 0=管理者, 1=一般ユーザー） -->
        <div class="user-info-group col-role">
          <span class="user-label">権限：</span>
          <span class="user-value">{{ formatRole(u.role) }}</span>
        </div>

        <!-- ステータス列 -->
        <div class="user-info-group col-status">
          <span class="user-label">ステータス：</span>
          <span class="user-value" :class="u.isActive ? 'status-active' : 'status-inactive'">
            {{ u.isActive ? '有効' : '無効' }}
          </span>
        </div>

        <!-- アクション列 -->
        <div class="user-card-actions">
          <button class="btn-text-action" @click="handleUpdate(u)">更新 〉</button>
          <button class="btn-danger" @click="handleDelete(u.userId)">削除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore, type User } from '@/stores/useUserStore'

const router = useRouter()
const userStore = useUserStore()

// 画面マウント時にユーザー一覧を取得
onMounted(async () => {
  await fetchUsers()
})

// ユーザー一覧の取得処理
const fetchUsers = async () => {
  try {
    await userStore.fetchAllUsers()
  } catch (error) {
    console.error('ユーザー一覧の取得に失敗しました', error)
  }
}

// 権限の数値を日本語に変換する関数 (0: 管理者, 1: 一般ユーザー)
const formatRole = (role: number) => {
  switch (role) {
    case 0:
      return '管理者'
    case 1:
      return '一般ユーザー'
    default:
      return '不明'
  }
}

// ユーザー新規登録画面へ遷移
const openRegisterModal = () => {
  router.push('/users/create')
}

// ユーザー更新画面へ遷移 (対象ユーザーのIDをパスに渡す)
const handleUpdate = (user: User) => {
  router.push(`/users/${user.userId}/edit`)
}

// ユーザー削除処理
const handleDelete = async (userId: number) => {
  if (confirm('このユーザーを削除してもよろしいですか？')) {
    try {
      await userStore.deleteUser(userId)
      alert('ユーザーを削除しました')
      // 削除完了後に一覧を再取得して画面を最新化
      await fetchUsers()
    } catch (error) {
      console.error('ユーザーの削除に失敗しました', error)
      alert('削除処理に失敗しました')
    }
  }
}
</script>