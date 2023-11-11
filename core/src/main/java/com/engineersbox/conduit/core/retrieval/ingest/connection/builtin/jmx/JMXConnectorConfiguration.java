package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.jmx;

import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.jmx.build.JMXEnvironmentProvider;
import com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.jmx.build.JMXSubjectProvider;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;
import java.net.MalformedURLException;
import java.util.Map;

public class JMXConnectorConfiguration implements ConnectorConfiguration {

    private final JMXServiceURL url;
    private final Map<String, ?> environment;
    private final Subject delegationSubject;

    @JsonCreator
    public JMXConnectorConfiguration(@JsonProperty(value = "url", required = true) final String url,
                                     @JsonProperty("environment") @Nullable final String environment,
                                     @JsonProperty("delegationSubject") @JsonAlias("delegation_subject") @Nullable final String delegationSubject) throws MalformedURLException {
        this.url = new JMXServiceURL(url);
        this.environment = JMXEnvironmentProvider.getEnvironment(environment);
        this.delegationSubject = JMXSubjectProvider.getSubject(delegationSubject);
    }

    public JMXServiceURL getServiceUrl() {
        return this.url;
    }

    public Map<String, ?> getEnvironment() {
        return this.environment;
    }

    public Subject getDelegationSubject() {
        return this.delegationSubject;
    }

}
