import shutil

shutil.copy2("cert/client.p12", "../client/NetworkApplication/app/src/main/assets/client.p12")

shutil.copy2("cert/root-ca.crt", "../client/NetworkApplication/app/src/main/res/raw/www_example_com.crt")
