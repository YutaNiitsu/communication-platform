// src/stores/ConfigStore.ts
import { defineStore } from "pinia"
import { ConfigClient } from "@/clients/ConfigClient"
import type { AppConfig } from "@/types/config"

export const useConfigStore = defineStore("config", {
  state: () => ({
    config: null as AppConfig | null,
    loaded: false,
    loadFailed: false,
    errorMessage: ""
  }),

  getters: {
    isAvailable: (state) =>
      state.loaded &&
      !state.loadFailed &&
      state.config !== null
  },

  actions: {
    async loadConfig() {
      try {
        const configData = await ConfigClient.getConfig()
        if (!configData) {
          throw new Error("設定情報が取得できませんでした")
        }

        if (
          !configData.message ||
          !configData.attachment ||
          !configData.storage
        ) {
          throw new Error(
            "設定情報が不正です"
          )
        }

        this.config = configData

      } catch (error) {
        
        this.loadFailed = true
        this.errorMessage =
          "設定情報の取得に失敗しました"

        console.error(error)
      } finally {

        this.loaded = true

      }
    }
  }
})