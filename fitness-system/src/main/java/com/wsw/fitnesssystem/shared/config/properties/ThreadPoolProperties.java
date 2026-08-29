package com.wsw.fitnesssystem.shared.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/29 11:18
 * @since 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolProperties {

    private Business business = new Business();
    private Compute compute = new Compute();
    private System system = new System();

    @Data
    public static class Business {
        private String ThreadPoolName = "businessExecutor";
        private String threadNamePrefix = "business-";
        private int corePoolSize = 8;
        private int maxPoolSize = 12;
        private int queueCapacity = 100;
        private int awaitTerminationSeconds = 60;
    }

    @Data
    public static class Compute {
        private String ThreadPoolName = "computeExecutor";
        private String threadNamePrefix = "compute-";
        private int corePoolSize;
        private int maxPoolSize;
        private int queueCapacity = 50;
        private int awaitTerminationSeconds = 120;
        // 计算因子
        private double cpuFactor = 0.25;
        private int extraCores = 2;
    }

    @Data
    public static class System {
        private String ThreadPoolName = "systemExecutor";
        private String threadNamePrefix = "system-";
        private int corePoolSize = 2;
        private int maxPoolSize = 4;
        private int queueCapacity = 500;
        private int awaitTerminationSeconds = 30;
    }

}
