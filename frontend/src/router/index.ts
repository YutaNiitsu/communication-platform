import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from "../stores/useAuthStore.ts"

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: "/login",
    name: "login",
    component: () => import("../views/LoginView.vue")
  },
  {
    // ----------------------------------------------------
    // 共通レイアウト（AppLayout）を適用するグループ
    // ----------------------------------------------------
    path: "/",
    component: () => import("../layouts/AppLayout.vue"),
    meta: { requiresAuth: true },
    children: [
      {
        path: "home",
        name: "home",
        component: () => import("../views/HomeView.vue"),
        children: [
          {
            path: ":id",
            name: "channel-detail-in-home",
            component: () => import("../views/ChannelDetailView.vue")
          },
          {
            path: "channels/create",
            name: "channel-create-in-home",
            component: () => import("../components/ChannelCreateModal.vue")
          }
        ]
      },
      {
        path: "templates",
        name: "templates",
        component: () => import("../views/TemplateListView.vue")
      },
      {
        path: "search",
        name: "search",
        component: () => import("../views/SearchView.vue")
      },
      {
        path: "users",
        name: "UserList",
        component: () => import("../views/UserListView.vue"),
        meta: {
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: "users/create",
        name: "user-create",
        component: () => import("@/views/UserRegisterView.vue"),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: "users/:id/edit",
        name: "user-edit",
        component: () => import("@/views/UserRegisterView.vue"),
        meta: { requiresAuth: true, requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (to.path === "/login") return true

  try {
    if (!authStore.isLoggedIn) {
      if (typeof authStore.restoreSession === 'function') {
        await authStore.restoreSession()
      }
    }
  } catch (e) {
    console.warn("セッション復元に失敗しました:", e)
  }

  if (!authStore.isLoggedIn) {
    return "/login"
  }

  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)
  if (requiresAdmin && !authStore.isAdmin) {
    return "/home"
  }

  return true
})

export default router