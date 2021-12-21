package com.example.yelloclient;

import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

import android.os.Handler;
import android.os.Looper;

import org.conscrypt.Conscrypt;

import java.io.IOException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class WebRequest {

    public enum  HttpMethod {
        GET,
        POST,
        PUT
    }

    public interface HttpResponse {
        void httpRespone(int httpReponseCode, String data);
    }

    private String mUrl;
    private HttpMethod mMethod;
    private HttpResponse mWebResponse;
    private Map<String, String> mHeader;
    private Map<String, String> mParameters;
    private String mData;
    private int mHttpResponseCode;
    private String mOutputData;

    public WebRequest(String url, HttpMethod method, HttpResponse r) {
        mData = "";
        mOutputData = "";
        mHttpResponseCode = 0;
        mHeader = new HashMap<>();
        mParameters = new HashMap<>();
        mUrl = "https://" + Config.host() + url;
        mMethod = method;
        mWebResponse = r;

        setHeader("Authorization", "Bearer " + Config.bearerKey());
        setHeader("Accept", "application/json");
    }

    public WebRequest setHeader(String key, String value) {
        mHeader.put(key, value);
        return this;
    }

    public WebRequest setParameter(String key, String value) {
        mParameters.put(key, value);
        return this;
    }

    private RequestBody getBody() {
        FormBody.Builder form = new FormBody.Builder();
        for (Map.Entry<String, String> e: mParameters.entrySet()) {
            form.add(e.getKey(), e.getValue());
        }
        return form.build();
    }

    public void request() {
        Security.insertProviderAt(Conscrypt.newProvider(), 1);
        OkHttpClient httpClient = getUnsafeOkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url(mUrl);
        for (Map.Entry<String, String> e: mHeader.entrySet()) {
            builder.addHeader(e.getKey(), e.getValue());
        }
        switch (mMethod) {
            case GET:
                builder.get();
                break;
            case POST:
                builder.post(getBody());
                break;
        }
        Thread thread = new Thread(() -> {
            try {
                System.out.println(mUrl);
                final Buffer buffer = new Buffer();
                getBody().writeTo(buffer);
                System.out.println(buffer.readUtf8());
                Response response = httpClient.newCall(builder.build()).execute();
                if (!response.isSuccessful()) {
                    mHttpResponseCode = response.code();
                    throw new IOException(response.body().string());
                }
                mHttpResponseCode = response.code();
                mOutputData = response.body().string();
                System.out.println(mOutputData);
            }
            catch (Exception e) {
                mOutputData = e.getMessage();
                if (mHttpResponseCode == 0) {
                    mHttpResponseCode = -1;
                }
                e.printStackTrace();
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (mWebResponse != null) {
                        mWebResponse.httpRespone(mHttpResponseCode, mOutputData);
                    }
                }
            });
        });
        thread.start();
    }

    public static WebRequest create(String url, HttpMethod method, HttpResponse r) {
        return new WebRequest(url, method, r);
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance(SSL);
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
