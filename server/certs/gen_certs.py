import os

command = "openssl genrsa -out server-key.key 2048"
print(os.system(command))

command = "openssl req -new -key server-key.key -out server-req.csr -config server_csr.conf"
print(os.system(command))

command = "openssl x509 -req -in server-req.csr -signkey server-key.key -out server-cert.cer -days 3650"
print(os.system(command))

command = "openssl genrsa -out client-key.key 2048"
print(os.system(command))

command = "openssl req -new -key client-key.key -out client-req.csr -config client_csr.conf"
print(os.system(command))

command = "openssl x509 -req -in client-req.csr -signkey client-key.key -out client-cert.cer -days 3650"
print(os.system(command))

command = "openssl pkcs12 -export -in client-cert.cer -inkey client-key.key -out client.p12 -passout pass:password"
print(os.system(command))
