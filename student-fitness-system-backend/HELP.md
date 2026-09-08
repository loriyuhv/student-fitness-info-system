# 学生体测信息诊断系统后端

## 项目结构

> 采用【领域模块化包结构】

```
com.wsw.fitnesssystem
├── infrastructure/								# 技术基础设施（最底层）
│   ├── security/								# 安全模块
│   │	└── config/
│   │   	└── SecurityConfig.java				# SpringSecurity配置类
├── interfaces/									# 接口层（Controller）
│	└── system/									
│			└── health/
│					└── HealthController.java	# 存活接口 
└── FitnessSystemApplication.java 				# 启动入口
    
```

