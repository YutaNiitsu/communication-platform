// src/clients/AttachmentClient.ts
import { api } from "@/services/api"

export const AttachmentClient = {
  async upload(messageId: number, file: File) {
    const form = new FormData()
    form.append("file", file)

    const res = await api.post(`/messages/${messageId}/attachments`, form, {
      headers: { "Content-Type": "multipart/form-data" },
      withCredentials: true
    })

    return res.data
  },


  async delete(attachmentId: number) {
    const res = await api.delete(`/attachments/${attachmentId}`, {
      withCredentials: true
    })
    return res.data
  },

  async getStorageUsage() {
    const res = await api.get("/storages/usage")
    console.log("Storage usage response:", res.data) // Debugging line
    console.log(res.request?.responseURL)
    return res.data
  },

  async cleanupStorage() {
    const res = await api.delete("/storages/cleanup")
    return res.data
  }
}
