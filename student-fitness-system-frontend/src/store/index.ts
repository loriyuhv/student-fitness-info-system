import type { App } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedState from 'pinia-plugin-persistedstate'

const store = createPinia()

// 注册持久化插件
store.use(piniaPluginPersistedState)

// 全局注册store
export function setupStore(app: App<Element>) {
  app.use(store)
}

export * from './modules/user-store'
export * from './modules/token-store'
export { store }
