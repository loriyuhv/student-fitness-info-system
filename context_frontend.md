# 前端上下文（精简核心版）

> 本文件仅用于约束 AI 行为，避免路径错误和代码风格偏差。

## 0. 物理路径映射（最重要）
- **工作区根目录**：`/home/loriyuhv/codes/github/student-fitness-info-system`
- **前端根目录（相对路径）**：`student-fitness-system-frontend`
- **AI 操作规则**：读写前端文件时，路径必须以 `student-fitness-system-frontend/` 开头。
  - 例：`student-fitness-system-frontend/src/views/Login.vue`

## 1. 开发环境关键配置（防止写错地址）
- **前端端口**：`5000`（`npm run dev` 打开的地址）
- **后端代理**：Vite 已配置 Proxy，`/api` 自动转发到 `http://localhost:8080`。
- **编码约定**：代码中请求后端**直接写 `/api/xxx`**，严禁硬编码 `localhost:8080`。

## 2. 自动导入机制（代码风格硬约束）
项目已配置 `unplugin-auto-import` 和 `unplugin-vue-components`。

- **禁止**在代码中手动 import 以下内容（AI 生成时直接使用即可）：
  - Vue API：`ref`, `reactive`, `computed`, `watch`, `onMounted` 等
  - 路由：`useRoute`, `useRouter`
  - 状态管理：`useStore`, `storeToRefs`
  - 工具库：`@vueuse/core` 所有函数（如 `useLocalStorage`）
  - UI 组件：所有 Element Plus 组件（如 `<ElButton>`）
- **违例示例**：`import { ref } from 'vue'`（这是多余的，AI 不应生成）

## 3. 目录速查（快速定位）
- `src/views/`：页面组件（路由级别）。
- `src/components/`：公共组件（布局、通用）。
- `src/api/`：接口请求函数（按模块分文件）。
- `src/store/`：Pinia 状态管理（Token 存于此）。
- `src/utils/request.ts`：Axios 实例（已配置请求/响应拦截器）。

## 4. 对接后端约定（关键数据结构）
- **响应格式**：后端统一返回 `{ httpCode, bizCode, message, data, timestamp }`。
- **登录流程**：登录成功后，将 `data.token` 存入 Pinia，后续请求在 Header 中携带 `Authorization: Bearer ${token}`。
- **错误处理**：Axios 响应拦截器需统一处理 `bizCode` 非 200 的情况（如 401 跳转登录页）。