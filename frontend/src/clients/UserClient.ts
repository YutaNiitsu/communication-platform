import { api } from "@/services/api"

export interface UserCreatePayload {
  username: string
  password?: string
  role: number
  isActive: boolean
}

export interface UserUpdatePayload {
  username?: string
  password?: string
  role?: number
  isActive?: boolean
}

export const UserClient = {
  async getUserById(userId: number) {
    return api.get(`/users/${userId}`)
  },

  async getAllUsers() {
    return api.get("/users")
  },

  async createUser(payload: UserCreatePayload) {
    const body = {
      username: payload.username,
      password: payload.password,
      role: payload.role,
      is_active: payload.isActive
    }
    return api.post("/users", body)
  },

  async updateUser(userId: number, payload: UserUpdatePayload) {
    const body: any = {}
    if (payload.username !== undefined) body.username = payload.username
    if (payload.password !== undefined) body.password = payload.password
    if (payload.role !== undefined) body.role = payload.role
    if (payload.isActive !== undefined) body.is_active = payload.isActive

    return api.put(`/users/${userId}`, body)
  },

  async deleteUser(userId: number) {
    return api.delete(`/users/${userId}`)
  }
}