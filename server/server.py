import os
import socket
import ssl
import sys
from http.server import BaseHTTPRequestHandler, HTTPServer

server_cert = os.path.join(os.getcwd(), "certs", "server-cert.cer")
server_key = os.path.join(os.getcwd(), "certs", "server-key.key")
client_cert = os.path.join(os.getcwd(), "certs", "client-cert.cer")

print("server_cert:", server_cert)
print("server_key:", server_key)
print("client_cert:", client_cert)


class RequestHandler(BaseHTTPRequestHandler):
    def _writeheaders(self):
        self.send_response(200)
        self.send_header("Content-type", "text/plain")
        self.end_headers()

    def do_GET(self):
        self._writeheaders()
        self.wfile.write("OK".encode("utf-8"))


def get_local_host() -> str:
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        host = s.getsockname()[0]
        s.close()
        return host
    except Exception:  # noqa
        return "127.0.0.1"


def main():
    port = int(sys.argv[1]) if len(sys.argv) == 2 else 443
    server_address = ("0.0.0.0", port)

    server = HTTPServer(server_address, RequestHandler)  # type: ignore

    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    context.load_cert_chain(certfile=server_cert, keyfile=server_key)
    context.verify_mode = ssl.CERT_REQUIRED
    context.load_verify_locations(client_cert)

    server.socket = context.wrap_socket(server.socket, server_side=True, do_handshake_on_connect=False)

    print(f"Starting server, listen at: {server_address[0]}:{server_address[1]}")
    print(f"Access from LAN: https://{get_local_host()}:{server_address[1]}")
    server.serve_forever()


if __name__ == "__main__":
    main()
