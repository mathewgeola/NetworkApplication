# NetworkApplication

~~~
> openssl s_client -connect www.baidu.com:443 -servername www.baidu.com | openssl x509 -pubkey -noout | openssl rsa -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64


> cd .\app\src\main\res\raw
> openssl s_client -connect cn.bing.com:443 -servername cn.bing.com | openssl x509 -out cn_bing_com.pem


~~~