import type { App } from 'vue'
import { setupStore } from '@/store'
import { setupRouter } from '@/router'
import { setupElIcons } from './icons'
import { setupElementPlus } from './element'

export default {
  install(app: App<Element>) {
    // 路由(router)
    setupRouter(app)
    // 状态管理(store)
    setupStore(app)
    // Element-plus图标
    setupElIcons(app)
    // ElementPlus（含中文）
    setupElementPlus(app)
  },
}
