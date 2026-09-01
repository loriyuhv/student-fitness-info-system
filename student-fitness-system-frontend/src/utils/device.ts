// src/utils/device.ts

/**
 * 获取设备类型
 * @returns 'desktop' | 'tablet' | 'mobile'
 */
export function getDeviceType(): string {
  const ua = navigator.userAgent;
  if (/iPad|Tablet|PlayBook|Silk/i.test(ua)) return 'tablet';
  if (/Mobile|Android|iPhone|iPod|Windows Phone/i.test(ua)) return 'mobile';
  return 'desktop';
}

/**
 * 获取设备唯一标识（首次生成后保存在localStorage，永久有效）
 * @returns 设备ID字符串
 */
export function getDeviceId(): string {
  let deviceId = localStorage.getItem('device_id');
  if (!deviceId) {
    // 生成简单的UUID v4（不依赖第三方库）
    deviceId = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
      const r = (Math.random() * 16) | 0;
      const v = c === 'x' ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    });
    localStorage.setItem('device_id', deviceId);
  }
  return deviceId;
}

// export default { getDeviceType, getDeviceId };
