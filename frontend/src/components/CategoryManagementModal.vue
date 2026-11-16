<!-- src/components/CategoryManagementModal.vue -->
<template>
  <div class="modal-overlay" @click.self="closeModal">
    <div class="modal-container">
      <!-- モーダルヘッダー -->
      <div class="modal-header">
        <h2>カテゴリ管理</h2>
      </div>

      <div class="modal-body">
        <!-- 1. カテゴリ登録 / 編集エリア -->
        <section class="form-section">
          <h3>{{ isEditing ? 'カテゴリ編集' : 'カテゴリ登録' }}</h3>
          
          <div class="form-group">
            <input
              type="text"
              v-model="categoryName"
              placeholder="カテゴリ名を入力"
              class="form-input"
            />
          </div>

          <div v-if="authStore.isAdmin" class="checkbox-row">
            <label class="checkbox-label">
              <input type="checkbox" v-model="isShared" />
              <span>共有カテゴリとして登録</span>
            </label>
          </div>

          <div class="btn-group-left">
            <template v-if="isEditing">
              <button class="btn btn-primary" @click="handleUpdate">更新</button>
              <button class="btn btn-secondary" @click="resetForm">キャンセル</button>
            </template>
            <template v-else>
              <button class="btn btn-primary" @click="handleRegister">登録</button>
              <button class="btn btn-secondary" @click="resetForm">キャンセル</button>
            </template>
          </div>
        </section>

        <!-- 2. 登録済カテゴリー一覧 -->
        <section class="list-section">
          <h3>登録済カテゴリー一覧</h3>

          <div class="table-wrapper">
            <table class="category-table">
              <thead>
                <tr>
                  <th>カテゴリ名</th>
                  <th>種別</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="cat in templateStore.categories" :key="cat.id">
                  <td>{{ cat.name }}</td>
                  <td>{{ cat.isShared ? '共有' : '個人' }}</td>
                  <td class="action-cell">
                    <template v-if="canManage(cat)">
                      <button class="btn btn-sm btn-secondary" @click="startEdit(cat)">編集</button>
                      <button class="btn btn-sm btn-danger" @click="deleteCategory(cat.id)">削除</button>
                    </template>
                    <template v-else>
                      <span class="no-permission">—</span>
                    </template>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </div>

      <!-- モーダルフッター -->
      <div class="modal-footer">
        <button class="btn btn-secondary" @click="closeModal">閉じる</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { useMessageTemplateStore } from '@/stores/useMessageTemplateStore'

const emit = defineEmits(['close'])
const authStore = useAuthStore()
const templateStore = useMessageTemplateStore()

// フォームの入力ステート
const categoryName = ref('')
const isShared = ref(false)

// 編集モード管理ステート
const isEditing = ref(false)
const editingTargetId = ref<number | null>(null)

function canManage(cat: any) {
  const currentUserId = authStore.user?.userId
  if (!cat.isShared && Number(cat.userId) === Number(currentUserId)) return true
  if (cat.isShared && authStore.isAdmin) return true
  return false
}

function startEdit(cat: any) {
  isEditing.value = true
  editingTargetId.value = cat.id
  categoryName.value = cat.name
  isShared.value = Boolean(cat.isShared)
}

async function handleUpdate() {
  if (!categoryName.value.trim() || editingTargetId.value === null) return

  try {
    await templateStore.updateCategory(editingTargetId.value, {
      name: categoryName.value.trim(),
      isShared: isShared.value
    })
    resetForm()
  } catch (e: any) {
    alert(e.response?.data?.message || 'カテゴリの更新に失敗しました')
  }
}

async function handleRegister() {
  if (!categoryName.value.trim()) return
  
  try {
    await templateStore.createCategory({
      name: categoryName.value.trim(),
      isShared: isShared.value
    })
    resetForm()
  } catch (e: any) {
    alert(e.response?.data?.message || 'カテゴリの登録に失敗しました')
  }
}

function resetForm() {
  categoryName.value = ''
  isShared.value = false
  isEditing.value = false
  editingTargetId.value = null
}

async function deleteCategory(id: number) {
  if (confirm('このカテゴリを削除しますか？')) {
    try {
      await templateStore.deleteCategory(id)
      if (editingTargetId.value === id) {
        resetForm()
      }
    } catch (e: any) {
      alert(e.response?.data?.message || 'カテゴリの削除に失敗しました')
    }
  }
}

function closeModal() {
  emit('close')
}
</script>