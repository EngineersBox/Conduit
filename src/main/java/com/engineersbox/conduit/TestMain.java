package com.engineersbox.conduit;

import io.riemann.riemann.Proto;
import io.riemann.riemann.client.RiemannClient;
import org.apache.commons.collections4.ListUtils;

import java.io.IOException;
import java.util.List;

public class TestMain {

    public static void main(final String[] args) throws IOException {
        final RiemannClient client = RiemannClient.tcp("localhost", 5555);
        client.connect();
        client.event().
                service("fridge").
                state("running").
                metric(5.3).
                tags("appliance", "cold").
                send().
                deref(5000, java.util.concurrent.TimeUnit.MILLISECONDS);

        final List<Proto.Event> events = client.query("tagged \"cold\" and metric > 0").deref(); // => List<Event>;
        System.out.println(events);
        client.close();
    }

}
