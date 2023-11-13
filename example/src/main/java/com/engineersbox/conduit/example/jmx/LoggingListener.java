package com.engineersbox.conduit.example.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Notification;
import javax.management.NotificationListener;

public class LoggingListener implements NotificationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingListener.class);

    @Override
    public  void handleNotification(final Notification notification,
                                    final Object obj) {
        LOGGER.info("[Notification: {}]", notification.toString());
    }

}
