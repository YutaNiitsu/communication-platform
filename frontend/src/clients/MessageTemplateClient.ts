import { api } from "@/services/api"

// --- DTO & 型定義（フロント側はキャメルケースに統一） ---
export interface MessageTemplateCategoryResponseDTO {
  id: number
  userId: number
  name: string
  isShared: boolean
  createdAt: string
  updatedAt: string
}

export interface MessageTemplateCategoryCreateRequestDTO {
  name: string
  isShared: boolean
}

export interface MessageTemplateCategoryEditRequestDTO {
  name: string
  isShared: boolean
}

export interface MessageTemplateResponseDTO {
  id: number
  userId: number
  title: string
  content: string
  categoryId: number
  isShared: boolean
  createdAt: string
  updatedAt: string
}

export interface MessageTemplateCreateRequestDTO {
  title: string
  content: string
  categoryId: number
  isShared: boolean
}

export interface MessageTemplateEditRequestDTO {
  title: string
  content: string
  categoryId: number
  isShared: boolean
}

// --- クライアント定義 ---
export const MessageTemplateClient = {
  async getCategories(isShared?: boolean) {
    const params = isShared !== undefined ? { is_shared: isShared } : {}
    return api.get<MessageTemplateCategoryResponseDTO[]>("/message_template_categories", { params })
  },
  async createCategory(payload: MessageTemplateCategoryCreateRequestDTO) {
    const body = { name: payload.name, is_shared: payload.isShared }
    return api.post<MessageTemplateCategoryResponseDTO>("/message_template_categories", body)
  },
  async updateCategory(id: number, payload: MessageTemplateCategoryEditRequestDTO) {
    const body = { name: payload.name, is_shared: payload.isShared }
    return api.put<MessageTemplateCategoryResponseDTO>(`/message_template_categories/${id}`, body)
  },
  async deleteCategory(id: number) {
    return api.delete(`/message_template_categories/${id}`)
  },

  async getTemplates(isShared?: boolean) {
    const params = isShared !== undefined ? { is_shared: isShared } : {}
    return api.get<MessageTemplateResponseDTO[]>("/message_templates", { params })
  },
  async createTemplate(payload: MessageTemplateCreateRequestDTO) {
    const body = {
      title: payload.title,
      content: payload.content,
      category_id: payload.categoryId,
      is_shared: payload.isShared
    }
    return api.post<MessageTemplateResponseDTO>("/message_templates", body)
  },
  async updateTemplate(id: number, payload: MessageTemplateEditRequestDTO) {
    const body = {
      title: payload.title,
      content: payload.content,
      category_id: payload.categoryId,
      is_shared: payload.isShared
    }
    return api.put<MessageTemplateResponseDTO>(`/message_templates/${id}`, body)
  },
  async deleteTemplate(id: number) {
    return api.delete(`/message_templates/${id}`)
  }
}