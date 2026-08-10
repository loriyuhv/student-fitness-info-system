import type { App } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

/** Element-Plus 插件（含中文） */
export function setupElementPlus(app: App) {
  app.use(ElementPlus, { locale: zhCn, size: 'default' })
}
