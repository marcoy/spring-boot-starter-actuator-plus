package com.marcoy;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadDeadlockDetector;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.ThreadDumpServlet;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.assertj.core.util.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.MetricsDropwizardAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Optional;

@Configuration
@EnableMetrics
@ComponentScan
@AutoConfigureBefore(MetricsDropwizardAutoConfiguration.class)
@EnableConfigurationProperties(ActuatorPlusProperties.class)
public class ActuatorPlusConfiguration extends MetricsConfigurerAdapter {

    /**
     * The property that contains the prefix for the management endpoints.
     *
     * For example, if it is set to {@code /admin}, the actuator endpoints will have a {@code /admin} prefix.
     * (e.g. /admin/mappings)
     */
    private static final String MGMT_CTX_PATH = "management.context-path";

    @Autowired
    private ActuatorPlusProperties actuatorPlusProperties;

    @Autowired
    private Environment environment;

    @Bean(name = "threadDumpServlet")
    @ConditionalOnProperty(prefix = "actuator-plus", name = "enable-thread-dump", havingValue = "true")
    public ServletRegistrationBean threadDumpServlet() {
        final String url = deriveEndpoint(environment, actuatorPlusProperties.getThreadDumpUrl());
        return new ServletRegistrationBean(new ThreadDumpServlet(), url);
    }

    @Bean(name = "healthCheckServlet")
    @ConditionalOnProperty(prefix = "actuator-plus", name = "enable-health-check", havingValue = "true")
    public ServletRegistrationBean healtCheckServlet(final HealthCheckRegistry healthCheckRegistry) {
        final String url = deriveEndpoint(environment, actuatorPlusProperties.getHealthCheckUrl());
        healthCheckRegistry.register("deadlock-check"
                , new ThreadDeadlockHealthCheck(new ThreadDeadlockDetector()));
        return new ServletRegistrationBean(new HealthCheckServlet(healthCheckRegistry), url);
    }

    @Override
    public void configureReporters(MetricRegistry metricRegistry) {

        if (actuatorPlusProperties.isEnableJvmMetrics()) {
            metricRegistry.registerAll(new GarbageCollectorMetricSet());
            metricRegistry.registerAll(new MemoryUsageGaugeSet());
        }

        registerReporter(JmxReporter.forRegistry(metricRegistry)
                .inDomain(actuatorPlusProperties.getJmxDomain())
                .build()).start();
    }

    @VisibleForTesting
    static String deriveEndpoint(final Environment env, final String url) {
        return Optional.ofNullable(env.getProperty(MGMT_CTX_PATH))
                       .map(prefix -> prefix + url)
                       .orElse(url);
    }
}
