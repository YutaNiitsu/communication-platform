// src/clients/ChannelClient.ts
import { api } from "@/services/api"

export const ChannelClient = {
  async getChannels() {
    const res = await api.get("/channels")
    return res.data
  },

  async getChannelById(id: number) {
    const res = await api.get(`/channels/${id}`)
    return res.data
  },

  async createChannel(name: string) {
    const res = await api.post("/channels", { name })
    return res.data
  },

  async editChannel(id: number ,name: string, status: number) {
    const res = await api.put(`/channels/${id}`, { name, status })
    return res.data
  },

  async getMembers(channelId: number) {
    const res = await api.get(`/channels/${channelId}/members`)
    return res.data
  },

  async deleteChannel(id: number) {
    const res = await api.delete(`/channels/${id}`)
    return res.data
  },

  async joinChannel(channelId: number, userId: number) {
    const res = await api.post(`/channels/${channelId}/join`, {
      user_id: userId,
    })
    return res.data
  },

  async leaveChannel(channelId: number, userId: number) {
    const res = await api.post(`/channels/${channelId}/leave`, {
      user_id: userId,
    })
    return res.data
  },
}