package com.wsw.fitnesssystem.shared.interfaces.web.pagination;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wsw.fitnesssystem.shared.domain.pagination.PageQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * Web 边界分页请求基类。
 *
 * <p><b>职责：</b>为接口层 Request DTO 提供统一的分页字段、默认值、
 * Jakarta Validation 注解与 JavaBean 访问器。</p>
 *
 * <p><b>归属 interfaces 层的原因：</b></p>
 * <ul>
 *   <li>携带 {@code @Min} / {@code @Max} —— 属 Web 校验关注点；</li>
 *   <li>JavaBean 风格（getter/setter）—— 供 Spring MVC 绑定；</li>
 *   <li>字段默认值（{@code pageNum = 1}）—— 用户未传参时的兜底；</li>
 *   <li>这些均属传输层语义，不应泄漏到应用层。</li>
 * </ul>
 *
 * <p><b>与 {@link PageQuery} 的区别：</b></p>
 * <ul>
 *   <li>{@link PageQuery} 是<b>领域契约</b>，无注解无默认值；</li>
 *   <li>本类实现 {@link PageQuery}，使 Request 在翻译时天然满足领域契约；</li>
 *   <li>应用层 DTO 与领域 Query 均<b>不继承</b>本类，它们各自独立。</li>
 * </ul>
 *
 * <p><b>使用方式：</b></p>
 * <pre>{@code
 * public class UserListRequest extends PageRequest {
 *     private Integer userType;
 *     private String keyword;
 *     // 只写业务字段的 getter / setter
 * }
 * }</pre>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/19 08:34
 * @since 1.0
 */
@Setter
@Getter
public abstract class PageRequest implements PageQuery {

    /**
     * 单页最大条数，防止 {@code pageSize} 过大拖垮数据库。
     */
    private static final int MAX_PAGE_SIZE = 200;

    /** 默认页码 */
    private static final int DEFAULT_PAGE_NUM = 1;

    /** 默认每页条数 */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 页码（从 1 开始）。
     * <p>初始值 {@code 1}，供 Spring MVC 绑定时用户未传参的场景。</p>
     */
    @JsonProperty("page_num")
    @Min(value = 1, message = "页码必须大于等于 1")
    private int pageNum = DEFAULT_PAGE_NUM;

    /**
     * 每页条数。
     * <p>初始值 {@code 10}，{@code @Max} 与 setter 兜底共同保证不超过 {@link #MAX_PAGE_SIZE}。</p>
     */
    @JsonProperty("page_size")
    @Min(value = 1, message = "每页条数必须大于等于 1")
    @Max(value = MAX_PAGE_SIZE, message = "每页条数不能超过 " + MAX_PAGE_SIZE)
    private int pageSize = DEFAULT_PAGE_SIZE;

    // ==================== PageQuery 契约实现 ====================
    // record 会自动生成 pageNum() / pageSize()，但普通类需要手写

    @Override
    public int pageNum() {
        return pageNum;
    }

    @Override
    public int pageSize() {
        return pageSize;
    }

}
