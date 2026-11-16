// src/clients/ConfigClient.ts
import { api } from "@/services/api"

export const ConfigClient = {
  async getConfig() {
    const res = await api.get("/configs")
    return res.data
  }
}