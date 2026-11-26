<!-- src/components/TemplateFormModal.vue -->
<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-card template-form-modal">
      <h2 class="modal-title">
        {{ mode === 'view' ? 'テンプレート編集（閲覧専用モード）' : (mode === 'create' ? 'テンプレート作成' : 'テンプレート編集') }}
      </h2>

      <div class="form-body">
        <!-- 共有設定 -->
        <div class="form-group">
          <label class="form-label">共有設定</label>
          <div class="radio-group">
            <label class="radio-label">
              <input
                type="radio"
                :value="false"
                v-model="form.isShared"
                :disabled="mode === 'view' || (!authStore.isAdmin && mode === 'edit' && form.isShared)"
              />
              個人テンプレート
            </label>
            <label class="radio-label" v-if="authStore.isAdmin || form.isShared">
              <input
                type="radio"
                :value="true"
                v-model="form.isShared"
                :disabled="mode === 'view' || !authStore.isAdmin"
              />
              共有テンプレート
            </label>
          </div>
        </div>

        <!-- タイトル -->
        <div class="form-group">
          <label class="form-label">タイトル</label>
          <input
            type="text"
            v-model="form.title"
            class="form-input"
            :readonly="mode === 'view'"
            placeholder="タイトルを入力"
          />
        </div>

        <!-- カテゴリ -->
        <div class="form-group">
          <label class="form-label">カテゴリ</label>
          <select v-model.number="form.categoryId" class="form-select" :disabled="mode === 'view'">
            <option v-for="cat in templateStore.categories" :key="cat.id" :value="cat.id">
              {{ cat.name }} ({{ cat.isShared ? '共有' : '個人' }})
            </option>
          </select>
        </div>

        <!-- 本文 -->
        <div class="form-group">
          <label class="form-label">本文</label>
          <textarea
            v-model="form.content"
            class="form-textarea"
            rows="8"
            maxlength="500"
            :readonly="mode === 'view'"
            placeholder="テンプレート本文を入力"
          ></textarea>
          <div class="char-count-row">
            <span class="note">※500文字以内</span>
            <span class="count">現在の文字数 : {{ form.content.length }} / 500</span>
          </div>
        </div>
      </div>

      <!-- アクションボタンエリア -->
      <div class="modal-footer">
        <template v-if="mode === 'view'">
          <button class="btn btn--secondary footer-btn-right" @click="$emit('close')">閉じる</button>
        </template>
        
        <template v-else>
          <div class="footer-actions-container">
            <button class="btn btn--primary" @click="submit">
              {{ mode === 'create' ? '作成' : '更新' }}
            </button>
            <button class="btn btn--secondary" @click="$emit('close')">
              キャンセル
            </button>
          </div>
        </template>
      </div>
    </div>
  </div>
  <AlertDialog
    :visible="visible"
    :title="title"
    :message="message"
    :type="type"
    @confirm="confirm"
    @cancel="cancel"
  />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { useMessageTemplateStore } from '@/stores/useMessageTemplateStore'
import AlertDialog from "@/components/AlertDialog.vue"
import { useAlertDialog } from "@/composables/useAlertDialog"

const {
  visible,
  title,
  message,
  type,
  confirm,
  cancel,
  //showSuccess,
  //showWarning,
  showError
} = useAlertDialog()

const props = defineProps<{
  mode: 'create' | 'edit' | 'view'
  initialData?: {
    id?: number
    title: string
    categoryId: number
    content: string
    isShared: boolean
    userId?: number
  }
}>()

const emit = defineEmits(['close', 'save'])
const authStore = useAuthStore()
const templateStore = useMessageTemplateStore()

const form = ref({
  title: '',
  categoryId: templateStore.categories[0]?.id || 1,
  content: '',
  isShared: false
})

onMounted(() => {
  if (props.initialData) {
    form.value = { 
      title: props.initialData.title,
      categoryId: props.initialData.categoryId,
      content: props.initialData.content,
      isShared: props.initialData.isShared
    }
  } else if (templateStore.categories.length > 0) {
    form.value.categoryId = templateStore.categories[0].id
  }
})

function submit() {
  const trimmedTitle = form.value.title.trim()
  const trimmedContent = form.value.content.trim()

  if (!trimmedTitle || !trimmedContent) {
    showError("テンプレート作成エラー", "タイトルと本文を入力してください")
    return
  }

  const currentUserId = authStore.user?.userId
  const targetId = props.initialData?.id

  const isDuplicate = templateStore.hasDuplicateTitle(
    trimmedTitle, 
    form.value.isShared, 
    targetId, 
    currentUserId
  )

  if (isDuplicate) {
    if (form.value.isShared) {
      showError("テンプレート作成エラー", "すでに同じタイトルの「共有テンプレート」が存在します。")
    } else {
      showError("テンプレート作成エラー", "すでに同じタイトルの「個人テンプレート」があなたのアカウントに存在します。")
    }
    return
  }

  emit('save', form.value)
  emit('close')
}
</script>