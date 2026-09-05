-- 查看用户
SELECT * FROM sys_user WHERE username = 'admin' AND status = 1 AND deleted = 0;

-- 根据用户名称查看用户角色和权限编码
SELECT u.user_id, r.role_code, p.perm_code FROM sys_user u
                 JOIN sys_user_role ur
                 ON u.user_id = ur.user_id AND ur.status = 1 AND ur.deleted = 0
                             JOIN sys_role r
                             ON ur.role_id = r.role_id AND r.status = 1 AND r.deleted=0
                                          JOIN sys_role_permission rp
                                          ON r.role_id = rp.role_id AND rp.status = 1 AND rp.deleted = 0
                                                      JOIN sys_permission p
                                                      ON rp.perm_id = p.perm_id AND p.status = 1 AND p.deleted = 0
WHERE u.username = '412251401' AND u.campus_id = 1101 AND u.status = 1 AND u.deleted = 0;

