package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.http.build;

import com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.http.build.constraint.AlgorithmConstraintDeserializer;
import com.engineersbox.conduit.core.util.Functional;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.net.ssl.SSLParameters;
import java.security.AlgorithmConstraints;

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

    @Override
    public SSLParameters get() {
        final SSLParameters params = new SSLParameters();
        Functional.checkedApply(params::setCipherSuites, this.cipherSuites);
        Functional.checkedApply(params::setApplicationProtocols, this.applicationProtocols);
        Functional.checkedApply(params::setProtocols, this.protocols);
        Functional.checkedApply(params::setWantClientAuth, this.wantClientAuth);
        Functional.checkedApply(params::setNeedClientAuth, this.needClientAuth);
        Functional.checkedApply(params::setAlgorithmConstraints, this.algorithmConstraints);
        Functional.checkedApply(params::setEndpointIdentificationAlgorithm, this.endpointIdentificationAlgorithm);
        Functional.checkedApply(params::setUseCipherSuitesOrder, this.useCipherSuitesOrder);
        Functional.checkedApply(params::setEnableRetransmissions, this.enableRetransmissions);
        Functional.checkedApply(params::setMaximumPacketSize, this.maximumPacketSize);
        Functional.checkedApply(params::setSignatureSchemes, this.signatureSchemes);
        Functional.checkedApply(params::setNamedGroups, this.namedGroups);
        return params;
    }
}
