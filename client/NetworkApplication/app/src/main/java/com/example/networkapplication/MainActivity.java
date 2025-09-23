package com.example.networkapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.networkapplication.databinding.ActivityMainBinding;

import java.io.InputStream;
import java.net.Proxy;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CertificatePinner;
import okhttp3.Handshake;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private OkHttpClient okHttpClient = new OkHttpClient();

    public static SSLContext sslContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    private void showAccessResult(Message message) {
        if (message == null) {
            mainHandler.post(() -> binding.tvAccessResult.setText(""));
        } else {
            mainHandler.post(() -> binding.tvAccessResult.setText((CharSequence) message.obj));
        }
    }

    private void ToastText(String text) {
        mainHandler.post(() -> Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private void initView() {
        /*
         * http connect
         */
        binding.btnHttpConnect.setOnClickListener(v -> new Thread(() -> {
            showAccessResult(null);
            Message message = new Message();
            message.what = 1;

            Request request = new Request.Builder().url("http://httpbin.org/").build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                String text = "http connect access httpbin.org success, return code: " + response.code();
                message.obj = text;
                Log.d(TAG, text);
            } catch (Exception e) {
                String text = "http connect access httpbin.org failed, e: " + e;
                message.obj = text;
                Log.e(TAG, text);
            }

            showAccessResult(message);
        }).start());


        /*
         * https connect ignore cert check
         */
        binding.btnHttpsConnectIgnoreCertCheck.setOnClickListener(v -> new Thread(() -> {
            showAccessResult(null);
            Message message = new Message();
            message.what = 1;

            OkHttpClient client = okHttpClient.newBuilder().sslSocketFactory(TrustAllManager.createSSLSocketFactory(), new TrustAllManager()).hostnameVerifier(new TrustAllManager.TrustAllHostnameVerifier()).build();
            Request request = new Request.Builder().url("https://www.baidu.com/s?wd=HttpsConnectIgnoreCertCheck").build();
            try (Response response = client.newCall(request).execute()) {
                String text = "https connect ignore cert check access www.baidu.com success, return code: " + response.code();
                message.obj = text;
                Log.d(TAG, text);
            } catch (Exception e) {
                String text = "https connect ignore cert check access www.baidu.com failed, e: " + e;
                message.obj = text;
                Log.e(TAG, text);
            }

            showAccessResult(message);
        }).start());


        /*
         * https connect system cert check
         *
         * 默认证书链校验，只信任系统 CA (根证书)
         *
         * Tips: OKHTTP 默认的 https 请求使用系统 CA 验证服务端证书（Android 7.0 以下还信任用户证书，Android 7.0 开始默认只信任系统证书）
         */
        binding.btnHttpsConnectSystemCertCheck.setOnClickListener(v -> new Thread(() -> {
            showAccessResult(null);
            Message message = new Message();
            message.what = 1;

            Request request = new Request.Builder().url("https://www.baidu.com/s?wd=HttpsConnectSystemCertCheck").build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                String text = "https connect system cert check access www.baidu.com success, return code: " + response.code();
                message.obj = text;
                Log.d(TAG, text);
            } catch (Exception e) {
                String text = "https connect system cert check access www.baidu.com failed, e: " + e;
                message.obj = text;
                Log.e(TAG, text);
            }

            showAccessResult(message);
        }).start());


        /*
         * get pins
         */
        binding.btnGetPins.setOnClickListener(v -> {
            new Thread(() -> {
                String url = binding.etPinsUrl.getText().toString();
                Request request = new Request.Builder().url(url).build();
                try (Response response = okHttpClient.newCall(request).execute()) {
                    Handshake handshake = response.handshake();
                    if (handshake != null) {
                        Certificate certificate = handshake.peerCertificates().get(0);
                        if (certificate instanceof X509Certificate) {
                            byte[] hash = MessageDigest.getInstance("SHA-256").digest(certificate.getPublicKey().getEncoded());
                            String pins = "sha256/" + Base64.encodeToString(hash, Base64.NO_WRAP);
                            String text = "get pins success, pins: " + pins;
                            Log.d(TAG, text);
                            ToastText(text);
                        }
                    }
                } catch (Exception e) {
                    String text = "get pins failed, e: " + e;
                    Log.e(TAG, text);
                    ToastText(text);
                }
            }).start();
        });


        /*
         * ssl pinning code or file check
         *
         * 证书公钥绑定：验证证书公钥 baidu.com 使用 CertificatePinner
         * 证书文件绑定：验证证书文件 bing.com  使用 SSLSocketFactory
         */
        binding.btnSslPinningCodeOrFileCheck.setOnClickListener(v -> new Thread(() -> {
            showAccessResult(null);
            Message message = new Message();
            message.what = 1;

            final String pattern = "www.baidu.com";
            final String pins = "sha256/aqQ4L+Pac7Qy3Or7l6f9IypN8w1H64i48B4weiXJ2v4=";
            CertificatePinner certificatePinner = new CertificatePinner.Builder().add(pattern, pins).build();
            OkHttpClient client1 = okHttpClient.newBuilder().certificatePinner(certificatePinner).build();
            Request request1 = new Request.Builder().url("https://www.baidu.com/s?wd=SslPinningCodeCheck").build();
            try (Response response1 = client1.newCall(request1).execute()) {
                String text = "ssl pinning code check access www.baidu.com success, return code: " + response1.code();
                message.obj = text;
                Log.d(TAG, text);
            } catch (Exception e) {
                String text = "ssl pinning code check access www.baidu.com failed, e: " + e;
                message.obj = text;
                Log.d(TAG, text);
            }


            try {
                InputStream is = getApplicationContext().getResources().openRawResource(R.raw.cn_bing_com);
                Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(is);
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                keyStore.setCertificateEntry("certificate", certificate);
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

                OkHttpClient client2 = okHttpClient.newBuilder().sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagerFactory.getTrustManagers()[0]).build();
                Request request2 = new Request.Builder().url("https://cn.bing.com/search?q=SslPinningFileCheck").build();
                Response response2 = client2.newCall(request2).execute();
                String text = "ssl pinning file check access cn.bing.com success, return code: " + response2.code();
                message.obj += "\n" + text;
                Log.d(TAG, text);
                response2.close();
            } catch (Exception e) {
                String text = "ssl pinning file check access cn.bing.com failed, e: " + e;
                message.obj += "\n" + text;
                Log.d(TAG, text);
            }

            showAccessResult(message);
        }).start());


        /*
         * ssl pinning code xml or file xml check
         *
         * 证书绑定验证 配置在 @xml/network_security_config 中
         * sogou.com 使用 sogou.pem 验证证书
         * so.com 使用 sha256 key 验证
         */
        binding.btnSslPinningCodeXmlOrFileXmlCheck.setOnClickListener(v -> new Thread(() -> {
            showAccessResult(null);
            Message message = new Message();
            message.what = 1;

            Request request1 = new Request.Builder().url("https://www.zhihu.com/").build();
            try (Response response1 = okHttpClient.newCall(request1).execute()) {
                String text = "ssl pinning code xml check access www.zhihu.com success, return code: " + response1.code();
                message.obj = text;
                Log.d(TAG, text);
            } catch (Exception e) {
                String text = "ssl pinning code xml check access www.zhihu.com failed, e: " + e;
                message.obj = text;
                Log.d(TAG, text);
            }

            Request request2 = new Request.Builder().url("https://www.sogou.com/web?query=SslPinningFileXmlCheck").build();
            try (Response response2 = okHttpClient.newCall(request2).execute()) {
                String text = "ssl pinning file xml check access www.sogou.com success, return code: " + response2.code();
                message.obj += "\n" + text;
                Log.d(TAG, text);
            } catch (Exception e) {
                String text = "ssl pinning file xml check access www.sogou.com failed, e: " + e;
                message.obj += "\n" + text;
                Log.d(TAG, text);
            }

            showAccessResult(message);
        }).start());



        /*
         * no_proxy
         *
         * 目前仅限 OkHttp 发出的请求
         */
        binding.swNoProxy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                okHttpClient = new OkHttpClient().newBuilder().proxy(Proxy.NO_PROXY).build();
            } else {
                okHttpClient = new OkHttpClient();
            }
        });
    }

}