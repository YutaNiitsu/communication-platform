import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './index.css'
import { useAuthStore } from '@/stores/useAuthStore.ts'
import { useConfigStore } from "@/stores/useConfigStore"

import '@/assets/styles/foundation/reset.css'
import '@/assets/styles/foundation/base.css'
import '@/assets/styles/foundation/typography.css'

import '@/assets/styles/layout/container.css'
import '@/assets/styles/layout/grid.css'
import '@/assets/styles/layout/header.css'

import '@/assets/styles/object/component/button.css'
import '@/assets/styles/object/component/card.css'
import '@/assets/styles/object/component/modal.css'
import '@/assets/styles/object/component/form.css'

import '@/assets/styles/object/utility/margin.css'
import '@/assets/styles/object/utility/padding.css'
import '@/assets/styles/object/utility/text.css'
import '@/assets/styles/object/utility/display.css'


import "@/assets/styles/object/project/channel-detail-modal.css"
import "@/assets/styles/object/project/channel-detail-message.css"
import "@/assets/styles/object/project/channel-detail-layout.css"
import "@/assets/styles/object/project/channel-detail-form.css"
import "@/assets/styles/object/project/channel-detail-components.css"
import "@/assets/styles/object/project/channel-list.css"
import "@/assets/styles/object/project/login.css"
import "@/assets/styles/object/project/search.css"
import "@/assets/styles/object/project/home.css"
import "@/assets/styles/object/project/user-list.css"
import "@/assets/styles/object/project/user-register.css"
import "@/assets/styles/object/project/template-list-view.css"
import "@/assets/styles/object/project/category-management-modal.css"
import "@/assets/styles/object/project/channel-create-modal.css"
import "@/assets/styles/object/project/channel-info-modal.css"
import "@/assets/styles/object/project/template-form-modal.css"


const app = createApp(App)

app.use(createPinia())
app.use(router)

// Piniaが初期化された後にauthStoreを呼び出す
const authStore = useAuthStore()
authStore.restoreSession() // セッション復元を authStore 経由で行う

const configStore = useConfigStore()

await configStore.loadConfig()

app.mount('#app')