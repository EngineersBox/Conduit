# Commands

## Basic Auth

Start server:

`sauth <username> <password> <address> <port>`

## SSL

Start server:

`twistd -no web --https=<port> -c <certificate path [default: server.pem]> -k <private key, [default: server.pem]`

Generate a PKCS12 cert:

`openssl pkcs12 -export -in cert.pem -inkey key.pem -out certificate.p12 -name "certificate"`
