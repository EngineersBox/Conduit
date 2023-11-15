package com.engineersbox.conduit.example.jmx;

import com.engineersbox.conduit.example.bean.Example;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class JMXServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JMXServer.class);

    public JMXServer() throws Exception {
        final Registry registry = LocateRegistry.createRegistry(9081);
        LOGGER.info("Started RMI registry on port 9081");
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final Example helloBean = new Example();
        final ObjectName helloName = new ObjectName("SimpleAgent:name=test");
        mbs.registerMBean(helloBean, helloName);
        final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:9081/jmxrmi");
        mbs.addNotificationListener(
                helloName,
                new LoggingListener(),
                null,
                null
        );
        LOGGER.info("Service URL: " + url);
        final JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(
                url,
                null,
                mbs
        );
        cs.start();
    }

    public static void main(final String[] args) throws Exception {
        final JMXServer agent = new JMXServer();
        LOGGER.info("SimpleAgent is running...");
    }
}
