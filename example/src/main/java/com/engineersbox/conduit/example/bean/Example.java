package com.engineersbox.conduit.example.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;

public class Example implements ExampleMBean, NotificationBroadcaster {

    private static final Logger LOGGER = LoggerFactory.getLogger(Example.class);

    private final long count;
    private final double value;

    public Example() {
        this(
                123,
                56783.6742
        );
    }

    public Example(final long count, final double value) {
        this.count = count;
        this.value = value;
    }

    @Override
    public long getCount() {
        return this.count;
    }

    @Override
    public double totalValue() {
        return this.value;
    }

    @Override
    public void logState() {
        LOGGER.info("State: [Count={}] [Value={}]", this.count, this.value);
    }

    @Override
    public void addNotificationListener(final NotificationListener listener,
                                        final NotificationFilter filter,
                                        final Object handback) throws IllegalArgumentException {
        listener.handleNotification(
                new Notification(
                        "log",
                        this,
                        0L,
                        String.format(
                                "Count=%d,Value=%f",
                                this.count,
                                this.value
                        )
                ),
                handback
        );
    }

    @Override
    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {

    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[]{
                new MBeanNotificationInfo(
                        new String[]{"log"},
                        this.getClass().getName(),
                        "Logging Notification",
                        null
                )
        };
    }
}
