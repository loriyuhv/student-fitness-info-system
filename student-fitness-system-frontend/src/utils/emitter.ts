import mitt, { type Emitter } from 'mitt'
import type { UserInfo } from '@/types'

export type Events = {
  'send-student-info': UserInfo
}

const emitter: Emitter<Events> = mitt<Events>()

export default emitter
