package com.marcoy;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ActuatorPlusConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ActuatorPlusConfigurationPropsTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * The value should match the value of {@code actuator-plus.thread-dump-url} in application-test.properties.
     */
    private static final String TEST_PROP_THREAD_DUMP_URL = "/test/thread-dump";

    /**
     * The value should match the value of {@code actuator-plus.health-check-url} in application-test.properties.
     */
    private static final String TEST_PROP_HEALTH_CHECK_URL = "/test/health-check";

    @Autowired
    private MetricRegistry metricRegistry;

    @Autowired
    Environment environment;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void contextLoads() {

    }

    @Test
    public void testEmptyMetricRegistry() {
        assertThat(metricRegistry.getMetrics()).isEmpty();
    }

    @Test
    public void testHealthCheckServlet() {
        final ServletRegistrationBean bean = applicationContext.getBean("healthCheckServlet",
                                                                        ServletRegistrationBean.class);
        final String url = ActuatorPlusConfiguration.deriveEndpoint(environment, TEST_PROP_HEALTH_CHECK_URL);

        assertThat(bean).isNotNull();
        assertThat(bean.getUrlMappings()).isEqualTo(Collections.singleton(url));
    }

    @Test
    public void testThreadDumpServlet() {
        final ServletRegistrationBean bean = applicationContext.getBean("threadDumpServlet",
                                                                        ServletRegistrationBean.class);
        final String url = ActuatorPlusConfiguration.deriveEndpoint(environment, TEST_PROP_THREAD_DUMP_URL);

        assertThat(bean).isNotNull();
        assertThat(bean.getUrlMappings()).isEqualTo(Collections.singleton(url));
    }

    @Test
    public void testDeadlockDetector() {
        final HealthIndicator deadlockDetector = applicationContext.getBean("deadlockDetector", HealthIndicator.class);
        assertThat(deadlockDetector).isNotNull();
        assertThat(deadlockDetector.health().getStatus()).isEqualTo(Status.UP);
    }
}