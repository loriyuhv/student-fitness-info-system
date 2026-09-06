# 数据库脚本管理

- 基于Flyway完成目录结构与命名规范
- 按模块划分

## 一、脚本目录文件树图

```bash
src/main/resources/db/migration/
├── common/                          			# 通用基础（必填）
│   ├── V1.0.00__create_db_schema.sql			# 创建数据库
│   ├── V1.0.01__init_auth_schema.sql   	# 只放 CREATE TABLE (sys_user, sys_role...)
│   └── V1.0.02__init_auth_data.sql     	# 只放 INSERT (admin, 角色权限映射)
│
├── user_org/                        			# 用户组织模块（依赖 auth）
│   ├── V1.0.03__init_user_schema.sql   	# user_profile, class_info, teacher...
│   └── V1.0.04__init_user_data.sql     	# 初始化班级、测试教师学生（可选）
│
├── handle_excel/                        	# Excel导入导出模块
│   ├── V1.0.07__init_excel_template_config_schema.sql   	# 导入Excel模板
│   └── V1.0.08_init_excel_template_config_data.sql     	# 初始化模板
│
└── fitness/                         			# 体测业务模块
    ├── V1.0.05__init_fitness_schema.sql
    └── V1.0.06__init_fitness_data.sql
    
    
```

**关键点**：

- **版本号排序**：利用 Flyway 的版本号（`V1.0.0` > `V1.0.1`）强制保证 **Auth 表必须在 User 表之前创建**（因为 `student_profile` 依赖 `sys_user.user_id`）。
- **DDL 与 DML 分离**：`schema` 只负责建表/改表，`data` 只负责插入初始数据。好处是后续版本（如 `V1.1.0`）如果新增字段，只需新增 `ALTER TABLE` 的 Schema 文件，不再重复插入已有数据，避免主键冲突。