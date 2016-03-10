package com.marcoy;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ActuatorPlusConfiguration.class)
public class ActuatorPlusConfigurationTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private MetricRegistry metricRegistry;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void contextLoads() {

    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testHealthCheckServlet() {
        applicationContext.getBean("healthCheckServlet");
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testThreadDumpServlet() {
        applicationContext.getBean("threadDumpServlet");
    }

    @Test
    public void testMetricRegistryConfiguration() {
        // Some JVM metrics
        assertTrue(metricRegistry.getMetrics().containsKey("heap.init"));
        assertTrue(metricRegistry.getMetrics().containsKey("pools.Code-Cache.committed"));
    }
}
