package com.simbest.jms.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author lishuyi
 */
@Component
public class TopicMsgListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicMsgListener.class);

    /**
     * Method that read the Topic when exists messages.
     * This method is a listener
     *
     * @param msg - String message
     */
    public void onMessage(String msg) {
        LOGGER.debug("Receive topic message: " + msg);
    }

}
