package com.simbest.jms;


import com.simbest.jms.listener.QueueMsgListener;
import com.simbest.jms.listener.TopicMsgListener;
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
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import java.util.Properties;

/**
 * @author lishuyi
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.simbest")
@EnableAutoConfiguration(exclude = JmxAutoConfiguration.class)
public class WeblogicMQReaderApplication {

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

    @Value("${jms.clientId}")
    private String clientId;

    private static final Logger LOGGER = LoggerFactory.getLogger(WeblogicMQReaderApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WeblogicMQReaderApplication.class, args);
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

    @Bean(name = "jmsJndiConnectionFactory")
    public JndiObjectFactoryBean jndiObjectFactoryBean(final JndiTemplate jndiTemplate) {
        final JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        LOGGER.debug("Creating Weblogic JMS connection factory");
        jndiObjectFactoryBean.setJndiTemplate(jndiTemplate);
        LOGGER.debug("ConnectoinFactory Name is {}", connectionFactoryName);
        jndiObjectFactoryBean.setJndiName(connectionFactoryName);
        return jndiObjectFactoryBean;
    }

    @Bean(name = "jmsWlsConnectionFactory")
    public ConnectionFactory jmsConnectionFactory(final JndiObjectFactoryBean jndiObjectFactoryBean) {
        final ConnectionFactory connectionFactory = (ConnectionFactory) jndiObjectFactoryBean.getObject();
        LOGGER.debug("ConnectoinFactory is null? {}", connectionFactory == null);
        return connectionFactory;
    }

    @Bean(name = "jmsConnectionFactory")
    @Primary
    public ConnectionFactory cachingConnectionFactory() {
        final CachingConnectionFactory jmsConnectionFactory = new CachingConnectionFactory(
                (ConnectionFactory) appContext.getBean("jmsWlsConnectionFactory"));
        jmsConnectionFactory.setCacheProducers(true);
        jmsConnectionFactory.setSessionCacheSize(20);
        jmsConnectionFactory.setClientId(clientId);
        return jmsConnectionFactory;
    }

    /**
     * Message listener adapter configuration for topic reception.
     * MsgListenerTopic class implements in method onMessage
     *
     * @param topic - MsgListenerTopic
     * @return MessageListenerAdapter
     * @see TopicMsgListener
     * @see MessageListenerAdapter
     **/
    @Bean(name = "adapterTopic")
    public MessageListenerAdapter adapterTopic(TopicMsgListener topic) {
        MessageListenerAdapter listener = new MessageListenerAdapter(topic);
        listener.setDefaultListenerMethod("onMessage");
        listener.setMessageConverter(new SimpleMessageConverter());
        return listener;

    }

    /**
     * Message listener adapter configuration for queue reception.
     * MsgListenerQueue class implements in method onMessage
     *
     * @param queue - MsgListenerQueue
     * @return MessageListenerAdapter
     * @see QueueMsgListener
     * @see MessageListenerAdapter
     **/
    @Bean(name = "adapterQueue")
    public MessageListenerAdapter adapterQueue(QueueMsgListener queue) {
        MessageListenerAdapter listener = new MessageListenerAdapter(queue);
        listener.setDefaultListenerMethod("onMessage");
        listener.setMessageConverter(new SimpleMessageConverter());
        return listener;

    }

    private DestinationResolver destinationResolver() {
        final JndiDestinationResolver destinationResolver = new JndiDestinationResolver();
        destinationResolver.setJndiTemplate(jndiTemplate());
        return destinationResolver;
    }

    /**
     * Topic listener container.
     * This method configure a listener for a topic
     *
     * @param adapterTopic -  MessageListenerAdapter
     * @see MessageListenerAdapter
     * @see SimpleMessageListenerContainer
     **/
    @Bean(name = "jmsTopic")
    public SimpleMessageListenerContainer getTopic(MessageListenerAdapter adapterTopic) {
        LOGGER.debug("<<<<<< Loading Listener topic");
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        // settings for listener: connectonFactory,Topic name,MessageListener and PubSubDomain (true if is a topic)
        container.setConnectionFactory(cachingConnectionFactory());
        container.setDestinationName(topicName);
        container.setMessageListener(adapterTopic);
        container.setPubSubDomain(true);
        container.setDestinationResolver(destinationResolver());
        container.setSubscriptionDurable(true);
        container.setClientId(clientId);
        LOGGER.debug("Listener topic loaded >>>>>>>>>");
        return container;
    }

    /**
     * Queue listener container.
     * This method configure a listener for a queue
     *
     * @param adapterQueue -  MessageListenerAdapter
     * @see MessageListenerAdapter
     * @see SimpleMessageListenerContainer
     **/
    @Bean(name = "jmsQueue")
    public SimpleMessageListenerContainer getQueue(MessageListenerAdapter adapterQueue) {
        LOGGER.debug("<<<<<< Loading Listener Queue");
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        // settings for listener: connectonFactory,Topic name,MessageListener and PubSubDomain (false if is a queue)
        container.setConnectionFactory(cachingConnectionFactory());
        container.setDestinationName(queueName);
        container.setMessageListener(adapterQueue);
        container.setPubSubDomain(false);
        container.setDestinationResolver(destinationResolver());
        LOGGER.debug("Listener Queue loaded >>>>>>>");
        return container;
    }

}
