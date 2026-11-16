import { api } from "@/services/api"

export const AuthClient = {
  async login(username: string, password: string) {
    return api.post("/auth/login", { username, password })
  },

  async me() {
    return api.get("/auth/me")
  },

  async logout() {
    return api.post("/auth/logout")
  },
}