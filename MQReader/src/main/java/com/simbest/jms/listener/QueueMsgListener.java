package com.simbest.jms.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author lishuyi
 */
@Component
public class QueueMsgListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueMsgListener.class);

    /**
     * Method that read the Queue when exists messages.
     * This method is a listener
     *
     * @param msg - String message
     */
    public void onMessage(String msg) {
        LOGGER.debug("Receive queue message: " + msg);
    }

}
