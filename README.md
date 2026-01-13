# 学生体测信息诊断系统

## 项目背景
学生体测信息诊断管理系统用于对学生体质健康测试数据进行统一采集、管理、分析与诊断，面向 **学生、教师、管理员**等角色，支撑学校体质健康管理的日常业务与数据决策。

本项目为**实际业务交付项目**，采用前后端分离架构，支持长期运行与后期扩展，项目文档与代码保持同步维护。

## 技术栈
- 后端：`Java17` + `Spring Boot3.5.8` + `Redis6.2.6` +`MyBatisPlus` + `MySQL8.0.6`
- 前端：`Vue3` + Element Plus +` Pinia`
- 安全：`Spring Security` +`JWT`
- 权限：`RBAC`

## 项目结构说明
```
student-fitness-system/
├── docs/
│   ├── 00-overview.md           # 项目总览
│   ├── 01-requirements.md       # 业务需求说明
│   ├── 02-architecture.md       # 系统架构设计
│   ├── 03-database.md           # 数据库设计
│   ├── 04-auth-rbac.md          # 权限与安全
│   ├── 05-api.md                # 接口说明
│   ├── 06-deploy.md             # 部署/运维
│   ├── 07-change-log.md         # 版本变更记录
│   └── 08-faq.md                # 常见问题
├── fitness-backend/             # 后端服务
├── fitness-frontend/            # 前端服务
├── sql/						 # 数据库脚本
│   └── init.sql
├── .gitignore
└── README.md
```

## 快速启动
1. 初始化数据库
2. 启动后端
3. 启动前端

## 版本与维护说明
-   main 分支保持可交付状态
-   重要交付节点使用 Git Tag
-   所有变更记录维护在 `docs/07-change-log.md`
