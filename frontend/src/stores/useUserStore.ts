import { defineStore } from "pinia"
import { UserClient } from "../clients/UserClient"
import type { User } from "./useAuthStore"
export type { User } from "./useAuthStore"

export const useUserStore = defineStore("user", {
  state: () => ({
    users: [] as User[]
  }),

  actions: {
    async fetchAllUsers() {
      try {
        const res = await UserClient.getAllUsers()
        const rawUsers = res.data || []
        this.users = rawUsers.map((u: any) => ({
          userId: u.userId,
          username: u.username,
          role: u.role,
          isActive: u.isActive ?? true
        }))
        return this.users
      } catch (e) {
        console.warn("Failed to fetch all users (permission denied or error):", e)
        return this.users
      }
    },

    async fetchUserById(userId: number) {
      let user = this.users.find(u => u.userId === userId)
      if (user) return user

      try {
        const res = await UserClient.getUserById(userId)
        if (res && res.data) {
          const u = res.data
          const mappedUser: User = {
            userId: u.userId,
            username: u.username,
            role: u.role,
            isActive: u.isActive ?? true
          }
          this.users.push(mappedUser)
          return mappedUser
        }
      } catch (e) {
        console.warn(`Failed to fetch user by id ${userId}:`, e)
      }

      return {
        userId: userId,
        username: `User_${userId}`,
        role: 0,
        isActive: true
      } as User
    },

    async createUser(payload: { username: string; password?: string; role: number; isActive: boolean }) {
      await UserClient.createUser(payload)
    },

    async updateUser(userId: number, payload: { username?: string; password?: string; role?: number; isActive?: boolean }) {
      await UserClient.updateUser(userId, payload)
    },

    async deleteUser(userId: number) {
      await UserClient.deleteUser(userId)
    }
  }
})