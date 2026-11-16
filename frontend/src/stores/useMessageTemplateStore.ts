import { defineStore } from "pinia"
import { 
  MessageTemplateClient, 
  type MessageTemplateResponseDTO, 
  type MessageTemplateCategoryResponseDTO,
  type MessageTemplateCreateRequestDTO,
  type MessageTemplateEditRequestDTO,
  type MessageTemplateCategoryCreateRequestDTO,
  type MessageTemplateCategoryEditRequestDTO
} from "../clients/MessageTemplateClient"
import { useAuthStore } from "./useAuthStore"

export const useMessageTemplateStore = defineStore("messageTemplate", {
  state: () => ({
    templates: [] as MessageTemplateResponseDTO[],
    categories: [] as MessageTemplateCategoryResponseDTO[],
    loading: false
  }),

  actions: {
    async fetchTemplates() {
      this.loading = true
      try {
        const [sharedRes, personalRes] = await Promise.all([
          MessageTemplateClient.getTemplates(true).catch(() => ({ data: [] })),
          MessageTemplateClient.getTemplates(false).catch(() => ({ data: [] }))
        ])

        console.log("Shared templates:", sharedRes.data)
        console.log("Personal templates:", personalRes.data)
        
        const shared = sharedRes.data || []
        const personal = personalRes.data || []

        const authStore = useAuthStore()
        const currentUserId = authStore.user?.userId

        const filteredPersonal = personal.filter(t => {
          if (currentUserId === undefined || currentUserId === null) return false
          return Number(t.userId) === Number(currentUserId)
        })
        
        const map = new Map<number, MessageTemplateResponseDTO>()
        ;[...shared, ...filteredPersonal].forEach(t => {
          map.set(t.id, t)
        })
        this.templates = Array.from(map.values())
      } catch (e) {
        console.warn("Failed to fetch templates:", e)
        this.templates = []
      } finally {
        this.loading = false
      }
    },

    async createTemplate(payload: MessageTemplateCreateRequestDTO) {
      await MessageTemplateClient.createTemplate(payload)
      await this.fetchTemplates()
    },

    async updateTemplate(id: number, payload: MessageTemplateEditRequestDTO) {
      await MessageTemplateClient.updateTemplate(id, payload)
      await this.fetchTemplates()
    },

    async deleteTemplate(id: number) {
      await MessageTemplateClient.deleteTemplate(id)
      this.templates = this.templates.filter(t => t.id !== id)
    },

    async fetchCategories() {
      try {
        const [sharedRes, personalRes] = await Promise.all([
          MessageTemplateClient.getCategories(true).catch(() => ({ data: [] })),
          MessageTemplateClient.getCategories(false).catch(() => ({ data: [] }))
        ])

        const shared = sharedRes.data || []
        const personal = personalRes.data || []

        const authStore = useAuthStore()
        const currentUserId = authStore.user?.userId

        const filteredPersonalCategories = personal.filter(c => {
          if (currentUserId === undefined || currentUserId === null) return false
          return Number(c.userId) === Number(currentUserId)
        })

        const map = new Map<number, MessageTemplateCategoryResponseDTO>()
        ;[...shared, ...filteredPersonalCategories].forEach(c => {
          map.set(c.id, c)
        })
        this.categories = Array.from(map.values())
      } catch (e) {
        console.warn("Failed to fetch categories:", e)
        this.categories = []
      }
    },

    async createCategory(payload: MessageTemplateCategoryCreateRequestDTO) {
      await MessageTemplateClient.createCategory(payload)
      await this.fetchCategories()
    },

    async updateCategory(id: number, payload: MessageTemplateCategoryEditRequestDTO) {
      await MessageTemplateClient.updateCategory(id, payload)
      await this.fetchCategories()
    },

    async deleteCategory(id: number) {
      await MessageTemplateClient.deleteCategory(id)
      this.categories = this.categories.filter(c => c.id !== id)
    },

    hasDuplicateTitle(title: string, isShared: boolean, currentId?: number, currentUserId?: number): boolean {
      return this.templates.some(t => {
        if (currentId !== undefined && t.id === currentId) {
          return false
        }
        if (t.title !== title) {
          return false
        }
        if (isShared && t.isShared) {
          return true
        }
        if (!isShared && !t.isShared && currentUserId !== undefined && Number(t.userId) === Number(currentUserId)) {
          return true
        }
        return false
      })
    }
  }
})