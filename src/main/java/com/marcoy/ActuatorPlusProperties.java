package com.marcoy;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("actuator-plus")
public class ActuatorPlusProperties {
    public boolean enableJvmMetrics       = true;
    public boolean enableHealthCheck      = false;
    public boolean enableThreadDump       = false;
    public boolean enableDeadlockDetector = false;
    public String jmxDomain      = "actuator-plus";
    public String threadDumpUrl  = "/thread-dump";
    public String healthCheckUrl = "/health-check";
}
