/**
 * <p>需求：扩展新维度的完整步骤</p>
 *
 * <p>假设未来要加「手机号」维度：</p>
 * <ul>
 *    <li>RiskDimension 加常量 PHONE。</li>
 *    <li>RiskSubject 加工厂方法 phone(String)（可选）。</li>
 *    <li>application.yaml 增加 iam.risk.policies.phone 配置。</li>
 * </ul>
 * <p>完成。 无需改仓储、Key 工厂、聚合根、应用服务。</p>
 * @author loriyuhv
 * @version 1.0 2026/9/21
 * @since 1.0
 */
package com.wsw.fitnesssystem.iam.risk;
