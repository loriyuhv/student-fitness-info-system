package com.wsw.fitnesssystem.shared.domain.pagination;

/**
 * 分页查询契约（领域层）。
 *
 * <p><b>职责：</b>为领域层分页查询对象提供统一契约，只表达"页码 / 每页条数"
 * 及派生的分页计算，不携带任何传输层语义。</p>
 *
 * <p><b>为什么是接口而不是抽象类：</b>领域查询对象通常用 {@code record} 实现，
 * 而 {@code record} 只能实现接口，不能继承类。用接口可以让 record 直接
 * {@code implements}，保持不可变性与简洁性。</p>
 *
 * <p><b>与 {@code PageRequest} 的区别：</b></p>
 * <ul>
 *   <li>本接口是<b>领域契约</b>，只声明"参数长什么样"，无默认值、无校验注解；</li>
 *   <li>{@code shared.interfaces.web.pagination.PageRequest} 是<b>接口层基类</b>，
 *       承载 Web 边界关注点：默认值、上限保护、Jakarta Validation 注解；</li>
 *   <li>接口层 Request 继承 {@code PageRequest}，天然满足本接口；</li>
 *   <li>应用层 DTO 用 record 平铺分页字段，不继承本接口；</li>
 *   <li>领域 Query 直接 {@code implements PageQuery} 并做构造期校验。</li>
 * </ul>
 *
 * <p><b>契约不变量：</b></p>
 * <ul>
 *   <li>{@code pageNum >= 1}</li>
 *   <li>{@code pageSize >= 1}</li>
 * </ul>
 * <p>实现类须在构造期校验，保证 {@code PageQuery} 实例永远合法。</p>
 *
 * <p><b>设计说明：</b>本接口<b>不提供</b> {@code offset()} / {@code limit()} 等派生方法。
 * 使用 MyBatis-Plus 分页插件时，框架自动计算分页参数；如未来出现手写分页 SQL 的场景，
 * 再按需扩展，遵循 YAGNI 原则。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/19 08:29
 * @since 1.0
 */
public interface PageQuery {

    /**
     * 页码（从 1 开始）。
     * <p>命名与 {@code record} 的访问器一致，避免子类手写 {@code getXxx()} 样板代码。</p>
     *
     * @return 当前页码，恒 &ge; 1
     */
    int pageNum();

    /**
     * 每页条数。
     *
     * @return 每页条数，恒 &ge; 1
     */
    int pageSize();

}
