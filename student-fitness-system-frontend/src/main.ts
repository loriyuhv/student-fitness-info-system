import { createApp } from 'vue'

import App from './App.vue'
import 'element-plus/dist/index.css'
import '@/assets/styles/main.scss'
import setupPlugins from '@/plugins'

// 系统配置
// import systemConfig from '@/config/system'

const app = createApp(App)

app.use(setupPlugins)

app.mount('#app')
