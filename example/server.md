# Commands

## Basic Auth

Start server:

`sauth <username> <password> <address> <port>`

## SSL

Start server:

`twistd -no web --https=<port> -c <certificate path [default: server.pem]> -k <private key, [default: server.pem]`

Generate a key:

`openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 365`
