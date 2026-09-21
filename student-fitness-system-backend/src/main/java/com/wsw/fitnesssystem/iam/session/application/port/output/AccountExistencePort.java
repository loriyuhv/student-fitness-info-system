package com.wsw.fitnesssystem.iam.session.application.port.output;

/**
 * 账号存在性校验端口（session 模块定义的出站端口）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>定义"判断指定用户是否存在"这一能力契约，供撤销会话场景校验目标用户合法性</li>
 *   <li>由 authentication 模块提供本地实现，避免 session 直接依赖 authentication 的领域模型</li>
 *   <li>不依赖任何具体实现（本地/远程均透明）</li>
 * </ul>
 *
 * <p><b>依赖反转方向：</b>
 * <pre>
 *   session（定义 Port）  ←  authentication（实现 Adapter）
 * </pre>
 * 依赖方向符合"业务模块可以依赖其他模块 application 层的 Port"原则。</p>
 *
 * <p><b>设计边界（YAGNI）：</b>
 * 本接口只声明当前用例需要的最小能力，不预置"可能用到"的方法。
 * 未来若出现新的账号相关用例（如查询账号基本信息、校验账号状态等），
 * 应在 session 内<b>新增独立的 Port</b>，而不是向本接口追加方法，
 * 避免接口膨胀、实现方被迫实现无关能力。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/21 15:39
 * @since 1.0
 */
public interface AccountExistencePort {

    /**
     * 判断指定用户在指定校区是否存在
     *
     * @param campusId 校区ID
     * @param userId   用户ID
     * @return true 表示账号存在
     */
    boolean exists(long campusId, long userId);

}
