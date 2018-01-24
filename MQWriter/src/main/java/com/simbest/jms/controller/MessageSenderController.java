package com.simbest.jms.controller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author lishuyi
 */
@RestController
public class MessageSenderController {


    @Autowired
    @Qualifier("jmsTemplateTopic")
    private JmsTemplate jmsTemplateTopic;

    @Autowired
    @Qualifier("jmsTemplateQueue")
    private JmsTemplate jmsTemplateQueue;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSenderController.class);

    @RequestMapping(value = "/sendSampleTopic", method = RequestMethod.GET)
    public String sendSampleTopic() {
        try {
            jmsTemplateTopic.send(session -> session.createTextMessage("Send Sample Topic Message!"));
            return "SAMPLE MESSAGE WAS SENT";
        } catch (JmsException e) {
            LOGGER.debug("Error: ", e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/sendTopic", method = RequestMethod.POST)
    public String sendTopic(@RequestBody String msg) {
        try {
            jmsTemplateTopic.send(session -> session.createTextMessage(msg));
            return "TOPIC MESSAGE WAS SENT";
        } catch (JmsException e) {
            LOGGER.debug("Error: ", e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/sendSampleQueue", method = RequestMethod.GET)
    public String sendSampleQueue() {
        try {
            jmsTemplateQueue.send(session -> session.createTextMessage("Send Sample Queue Message!"));
            return "SAMPLE MESSAGE WAS SENT";
        } catch (JmsException e) {
            LOGGER.debug("Error: ", e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/sendQueue", method = RequestMethod.POST)
    public String sendQueue(@RequestBody String msg) {
        try {
            jmsTemplateQueue.send(session -> session.createTextMessage(msg));
            return "QUEUE MESSAGE WAS SENT";
        } catch (JmsException e) {
            LOGGER.debug("Error: ", e);
            return e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/sendFromFile", method = RequestMethod.GET)
    public String sendFromFile() {
        try {
            InputStream iStr = getClass().getClassLoader().getResourceAsStream("messages.txt");
            List<String> lsRes = IOUtils.readLines(iStr);
            executor.execute(createThread(lsRes));
            return "PROCESS LAUNCHED";

        } catch (IOException e) {

            LOGGER.debug("Error: ", e);
            return e.getMessage();
        }
    }

    private Thread createThread(List<String> ls) {
        Runnable hilo = new Runnable() {
            @Override
            public void run() {

                for (String s : ls) {
                    jmsTemplateQueue.send(session -> session.createTextMessage(s));
                }

            }
        };

        Thread tarea = new Thread(hilo);
        return tarea;
    }

}
