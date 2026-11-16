<!-- src/views/TemplateListView.vue -->
<template>
  <div class="container template-list__container">
    <!-- ヘッダー・操作エリア -->
    <div class="template-list__header">
      <h1>テンプレート管理</h1>
      <div class="template-list__actions">
        <button class="btn btn--secondary" @click="showCategoryModal = true">カテゴリ管理</button>
        <button class="btn btn--primary" @click="openCreateModal">+ 新規作成</button>
      </div>
    </div>

    <!-- 絞り込みフィルターバー -->
    <div class="template-filter-bar">
      <div class="filter-group">
        <label>表示対象：</label>
        <label class="checkbox-label">
          <input type="checkbox" v-model="filterPersonal" /> 個人テンプレート
        </label>
        <label class="checkbox-label">
          <input type="checkbox" v-model="filterShared" /> 共有テンプレート
        </label>
      </div>

      <div class="filter-group">
        <label>カテゴリ：</label>
        <select v-model="selectedCategory" class="form-select">
          <option value="すべて">すべて</option>
          <option v-for="cat in templateStore.categories" :key="cat.id" :value="cat.name">
            {{ cat.name }}
          </option>
        </select>
      </div>

      <div class="filter-group">
        <label>キーワード：</label>
        <input type="text" v-model="searchKeyword" placeholder="検索キーワード" class="form-input" />
      </div>
    </div>

    <!-- テンプレートグリッド一覧 -->
    <div class="template-grid">
      <div
        v-for="item in filteredTemplates"
        :key="item.id"
        class="template-card"
      >
        <div class="template-card__header">
          <div class="template-card__title">タイトル：{{ item.title }}</div>
          <span class="template-card__badge">[ {{ item.isShared ? '共有' : '個人' }} ]</span>
        </div>

        <div class="template-card__body">
          <div class="template-card__category">カテゴリ：{{ getCategoryName(item.categoryId) }}</div>
        </div>

        <div class="template-card__footer">
          <button class="btn btn--secondary btn--sm" @click="openEditModal(item)">
            編集
          </button>
          <button
            v-if="canDelete(item)"
            class="btn btn--danger btn--sm"
            @click="deleteTemplate(item.id)"
          >
            削除
          </button>
        </div>
      </div>
    </div>

    <!-- カテゴリ管理モーダル -->
    <CategoryManagementModal
      v-if="showCategoryModal"
      @close="showCategoryModal = false"
    />

    <!-- テンプレート作成・編集モーダル -->
    <TemplateFormModal
      v-if="showFormModal"
      :mode="formMode"
      :initial-data="targetTemplate"
      @close="showFormModal = false"
      @save="handleSaveTemplate"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { useMessageTemplateStore } from '@/stores/useMessageTemplateStore'
import CategoryManagementModal from '@/components/CategoryManagementModal.vue'
import TemplateFormModal from '@/components/TemplateFormModal.vue'

const authStore = useAuthStore()
const templateStore = useMessageTemplateStore()

// フィルター用ステート
const filterPersonal = ref(true)
const filterShared = ref(true)
const selectedCategory = ref('すべて')
const searchKeyword = ref('')

// モーダル用ステート
const showCategoryModal = ref(false)
const showFormModal = ref(false)
const formMode = ref<'create' | 'edit' | 'view'>('create')
const targetTemplate = ref<any>(null)

onMounted(async () => {
  await Promise.all([
    templateStore.fetchTemplates(),
    templateStore.fetchCategories()
  ])
})

function getCategoryName(categoryId: number) {
  const targetId = Number(categoryId)
  const cat = templateStore.categories.find(c => Number(c.id) === targetId)
  return cat ? cat.name : '未分類'
}

const filteredTemplates = computed(() => {
  return templateStore.templates.filter(item => {
    const isShared = Boolean(item.isShared)
    if (!filterPersonal.value && !isShared) return false
    if (!filterShared.value && isShared) return false

    if (selectedCategory.value !== 'すべて') {
      const catName = getCategoryName(item.categoryId)
      if (catName !== selectedCategory.value) return false
    }

    if (searchKeyword.value.trim()) {
      const kw = searchKeyword.value.toLowerCase()
      const matchTitle = item.title.toLowerCase().includes(kw)
      const matchContent = item.content.toLowerCase().includes(kw)
      if (!matchTitle && !matchContent) return false
    }

    return true
  })
})

function canDelete(item: any) {
  const currentUserId = authStore.user?.userId
  if (!item.isShared && Number(item.userId) === Number(currentUserId)) return true
  if (item.isShared && authStore.isAdmin) return true
  return false
}

function openCreateModal() {
  formMode.value = 'create'
  targetTemplate.value = null
  showFormModal.value = true
}

function openEditModal(item: any) {
  targetTemplate.value = item
  if (item.isShared && !authStore.isAdmin) {
    formMode.value = 'view'
  } else {
    formMode.value = 'edit'
  }
  showFormModal.value = true
}

async function handleSaveTemplate(data: any) {
  try {
    if (formMode.value === 'create') {
      await templateStore.createTemplate({
        title: data.title,
        content: data.content,
        categoryId: data.categoryId,
        isShared: data.isShared
      })
    } else if (formMode.value === 'edit' && targetTemplate.value) {
      await templateStore.updateTemplate(targetTemplate.value.id, {
        title: data.title,
        content: data.content,
        categoryId: data.categoryId,
        isShared: data.isShared
      })
    }
  } catch (e: any) {
    alert(e.response?.data?.message || '保存に失敗しました')
  }
}

async function deleteTemplate(id: number) {
  if (confirm('本当にこのテンプレートを削除しますか？')) {
    try {
      await templateStore.deleteTemplate(id)
    } catch (e: any) {
      alert(e.response?.data?.message || '削除に失敗しました')
    }
  }
}
</script>