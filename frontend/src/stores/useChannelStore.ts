// src/stores/channelStore.ts
import { defineStore } from "pinia"
import { ChannelClient } from "@/clients/ChannelClient"

export const useChannelStore = defineStore("channel", {
  state: () => ({
    channels: [] as Array<{
      id: number
      name: string
      status: number
      createdAt: string
    }>,

    currentChannel: null as null | {
      id: number
      name: string
      status: number
      createdAt: string
    },

    loading: false
  }),

  getters: {
    hasChannels(state) {
      return state.channels.length > 0
    },
    isSelected(state) {
      return !!state.currentChannel
    }
  },

  actions: {
    setChannels(channels: any[]) {
      this.channels = channels.map((c) => ({
        id: c.id,
        name: c.name,
        status: c.status,
        createdAt: c.createdAt
      }))
    },

    setCurrentChannel(channel: any) {
      const normalized = channel ? {
        id: channel.id,
        name: channel.name,
        status: channel.status,
        createdAt: channel.createdAt
      } : null
      this.currentChannel = normalized
      if (normalized) {
        localStorage.setItem("currentChannel", JSON.stringify(normalized))
      } else {
        localStorage.removeItem("currentChannel")
      }
    },

    clearCurrentChannel() {
      this.currentChannel = null
      localStorage.removeItem("currentChannel")
    },

    restoreCurrentChannel() {
      const saved = localStorage.getItem("currentChannel")
      if (!saved) return

      try {
        const parsed = JSON.parse(saved)
        this.currentChannel = {
          id: parsed.id,
          name: parsed.name,
          status: parsed.status,
          createdAt: parsed.createdAt || parsed.created_at
        }
      } catch {
        console.warn("restoreCurrentChannel(): JSON parse error")
      }
    },

    async fetchChannels() {
      this.loading = true
      try {
        const channels = await ChannelClient.getChannels()
        this.setChannels(channels)
      } finally {
        this.loading = false
      }
    },

    async createChannel(name: string) {
      const created = await ChannelClient.createChannel(name)
      await this.fetchChannels()
      return created
    },

    async editChannel(id: number, name: string, status: number) {
      const edit = await ChannelClient.editChannel(id, name, status)
      if (this.currentChannel?.id === id) {
        this.setCurrentChannel(edit)
      }
      await this.fetchChannels()
      return edit
    },

    async deleteChannel(id: number) {
      await ChannelClient.deleteChannel(id)
      if (this.currentChannel?.id === id) {
        this.clearCurrentChannel()
      }
      await this.fetchChannels()
    },

    async fetchChannelById(id: number) {
      try {
        const ch = await ChannelClient.getChannelById(id)
        if (!ch) return null
        return {
          id: ch.id,
          name: ch.name,
          status: ch.status,
          createdAt: ch.createdAt
        }
      } catch {
        console.warn("fetchChannelById(): チャンネルが存在しません")
        return null
      }
    },

    applyWebSocketEvent(event: any) {
      if (event.event === "channel_deleted") {
        const targetChannelId = event.channelId ?? event.channel_id
        this.channels = this.channels.filter(c => c.id !== targetChannelId)
        if (this.currentChannel?.id === targetChannelId) {
          this.clearCurrentChannel()
        }
      }

      if (event.event === "channel_created") {
        const rawCh = event.channel
        const newCh = {
          id: rawCh.id,
          name: rawCh.name,
          status: 0,
          createdAt: rawCh.createdAt ?? rawCh.created_at
        }
        const exists = this.channels.some(c => c.id === newCh.id)
        if (!exists) {
          this.channels.push(newCh)
        }
      }

      if (event.event === "channel_updated") {
        const rawCh = event.channel
        const targetChannelId = Number(rawCh.id ?? event.channelId ?? event.channel_id)
        if (!targetChannelId) return

        // 1. チャンネル一覧（サイドバー用など）の該当データを更新
        const target = this.channels.find(c => Number(c.id) === targetChannelId)
        if (target) {
          if (rawCh.name !== undefined) target.name = rawCh.name
          if (rawCh.status !== undefined) target.status = Number(rawCh.status)
        }

        // 2. 現在開いているチャンネル（メイン画面用）のデータをリアルタイムに更新
        if (this.currentChannel && Number(this.currentChannel.id) === targetChannelId) {
          const updatedChannel = {
            id: targetChannelId,
            name: rawCh.name ?? this.currentChannel.name,
            status: rawCh.status !== undefined ? Number(rawCh.status) : this.currentChannel.status,
            createdAt: rawCh.createdAt ?? rawCh.created_at ?? this.currentChannel.createdAt
          }
          
          // 一旦セッター関数を通して localStorage も含めて安全に同期させる
          this.setCurrentChannel(updatedChannel)
        }
      }
    }
  }
})