package com.example.networkapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.networkapplication.databinding.ActivityMainBinding;

import java.io.IOException;
import java.net.Proxy;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private OkHttpClient client = new OkHttpClient();

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

            OkHttpClient mClient = client.newBuilder().build();
            Request request = new Request.Builder().url("http://httpbin.org/").build();
            Message message = new Message();
            message.what = 1;
            try (Response response = mClient.newCall(request).execute()) {
                String text = "http connect access httpbin.org success, return code:" + response.code();
                message.obj = text;
                Log.d(TAG, text);
            } catch (IOException e) {
                String text = "http connect access httpbin.org failed";
                message.obj = text;
                Log.d(TAG, text);
                e.printStackTrace();
            }

            showAccessResult(message);
        }).start());


        /*
         * https connect ignore cert check
         */
        binding.btnHttpsConnectIgnoreCertCheck.setOnClickListener(v -> new Thread(() -> {
            showAccessResult(null);

            OkHttpClient mClient = client.newBuilder().sslSocketFactory(TrustAllManager.createSSLSocketFactory(), new TrustAllManager()).hostnameVerifier(new TrustAllManager.TrustAllHostnameVerifier()).build();
            Request request = new Request.Builder().url("https://www.baidu.com/s?wd=HttpsConnectIgnoreCertCheck").build();
            Message message = new Message();
            message.what = 1;
            try (Response response = mClient.newCall(request).execute()) {
                String text = "https connect ignore cert check access www.baidu.com success, return code:" + response.code();
                message.obj = text;
                Log.d(TAG, text);
            } catch (IOException e) {
                String text = "https connect ignore cert check access www.baidu.com failed";
                message.obj = text;
                Log.d(TAG, text);
                e.printStackTrace();
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
                client = new OkHttpClient().newBuilder().proxy(Proxy.NO_PROXY).build();
            } else {
                client = new OkHttpClient();
            }
        });
    }

}