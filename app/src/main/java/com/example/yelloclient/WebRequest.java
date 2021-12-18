package com.example.yelloclient;

public class WebRequest {

    public enum  HttpMethod {
        GET,
        POST,
        PUT
    }

    private String mUrl;
    private HttpMethod mMethod;
    private int mResponseCode;
    private WebResponse mWebResponse;
    private Map<String, String> mHeader;
    private Map<String, String> mParameters;
    private String mData;
    private int mWebResponseCode;
    private String mOutputData;

    public WebQuery(String url, HttpMethod method, int responseCode, WebResponse r) {
        mData = "";
        mOutputData = "";
        mWebResponseCode = 0;
        mHeader = new HashMap<>();
        mParameters = new HashMap<>();
        mUrl = url;
        mMethod = method;
        mResponseCode = responseCode;
        mWebResponse = r;

        setHeader("Authorization", "Bearer " + UPref.mBearerKey);
        setHeader("Accept", "application/json");
    }

    public WebQuery setHeader(String key, String value) {
        mHeader.put(key, value);
        return this;
    }

    public WebQuery setParameter(String key, String value) {
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
                    throw new IOException("Unexpected code " + Integer.toString(response.code()) + " " + response.body().string());
                }
                mWebResponseCode = 200;
                mOutputData = response.body().string();
                System.out.println(mOutputData);
            }
            catch (Exception e) {
                mOutputData = e.getMessage();
                mWebResponseCode = 500;
                System.out.println(mResponseCode);
                e.printStackTrace();
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (mWebResponse != null) {
                        mWebResponse.webResponse(mResponseCode, mWebResponseCode, mOutputData);
                    }
                }
            });
        });
        thread.start();
    }

    public static WebQuery create(String url, HttpMethod method, int responseCode, WebResponse r) {
        return new WebQuery(url, method, responseCode, r);
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
