# 权限与安全

## 1. 权限模型说明

系统采用经典 RBAC（Role-Based Access Control）模型：

- 用户（User）：系统登录主体
- 角色（Role）：权限集合的业务抽象
- 权限（Permission）：系统最小操作单元

## 2. 模型关系

User ——< UserRole >—— Role ——< RolePermission >—— Permission

## 3. 设计原则

- 权限不直接授予用户
- 权限粒度以“接口 / 业务动作”为单位
- 角色可禁用但不物理删除
- 管理员角色拥有兜底权限
