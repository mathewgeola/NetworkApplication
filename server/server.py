import os
import socket
import ssl
from http.server import HTTPServer, BaseHTTPRequestHandler

CERT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "cert")
SERVER_CRT = os.path.join(CERT_DIR, "server.crt")
SERVER_KEY = os.path.join(CERT_DIR, "server.key")
CA_CRT = os.path.join(CERT_DIR, "root-ca.crt")


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):  # noqa
        self.send_response(200)
        self.send_header("Content-type", "text/plain")
        self.end_headers()
        self.wfile.write(b"Hello from HTTPS server!")


def get_local_host() -> str:
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        host = s.getsockname()[0]
        s.close()
        return host
    except Exception:  # noqa
        return "127.0.0.1"


def run_server():
    server_address = ("0.0.0.0", 443)
    httpd = HTTPServer(server_address, Handler)  # noqa

    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    context.load_cert_chain(certfile=SERVER_CRT, keyfile=SERVER_KEY)
    context.load_verify_locations(CA_CRT)
    context.verify_mode = ssl.CERT_REQUIRED

    httpd.socket = context.wrap_socket(httpd.socket, server_side=True)
    print(f"Starting server, listen at: {server_address[0]}:{server_address[1]}")
    print(f"Access from LAN: https://{get_local_host()}:{server_address[1]}/")
    print(f"Server listening on https://www.example.com/")
    httpd.serve_forever()


if __name__ == "__main__":
    run_server()
