import { createApp } from 'vue'

import App from './App.vue'
// Element Plus 采用按需引入：组件/API 样式由 unplugin 自动注入，无需全量 CSS
import '@/assets/styles/main.scss'
import setupPlugins from '@/plugins'

const app = createApp(App)

app.use(setupPlugins)

app.mount('#app')
