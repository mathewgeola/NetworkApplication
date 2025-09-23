# NetworkApplication

## 证书命令

~~~shell
$ openssl s_client -connect www.baidu.com:443 -servername www.baidu.com | openssl x509 -pubkey -noout | openssl rsa -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64


$ cd app/src/main/res/raw/
$ openssl s_client -connect cn.bing.com:443 -servername cn.bing.com | openssl x509 -out cn_bing_com.pem


$ openssl s_client -connect www.zhihu.com:443 -servername www.zhihu.com | openssl x509 -pubkey -noout | openssl rsa -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64


$ cd app/src/main/res/raw/
$ openssl s_client -connect www.sogou.com:443 -servername www.sogou.com | openssl x509 -out www_sogou_com.pem

~~~

## window 关闭防火墙

~~~shell
C:\WINDOWS\system32> netsh advfirewall set allprofiles state off
~~~

## 双向认证环境

~~~shell
> openssl -v 
OpenSSL 3.5.2 5 Aug 2025 (Library: OpenSSL 3.5.2 5 Aug 2025)

# 电脑 修改 hosts 文件 -> 添加 127.0.0.1 www.example.com (结尾必须加个换行) -> 运行 server.py (python data/server.cmd)

# 手机 Magisk -> 安装 Systemless Hosts 17.2 模块 (data/hosts_17.2.zip) -> 重启手机 -> 修改 /system/etc/hosts -> 添加 192.168.3.150 www.example.com (结尾必须加个换行，192.168.3.150 为电脑的局域网 ip 地址)
~~~