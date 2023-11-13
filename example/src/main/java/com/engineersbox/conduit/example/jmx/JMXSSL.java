package com.engineersbox.conduit.example.jmx;

import com.engineersbox.conduit.example.bean.Hello;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

public class JMXSSL {

    public static void main(final String[] args) {
        try {
            final MBeanServer mbs = MBeanServerFactory.createMBeanServer();

            final Map<String, Object> env = new HashMap<>();
            final String keystore = "config" + File.separator + "keystore";
            final char[] keystorepass = "password".toCharArray();
            final char[] keypassword = "password".toCharArray();
            final KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(keystore), keystorepass);
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keypassword);
            final SSLContext ctx = SSLContext.getInstance("TLSv1");
            ctx.init(kmf.getKeyManagers(), null, null);
            final SSLSocketFactory ssf = ctx.getSocketFactory();

            env.put("jmx.remote.profiles", "TLS");
            env.put("jmx.remote.tls.socket.factory", ssf);
            env.put("jmx.remote.tls.enabled.protocols", "TLSv1");
            env.put("jmx.remote.tls.enabled.cipher.suites",
                    "SSL_RSA_WITH_NULL_MD5");

            final Hello helloBean = new Hello();
            final ObjectName helloName;
            try {
                helloName = new ObjectName("SimpleAgent:name=hellothere");
                mbs.registerMBean(helloBean, helloName);
            } catch(final Exception e) {
                e.printStackTrace();
            }

            final JMXServiceURL url = new JMXServiceURL("jmxmp", null, 5555);
            final JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(
                    url,
                    env,
                    mbs
            );
            cs.start();

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
