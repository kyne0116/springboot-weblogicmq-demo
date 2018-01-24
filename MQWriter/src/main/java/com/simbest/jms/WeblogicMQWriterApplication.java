package com.simbest.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import java.util.Properties;

/**
 * @author lishuyi
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.simbest")
@EnableAutoConfiguration(exclude = JmxAutoConfiguration.class)
public class WeblogicMQWriterApplication {

    @Autowired
    private ApplicationContext appContext;

    @Value("${java.naming.provider.url}")
    private String broker;

    @Value("${jms.topic.name}")
    private String topicName;

    @Value("${jms.queue.name}")
    private String queueName;

    @Value("${jms.factory.name}")
    private String connectionFactoryName;

    private static final Logger LOGGER = LoggerFactory.getLogger(WeblogicMQWriterApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WeblogicMQWriterApplication.class);
    }

    private Properties getJNDiProperties() {
        final Properties jndiProps = new Properties();
        LOGGER.debug("Initializing JndiTemplate");
        jndiProps.setProperty(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        jndiProps.setProperty(Context.PROVIDER_URL, broker);
        return jndiProps;
    }

    @Bean
    public JndiTemplate jndiTemplate() {
        final JndiTemplate jndiTemplate = new JndiTemplate();
        jndiTemplate.setEnvironment(getJNDiProperties());
        return jndiTemplate;
    }

    @Bean(name = "jndiObjectFactoryBean")
    public JndiObjectFactoryBean jndiObjectFactoryBean(final JndiTemplate jndiTemplate) {
        final JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        LOGGER.debug("Creating Weblogic JMS connection factory");
        jndiObjectFactoryBean.setJndiTemplate(jndiTemplate);
        LOGGER.debug("ConnectoinFactory Name is {}", connectionFactoryName);
        jndiObjectFactoryBean.setJndiName(connectionFactoryName);
        return jndiObjectFactoryBean;
    }

    @Bean(name = "wlsConnectionFactory")
    public ConnectionFactory jmsConnectionFactory(final JndiObjectFactoryBean jndiObjectFactoryBean) {
        final ConnectionFactory connectionFactory = (ConnectionFactory) jndiObjectFactoryBean.getObject();
        LOGGER.debug("ConnectoinFactory is null? {}", connectionFactory == null);
        return connectionFactory;
    }

    @Bean(name = "cachingConnectionFactory")
    @Primary
    public ConnectionFactory cachingConnectionFactory() {
        final CachingConnectionFactory jmsConnectionFactory = new CachingConnectionFactory(
                (ConnectionFactory) appContext.getBean("wlsConnectionFactory"));
        jmsConnectionFactory.setCacheProducers(true);
        jmsConnectionFactory.setSessionCacheSize(20);
        return jmsConnectionFactory;
    }

    /**
     * Create DestinationResolver to resolve QueueName
     *
     * @return Instance of JNDI Destination Resolver
     */
    private DestinationResolver destinationResolver() {
        final JndiDestinationResolver destinationResolver = new JndiDestinationResolver();
        destinationResolver.setJndiTemplate(jndiTemplate());
        return destinationResolver;
    }

    @Bean(name = "jmsTemplateTopic")
    public JmsTemplate jmsTemplateTopic() {
        LOGGER.debug("<<<<<< Loading jmsTemplateTopic");
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(cachingConnectionFactory());
        template.setDefaultDestinationName(topicName);
        template.setDestinationResolver(destinationResolver());
        template.setSessionAcknowledgeModeName("AUTO_ACKNOWLEDGE");
        template.setSessionTransacted(false);
        template.setPubSubDomain(true);
        LOGGER.debug("jmsTemplateTopic loaded >>>>>>>");
        return template;
    }

    @Bean(name = "jmsTemplateQueue")
    public JmsTemplate jmsTemplateQueue() {
        LOGGER.debug("<<<<<< Loading jmsTemplateQueue");
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(cachingConnectionFactory());
        template.setDefaultDestinationName(queueName);
        template.setDestinationResolver(destinationResolver());
        template.setSessionAcknowledgeModeName("AUTO_ACKNOWLEDGE");
        template.setSessionTransacted(false);
        template.setPubSubDomain(false);
        LOGGER.debug("jmsTemplateTopic loaded >>>>>>>");
        return template;
    }

    @Bean
    public ThreadPoolTaskExecutor executor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(5);
        ex.setMaxPoolSize(15);
        return ex;
    }
}