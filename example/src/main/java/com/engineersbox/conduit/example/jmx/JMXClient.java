package com.engineersbox.conduit.example.jmx;

import com.engineersbox.conduit.example.bean.ExampleMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.Set;

public class JMXClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(JMXClient.class);

    public static void main(final String[] args) throws Exception {
        final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:5555/jmxrmi");
        final JMXConnector connector = JMXConnectorFactory.connect(url);
        final MBeanServerConnection mbsc = connector.getMBeanServerConnection();
        LOGGER.info("Connected to remote JMX server {}", url);
        final ObjectName objectName = new ObjectName("SimpleAgent:name=test");
        Set<ObjectInstance> instances = mbsc.queryMBeans(objectName, null);
        for (final ObjectInstance instance : instances) {
            LOGGER.info("Instance: {}", instance);
        }
        ExampleMBean bean = JMX.newMBeanProxy(
                mbsc,
                objectName,
                ExampleMBean.class,
                true
        );
        mbsc.invoke(
                objectName,
                "logState",
                new Object[0],
                new String[0]
        );
        LOGGER.info(
                "Bean count={}",
                bean.getCount()
        );
        LOGGER.info(
                "Bean value={}",
                mbsc.invoke(
                        objectName,
                        "totalValue",
                        new Object[0],
                        new String[0]
                )
        );
        instances = mbsc.queryMBeans(objectName, null);
        for (final ObjectInstance instance : instances) {
            LOGGER.info("Instance: {}", instance);
        }
        connector.close();
    }

}
