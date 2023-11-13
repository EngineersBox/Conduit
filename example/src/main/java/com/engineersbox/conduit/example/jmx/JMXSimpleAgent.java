package com.engineersbox.conduit.example.jmx;

import com.engineersbox.conduit.example.bean.Hello;

import javax.management.*;
import java.lang.management.*;

public class JMXSimpleAgent {

    public JMXSimpleAgent() {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final Hello helloBean = new Hello();
        final ObjectName helloName;
        try {
            helloName = new ObjectName("SimpleAgent:name=hellothere");
            mbs.registerMBean(helloBean, helloName);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void waitForEnterPressed() {
        try {
            System.out.println("Press <enter> to continue...");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(final String args[]) {
        final JMXSimpleAgent agent = new JMXSimpleAgent();
        System.out.println("SimpleAgent is running...");
        JMXSimpleAgent.waitForEnterPressed();
    }
}
