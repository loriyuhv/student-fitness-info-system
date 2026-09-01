// stores/token-store.ts
// 职责：token
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTokenStore = defineStore(
  'token',
  () => {
    // state
    const accessToken = ref('')
    const refreshToken = ref('')

    // actions
    const setTokens = (access: string, refresh: string) => {
      accessToken.value = access
      refreshToken.value = refresh
    }

    const clearTokens = () => {
      accessToken.value = ''
      refreshToken.value = ''
    }

    return {
      accessToken,
      refreshToken,
      setTokens,
      clearTokens,
    }
  },
  {
    persist: true, // 持久化配置
  },
)
