// stores/user-store.ts
// 职责：用户信息、权限和角色
import { type UserInfo } from '@/types'

export const useUserStore = defineStore(
  'user',
  () => {
  // state
  const userInfo = ref<UserInfo | null>(null)

  // actions
  const setUserInfo = (data: UserInfo) => {
    userInfo.value = data
  }

  const clearUserInfo = () => {
    userInfo.value = null
  }

  const hasUserInfo = computed(() => {
    return !!userInfo.value?.userId
  })

  return { userInfo, setUserInfo, clearUserInfo, hasUserInfo }
})
