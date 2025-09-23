@echo off
chcp 65001 >nul

REM ===== 设置证书目录 =====
set "CERT_DIR=%~dp0cert"
if not exist "%CERT_DIR%" mkdir "%CERT_DIR%"

REM ===== 根 CA =====
set "ROOT_KEY=%CERT_DIR%\root-ca.key"
set "ROOT_CERT=%CERT_DIR%\root-ca.crt"

REM ===== Server =====
set "SERVER_KEY=%CERT_DIR%\server.key"
set "SERVER_CERT=%CERT_DIR%\server.crt"
set "SERVER_CONF=%CERT_DIR%\server.conf"
set "SERVER_CSR=%CERT_DIR%\server.csr"

REM ===== Client =====
set "CLIENT_KEY=%CERT_DIR%\client.key"
set "CLIENT_CSR=%CERT_DIR%\client.csr"
set "CLIENT_CERT=%CERT_DIR%\client.crt"
set "CLIENT_P12=%CERT_DIR%\client.p12"

REM ==============================
REM ===== 生成 CA 根证书 =====
REM ==============================
openssl genrsa -out "%ROOT_KEY%" 2048
openssl req -x509 -new -nodes -key "%ROOT_KEY%" -sha256 -days 3650 -out "%ROOT_CERT%" ^
    -subj "/C=CN/ST=Zhejiang/L=Hangzhou/O=Company/OU=Dev/CN=RootCA"

REM ==============================
REM ===== 生成 Server 配置文件 =====
REM ==============================
(
echo [ req ]
echo distinguished_name = req_distinguished_name
echo req_extensions = v3_req
echo prompt = no
echo.
echo [ req_distinguished_name ]
echo C = CN
echo ST = Zhejiang
echo L = Hangzhou
echo O = Company
echo OU = Dev
echo CN = www.example.com
echo.
echo [ v3_req ]
echo subjectAltName = @alt_names
echo.
echo [ alt_names ]
echo DNS.1 = www.example.com
) > "%SERVER_CONF%"

REM ==============================
REM ===== 生成 Server 证书 =====
REM ==============================
openssl genrsa -out "%SERVER_KEY%" 2048
openssl req -new -key "%SERVER_KEY%" -out "%SERVER_CSR%" -config "%SERVER_CONF%"
openssl x509 -req -in "%SERVER_CSR%" -CA "%ROOT_CERT%" -CAkey "%ROOT_KEY%" -CAcreateserial -out "%SERVER_CERT%" -days 3650 -sha256 -extfile "%SERVER_CONF%" -extensions v3_req

REM ==============================
REM ===== 生成客户端证书 =====
REM ==============================
openssl genrsa -out "%CLIENT_KEY%" 2048
openssl req -new -key "%CLIENT_KEY%" -out "%CLIENT_CSR%" -subj "/C=CN/ST=Zhejiang/L=Hangzhou/O=Company/OU=Dev/CN=client"
openssl x509 -req -in "%CLIENT_CSR%" -CA "%ROOT_CERT%" -CAkey "%ROOT_KEY%" -CAcreateserial -out "%CLIENT_CERT%" -days 3650 -sha256

REM ==============================
REM ===== 导出客户端 p12 文件 =====
REM ==============================
openssl pkcs12 -export -in "%CLIENT_CERT%" -inkey "%CLIENT_KEY%" -out "%CLIENT_P12%" -passout pass:password -keypbe PBE-SHA1-3DES -certpbe PBE-SHA1-3DES -macalg sha1

REM ==============================
REM ===== 删除中间文件 =====
REM ==============================
if exist "%SERVER_CSR%" del "%SERVER_CSR%"
if exist "%CLIENT_CSR%" del "%CLIENT_CSR%"
if exist "%CERT_DIR%\root-ca.srl" del "%CERT_DIR%\root-ca.srl"

REM ==============================
REM ===== 输出结果 =====
REM ==============================
echo ===============================
echo Finish!
echo CERT_DIR: "%CERT_DIR%"
echo    ROOT_CERT: "%ROOT_KEY%", "%ROOT_CERT%"
echo    Server: "%SERVER_KEY%", "%SERVER_CERT%", "%SERVER_CONF%"
echo    Client: "%CLIENT_KEY%", "%CLIENT_CERT%", "%CLIENT_P12%"
echo ===============================
pause
