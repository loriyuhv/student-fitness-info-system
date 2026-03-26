package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/14 12:54
 * @since 1.0
 */
@Mapper
public interface ExcelSysUserMapper extends BaseMapper<SysUser> {
    /** 批量插入（XML实现） */
    int batchInsert(@Param("list") List<SysUser> list);

    /** 查询已存在用户名 */
    List<String> selectExistingUsernames(@Param("usernames") List<String> usernames);

    /** 查询已存在手机号 */
    List<String> selectExistingPhones(@Param("phones") List<String> phones);
}
