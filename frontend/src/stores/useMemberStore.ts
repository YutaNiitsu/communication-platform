// src/stores/MemberStore.ts
import { defineStore } from "pinia"
import { ChannelClient } from "@/clients/ChannelClient"
import { useChannelStore } from "@/stores/useChannelStore"

export const useMemberStore = defineStore("member", {
  state: () => ({
    members: [] as Array<{
      userId: number
      username: string
      role: number // 0: 管理者, 1: 一般ユーザー
      isActive: boolean
      joinedAt: string
    }>,
    loading: false
  }),

  actions: {
    async fetchMembers(channelId: number) {
      this.loading = true
      try {
        const members = await ChannelClient.getMembers(channelId)
        this.members = members.map((m: any) => ({
          userId: m.userId,
          username: m.username,
          role: m.role,
          isActive: m.isActive ?? true,
          joinedAt: m.joinedAt
        }))
      } finally {
        this.loading = false
      }
    },

    async joinChannel(channelId: number, userId: number) {
      await ChannelClient.joinChannel(channelId, userId)
      await this.fetchMembers(channelId)
    },

    async leaveChannel(channelId: number, userId: number) {
      await ChannelClient.leaveChannel(channelId, userId)
      await this.fetchMembers(channelId)
    },

    async addMember(channelId: number, userId: number) {
      await ChannelClient.joinChannel(channelId, userId)
      await this.fetchMembers(channelId)
    },

    async applyWebSocketEvent(event: any) {
      if (event.event === "channel_joined" || event.event === "channel_left") {
        const eventChannelId = event.channelId ?? event.channel_id
        const channelStore = useChannelStore()

        if (channelStore.currentChannel?.id === eventChannelId) {
          await this.fetchMembers(eventChannelId)
        }
      }
    }
  }
})