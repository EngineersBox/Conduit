package com.engineersbox.conduit.retrieval.ingest.connection.builtin.http.build;

import com.engineersbox.conduit.retrieval.ingest.connection.builtin.http.build.constraint.AlgorithmConstraintDeserializer;
import com.engineersbox.conduit.util.Functional;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.net.ssl.SSLParameters;
import java.security.AlgorithmConstraints;
import java.util.function.Consumer;

public class SSLParametersProvider implements Functional.ThrowsSupplier<SSLParameters> {

    private final String[] cipherSuites;
    private final String[] applicationProtocols;
    private final String[] protocols;
    private final Boolean wantClientAuth;
    private final Boolean needClientAuth;
    private final AlgorithmConstraints algorithmConstraints; // TODO: Create map to instance static provider to get instance by string name as a deserialiser
    private final String endpointIdentificationAlgorithm;
    private final Boolean useCipherSuitesOrder;
    private final Boolean enableRetransmissions;
    private final Integer maximumPacketSize;
    private final String[] signatureSchemes;
    private final String[] namedGroups;

    @JsonCreator
    public SSLParametersProvider(@JsonProperty("cipherSuites") @JsonAlias("cipher_suites") final String[] cipherSuites,
                                 @JsonProperty("applicationProtocols") @JsonAlias("application_protocols") @JsonDeserialize(using = AlgorithmConstraintDeserializer.class) final String[] applicationProtocols,
                                 @JsonProperty("protocols") final String[] protocols,
                                 @JsonProperty("wantClientAuth") @JsonAlias("want_client_auth") final Boolean wantClientAuth,
                                 @JsonProperty("needClientAuth") @JsonAlias("need_client_auth") final Boolean needClientAuth,
                                 @JsonProperty("algorithmConstraints") @JsonAlias("algorithm_constraints") final AlgorithmConstraints algorithmConstraints,
                                 @JsonProperty("endpointIdentificationAlgorithm") @JsonAlias("endpoint_identification_algorithm") final String endpointIdentificationAlgorithm,
                                 @JsonProperty("useCipherSuitesOrder") @JsonAlias("use_cipher_suites_order") final Boolean useCipherSuitesOrder,
                                 @JsonProperty("enableRetransmissions") @JsonAlias("enable_retransmissions") final Boolean enableRetransmissions,
                                 @JsonProperty("maximumPacketSize") @JsonAlias("maximum_packet_size") final Integer maximumPacketSize,
                                 @JsonProperty("signatureSchemes") @JsonAlias("signature_schemes") final String[] signatureSchemes,
                                 @JsonProperty("namedGroups") @JsonAlias("named_groups") final String[] namedGroups) {
        this.cipherSuites = cipherSuites;
        this.applicationProtocols = applicationProtocols;
        this.protocols = protocols;
        this.wantClientAuth = wantClientAuth;
        this.needClientAuth = needClientAuth;
        this.algorithmConstraints = algorithmConstraints;
        this.endpointIdentificationAlgorithm = endpointIdentificationAlgorithm;
        this.useCipherSuitesOrder = useCipherSuitesOrder;
        this.enableRetransmissions = enableRetransmissions;
        this.maximumPacketSize = maximumPacketSize;
        this.signatureSchemes = signatureSchemes;
        this.namedGroups = namedGroups;
    }

    private static <T> void checkedApply(final Consumer<T> method,
                                         final T value) {
        if (value == null) {
            return;
        }
        method.accept(value);
    }

    @Override
    public SSLParameters get() {
        final SSLParameters params = new SSLParameters();
        checkedApply(params::setCipherSuites, this.cipherSuites);
        checkedApply(params::setApplicationProtocols, this.applicationProtocols);
        checkedApply(params::setProtocols, this.protocols);
        checkedApply(params::setWantClientAuth, this.wantClientAuth);
        checkedApply(params::setNeedClientAuth, this.needClientAuth);
        checkedApply(params::setAlgorithmConstraints, this.algorithmConstraints);
        checkedApply(params::setEndpointIdentificationAlgorithm, this.endpointIdentificationAlgorithm);
        checkedApply(params::setUseCipherSuitesOrder, this.useCipherSuitesOrder);
        checkedApply(params::setEnableRetransmissions, this.enableRetransmissions);
        checkedApply(params::setMaximumPacketSize, this.maximumPacketSize);
        checkedApply(params::setSignatureSchemes, this.signatureSchemes);
        checkedApply(params::setNamedGroups, this.namedGroups);
        return params;
    }
}
