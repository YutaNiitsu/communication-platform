// src/clients/ReactionClient.ts
import { api } from "@/services/api"

export interface ReactionSummary {
  reactionType: string
  count: number
  users: number[]
}

export const ReactionClient = {
  async addReaction(messageId: number, reactionType: string) {
    const res = await api.post(`/messages/${messageId}/reactions`, {
      reaction_type: reactionType,
    })
    return res.data
  },

  async removeReaction(messageId: number, reactionType: string) {
    const res = await api.delete(`/messages/${messageId}/reactions`, {
      data: { reaction_type: reactionType },
    })
    return res.data
  },

  async getReactions(messageId: number): Promise<ReactionSummary[]> {
    const res = await api.get(`/messages/${messageId}/reactions`)
    return res.data
  },
}