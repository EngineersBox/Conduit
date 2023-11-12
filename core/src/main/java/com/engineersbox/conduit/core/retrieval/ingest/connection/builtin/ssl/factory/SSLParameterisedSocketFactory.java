package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.ssl.factory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SSLParameterisedSocketFactory extends SSLSocketFactory {

    private final SSLParameters parameters;
    private final SSLSocketFactory factory;

    public SSLParameterisedSocketFactory(final SSLContext context,
                                         final SSLParameters parameters) {
        this.parameters = parameters;
        this.factory = context.getSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return this.factory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return this.factory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(final Socket s,
                               final String host,
                               final int port,
                               final boolean autoClose) throws IOException {
        final SSLSocket socket = (SSLSocket) this.factory.createSocket(s, host, port, autoClose);
        socket.setSSLParameters(this.parameters);
        return socket;
    }

    @Override
    public Socket createSocket(final String host,
                               final int port) throws IOException, UnknownHostException {
        final SSLSocket socket = (SSLSocket) this.factory.createSocket(host, port);
        socket.setSSLParameters(this.parameters);
        return socket;
    }

    @Override
    public Socket createSocket(final String host,
                               final int port,
                               final InetAddress localHost,
                               final int localPort) throws IOException, UnknownHostException {
        final SSLSocket socket = (SSLSocket) this.factory.createSocket(
                host,
                port,
                localHost,
                localPort
        );
        socket.setSSLParameters(this.parameters);
        return socket;
    }

    @Override
    public Socket createSocket(final InetAddress host,
                               final int port) throws IOException {
        final SSLSocket socket = (SSLSocket) this.factory.createSocket(
                host,
                port
        );
        socket.setSSLParameters(this.parameters);
        return socket;
    }

    @Override
    public Socket createSocket(final InetAddress address,
                               final int port,
                               final InetAddress localAddress,
                               final int localPort) throws IOException {
        final SSLSocket socket = (SSLSocket) this.factory.createSocket(
                address,
                port,
                localAddress,
                localPort
        );
        socket.setSSLParameters(this.parameters);
        return socket;
    }
}
