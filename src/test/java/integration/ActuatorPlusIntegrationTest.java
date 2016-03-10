package integration;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@TestPropertySource(properties = { "actuator-plus.enable-health-check=true"
                                 , "actuator-plus.health-check-url=/integration-health-check"
                                 , "actuator-plus.jmx-domain=integration.tests" })
public class ActuatorPlusIntegrationTest implements ApplicationContextAware {

    private static final String HEALTH_CHECK_URL = "/integration-health-check";

    private ApplicationContext applicationContext;

    @Autowired
    private MetricRegistry metricRegistry;

    @Test
    public void contextLoads() {

    }

    @Test
    public void testMetricRegistry() {
        // Some Jvm metrics
        final Map<String, Metric> metrics = metricRegistry.getMetrics();

        assertThat(metrics).containsKeys( "heap.committed"
                                        , "pools.Code-Cache.committed"
                                        , "heap.used");
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testThreadDumpServlet() {
        applicationContext.getBean("threadDumpServlet", ServletRegistrationBean.class);
    }

    @Test
    public void testHealthCheckServlet() {
        final ServletRegistrationBean bean =
                applicationContext.getBean("healthCheckServlet", ServletRegistrationBean.class);

        assertThat(bean).isNotNull();
        assertThat(bean.getUrlMappings()).isEqualTo(Collections.singleton(HEALTH_CHECK_URL));
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
