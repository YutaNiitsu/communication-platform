// src/stores/SearchStore.ts
import { defineStore } from 'pinia'
import { SearchClient } from '@/clients/SearchClient'

export const useSearchStore = defineStore('search', {
  state: () => ({
    query: '',
    channelId: null as number | null,
    userFilter: null as number | null,
    dateFrom: null as string | null,
    dateTo: null as string | null,

    results: [] as any[],
    loading: false,
    error: null as string | null
  }),

  getters: {
    hasResults(state): boolean {
      return state.results.length > 0
    }
  },

  actions: {
    setQuery(query: string) {
      this.query = query
    },

    setChannelIdFilter(channelId: number | null) {
      this.channelId = channelId
    },

    setUserFilter(userId: number | null) {
      this.userFilter = userId
    },

    setDateRange(from: string | null | undefined, to: string | null | undefined) {
      this.dateFrom = from && from.trim() !== '' ? from.trim() : null
      this.dateTo = to && to.trim() !== '' ? to.trim() : null
    },

    clearFilters() {
      this.query = ''
      this.channelId = null
      this.userFilter = null
      this.dateFrom = null
      this.dateTo = null
    },

    clearResults() {
      this.results = []
    },

    async search(requestUserId: number) {
      this.loading = true
      this.error = null

      try {
        const results = await SearchClient.search({
          keyword: this.query && this.query.trim() !== '' ? this.query : undefined,
          channelId: this.channelId,
          userId: this.userFilter,
          from: this.dateFrom && this.dateFrom.trim() !== '' ? this.dateFrom : undefined,
          to: this.dateTo && this.dateTo.trim() !== '' ? this.dateTo : undefined,
          requestUserId: requestUserId
        })

        this.results = results.map((r: any) => ({
          id: r.id,
          channelId: r.channelId,
          userId: r.userId,
          content: r.content,
          createdAt: r.createdAt
        }))
      } catch (err: any) {
        console.error('検索処理エラー:', err)
        this.error = '検索処理中にエラーが発生しました。'
        this.results = []
      } finally {
        this.loading = false
      }
    }
  }
})