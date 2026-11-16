// src/clients/SearchClient.ts
import { api } from "@/services/api"

export interface SearchResponseDTO {
  id: number
  channelId: number
  userId: number
  content: string
  createdAt: string
}

export interface SearchParams {
  keyword?: string
  channelId?: number | null
  userId?: number | null
  from?: string | null
  to?: string | null
  requestUserId: number
}

export const SearchClient = {
  async search(params: SearchParams): Promise<SearchResponseDTO[]> {
    const res = await api.get("/search", {
      params: {
        keyword: params.keyword || undefined,
        channel_id: params.channelId ?? undefined,
        user_id: params.userId ?? undefined,
        from: params.from || undefined,
        to: params.to || undefined,
        request_user_id: params.requestUserId,
      },
    })
    return res.data
  },
}