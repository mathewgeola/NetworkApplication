@echo off
setlocal


set JAVA_OPTS=--enable-native-access=ALL-UNNAMED


if not exist data mkdir data


if not exist data\my-release-key.keystore (
  echo Generating keystore...
  keytool -genkeypair ^
    -keystore data\release-key.keystore ^
    -alias alias ^
    -keyalg RSA ^
    -keysize 2048 ^
    -validity 36500 ^
    -sigalg SHA256withRSA ^
    -storetype PKCS12 ^
    -dname "CN=NetworkApplication, OU=Dev, O=Company, L=Hangzhou, ST=Zhejiang, C=CN" ^
    -storepass 123456 ^
    -keypass 123456
  if %ERRORLEVEL% NEQ 0 (
      echo Error generating keystore!
      pause
      exit /b 1
  )
) else (
  echo Keystore already exists, skipping generation.
)


echo Cleaning project...
call gradlew clean
if %ERRORLEVEL% NEQ 0 (
    echo Clean failed!
    pause
    exit /b 1
)


echo Building Release APK...
call gradlew assembleRelease
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)


echo.
echo Release APK generated at:
echo %CD%\app\build\outputs\apk\release\app-release.apk
echo.
pause
