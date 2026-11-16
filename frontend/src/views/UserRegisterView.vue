<!-- src/views/UserRegisterView.vue -->
<template>
  <div class="user-form-container">
    <div class="user-form-card">
      <h2 class="page-title">{{ isEditMode ? 'ユーザー更新' : 'ユーザー登録' }}</h2>

      <form @submit.prevent="handleSubmit">
        <!-- ユーザー名 -->
        <div class="form-group">
          <label class="form-label">ユーザー名</label>
          <input
            type="text"
            v-model="form.username"
            class="form-input"
            required
          />
        </div>

        <!-- パスワード -->
        <div class="form-group">
          <label class="form-label">パスワード</label>
          <div class="password-wrapper">
            <input
              type="text"
              v-model="form.password"
              :class="['form-input', 'password-input', { 'show-text': showPassword }]"
              :placeholder="isEditMode ? '変更する場合のみ入力してください' : ''"
              :required="!isEditMode"
            />
            <button 
              type="button" 
              class="password-toggle-btn" 
              @click="showPassword = !showPassword"
              :title="showPassword ? 'パスワードを隠す' : 'パスワードを表示する'"
            >
              {{ showPassword ? '隠す' : '表示' }}
            </button>
          </div>
        </div>

        <!-- 権限選択 (0: 管理者, 1: 一般ユーザー) -->
        <div class="form-group">
          <label class="form-label">権限選択</label>
          <div class="radio-group">
            <label class="radio-label">
              <input
                type="radio"
                :value="0"
                v-model.number="form.role"
              />
              管理者(admin)
            </label>
            <label class="radio-label">
              <input
                type="radio"
                :value="1"
                v-model.number="form.role"
              />
              一般ユーザー(user)
            </label>
          </div>
        </div>

        <!-- ステータス -->
        <div class="form-group">
          <label class="form-label">ステータス</label>
          <div class="checkbox-group">
            <input type="checkbox" id="isActiveCheck" v-model="form.isActive" class="checkbox-input" />
            <label for="isActiveCheck" class="checkbox-label-text">有効（チェックを外すとログインできなくなります）</label>
          </div>
        </div>

        <!-- アクションボタン -->
        <div class="form-actions">
          <button type="submit" class="btn-primary">
            {{ isEditMode ? '更新' : '登録' }}
          </button>
          <button type="button" class="btn-secondary" @click="handleCancel">
            キャンセル
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// URLのパラメータに id が存在するかどうかで編集モードか判定
const userId = computed(() => route.params.id)
const isEditMode = computed(() => !!userId.value)

// パスワードの表示・非表示を切り替えるフラグ
const showPassword = ref(false)

// フォームの入力値（roleは数値の 1 をデフォルトに設定）
const form = ref({
  username: '',
  password: '',
  role: 1,
  isActive: true
})

onMounted(async () => {
  // 更新モードの場合のみ、既存ユーザー情報を取得してフォームに埋め込む
  if (isEditMode.value && userId.value) {
    try {
      const targetUser = await userStore.fetchUserById(Number(userId.value))
      if (targetUser) {
        form.value.username = targetUser.username
        form.value.role = targetUser.role
        form.value.isActive = targetUser.isActive
      }
    } catch (error) {
      console.error('ユーザー情報の取得に失敗しました', error)
    }
  }
})

// 送信処理（登録 or 更新）
const handleSubmit = async () => {
  try {
    if (isEditMode.value) {
      await userStore.updateUser(Number(userId.value), form.value)
      alert('ユーザーを更新しました')
    } else {
      await userStore.createUser(form.value)
      alert('ユーザーを登録しました')
    }
    router.push('/users')
  } catch (error) {
    console.error('処理に失敗しました', error)
    alert('処理に失敗しました')
  }
}

// キャンセルボタン
const handleCancel = () => {
  router.back()
}
</script>

