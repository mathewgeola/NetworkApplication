import os

import requests

CERT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "cert")
CLIENT_CRT = os.path.join(CERT_DIR, "client.crt")
CLIENT_KEY = os.path.join(CERT_DIR, "client.key")
CA_CRT = os.path.join(CERT_DIR, "root-ca.crt")

url = "https://www.example.com/"

resp = requests.get(
    url,
    cert=(CLIENT_CRT, CLIENT_KEY),
    verify=CA_CRT
)

print(resp.status_code)
print(resp.text)
