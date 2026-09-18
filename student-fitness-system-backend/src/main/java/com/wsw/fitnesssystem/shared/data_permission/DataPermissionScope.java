package com.wsw.fitnesssystem.shared.data_permission;

import com.wsw.fitnesssystem.shared.data_permission.domain.DataScope;

import java.lang.annotation.*;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/18 14:52
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DataPermissionScope {

    DataScope value();

}
