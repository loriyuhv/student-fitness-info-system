import type { App } from 'vue'
import { setupStore } from '@/store'
import { setupRouter } from '@/router'

/**
 * 插件统一入口。
 * Element Plus 采用按需自动引入（unplugin-auto-import / unplugin-vue-components），
 * 因此不再在此全局 app.use(ElementPlus)；中文化由 App.vue 的 el-config-provider 提供。
 * Element Plus 图标同样改为按需显式 import，不再全局注册全部图标。
 */
export default {
  install(app: App) {
    // 状态管理(store)
    setupStore(app)
    // 路由(router)
    setupRouter(app)
  },
}
