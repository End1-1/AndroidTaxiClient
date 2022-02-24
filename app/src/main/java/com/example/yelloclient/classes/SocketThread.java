package com.example.yelloclient.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.yelloclient.Config;
import com.example.yelloclient.Preference;
import com.example.yelloclient.TaxiApp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SocketThread implements Runnable {

    public static final byte SEND_TEXT = 0x1;
    public static final String SOCKET_MESSAGE = "socket_message";

    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private boolean mIsStopped;
    private byte [] mBuffer;
    private int mBufferPos;
    private short mBufferSize;
    private boolean mStopped = false;
    private BlockingDeque<String> mMessageBuffer = new LinkedBlockingDeque<>();

    public SocketThread() {
        mIsStopped = true;
        mBufferSize = 0;
        LocalBroadcastManager.getInstance(TaxiApp.getContext()).registerReceiver(mBroadcastManager, new IntentFilter(Messanger.IntentId));
    }

    private BroadcastReceiver mBroadcastManager = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra("event", 0)) {
                case Messanger.MSG_WEBSOCKET_MESSAGE:
                    try {
                        mMessageBuffer.put(intent.getStringExtra("data"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case Messanger.MSG_SOCKET_CONNECTION:
                    mStopped = intent.getBooleanExtra("disconnected", false);
                    break;
            }
        }
    };

    SSLSocketFactory sslSocketFactory() {
        SSLContext sc = null;
        try {
            TrustManager[] victimizedManager = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            sc = SSLContext.getInstance("TLS");
            sc.init(null, victimizedManager, new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sc.getSocketFactory();
    }

    @Override
    public void run() {
        try {
            SSLSocketFactory sf = sslSocketFactory();
            SSLSocketFactory factory = sf; //(SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket(Config.host(), 6001);
            socket.setSoTimeout(3000);
            socket.startHandshake();
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            out.print("GET /app/324345 HTTP/1.1\r\n");
            out.print("Host: hjtaxi.loc:6001\r\n");
            out.print("Upgrade: websocket\r\n");
            out.print("Authorization: Bearer " + Config.bearerKey() + "\r\n");
            out.print("Connection: Upgrade\r\n");
            out.print("Origin: http://www.websocket.org\r\n");
            out.print("Sec-WebSocket-Key: bHi/xD6v0LGIhSXi474s8g==\r\n");
            out.print("Sec-WebSocket-Version: 13\r\n");
            out.print("\r\n");
            out.print("\r\n");
            out.flush();

            if (out.checkError()) {
                System.out.println("SSLSocketClient:  java.io.PrintWriter error");
            }

            byte[] bytes = new byte[16384];
            mInputStream = socket.getInputStream();
            mOutputStream = socket.getOutputStream();
            int br = mInputStream.read(bytes);
            if (br != -1) {
                int headerEnd = new String(bytes, 0, br).indexOf("\r\n\r\n", 0);
                headerEnd += 4;
                String response = new String(bytes, 0, headerEnd);
                Log.d("WEBSOCKET RESPONSE", response);
                byte opcode = bytes[headerEnd++];
                int size = bytes[headerEnd++] & 0x7f;
                String msg = new String(bytes, headerEnd, br - headerEnd);
                Log.d("FIRST EVETN  ", msg);
                mIsStopped = false;
                stringToEvent(msg);
                loopEvents();
            }

            mOutputStream.close();
            mInputStream.close();
            out.close();
            socket.close();
        } catch(IOException e){
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault());
            System.out.println(df.format(c));
            e.printStackTrace();
        }

        Messanger.create(Messanger.MSG_SOCKET_CONNECTION)
                .putExtra("connected", 0)
                .broadcast();
    }

    public void loopEvents() {
        Messanger.create(Messanger.MSG_SOCKET_CONNECTION)
                .putExtra("connected", 1)
                .broadcast();
        do {
            try {
                while (mMessageBuffer.size() > 0) {
                    String msg = mMessageBuffer.take();
                    if (msg.contains("client-broadcast-api/ping")) {
                        continue;
                    }
                    if (msg.contains("subscribe-free")) {
                        JsonObject jo = JsonParser.parseString(msg).getAsJsonObject();
                        subscribeToChannel(jo.get("channel").getAsString(), Config.socketId(), 30);
                        continue;
                    }
                    Log.d("SEND SOCKET", msg);
                    sendData(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte [] bytes = new byte[16384];
            int br = 0;
            try {
                if (mBufferSize == 0) {
                    br = mInputStream.read(bytes, 0,2);
                    if (br < 0) {
                        Log.d("CONENCTION CLOSED: ", "Bye");
                        return;
                    }
                    //byte[0] contains opcond and trash
                    short ns = (short) (bytes[1] & 0x7f);
                    if (ns < 126) {
                        mBufferSize = ns;
                    } else {
                        br = mInputStream.read(bytes, 0, 2);
                        mBufferSize = (short) (((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff));
                    }
                    mBuffer = new byte [mBufferSize];
                    mBufferPos = 0;
                }
                br = mInputStream.read(bytes, 0, mBufferSize);
                if (br < 0) {
                    Messanger.create(Messanger.MSG_SOCKET_CONNECTION)
                            .putExtra("connected", 0)
                            .broadcast();
                    Log.d("CONENCTION CLOSED: ", "Bye");
                    return;
                }
                System.arraycopy(bytes, 0, mBuffer, mBufferPos, br);
                bytes = null;
                mBufferSize -= br;
                mBufferPos += br;
                if (mBufferSize == 0) {
                    stringToEvent(new String(mBuffer));
                    mBuffer = null;
                }
            } catch (SocketTimeoutException e) {
                //Log.d("SOCKET READ TIMEOUT", "LOOP EVENT");
                //e.printStackTrace();
            } catch (SocketException e) {
                mIsStopped = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (!mIsStopped && !Thread.currentThread().isInterrupted() && !mStopped);
        mBuffer = null;
        mBufferPos = 0;
        mBufferSize = 0;
        Messanger.create(Messanger.MSG_SOCKET_CONNECTION)
                .putExtra("connected", 0)
                .broadcast();
        Log.d("IM QUIT FROOM LOOP", mIsStopped ?  "STOPPED" : "NOT STOPPED");
    }

    public void stringToEvent(String s) {
        try {
            JSONObject jo = new JSONObject(s);
            String event = jo.getString("event");
            Log.d("EVENT", event);
            if (event.equals("pusher:connection_established")) {
                JSONObject jd = new JSONObject(jo.getString("data"));
                String socketId = jd.getString("socket_id");
                Preference.setString("channel_socket_id", socketId);
                int activityTimeout = jd.getInt("activity_timeout");
                subscribeToChannel(Config.channelName(), socketId, activityTimeout);
            } else if (event.equals("pusher_internal:subscription_succeeded")) {
                Log.d("LOGGIN IN", "SUBSCRIBED TO CHANNEL");
            } else {
                Intent socketMessage = new Intent(SocketThread.SOCKET_MESSAGE);
                socketMessage.putExtra("event", s);
                LocalBroadcastManager.getInstance(TaxiApp.getContext()).sendBroadcast(socketMessage);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribeToChannel(String channel, String socketId, int activityTimeout) throws Exception {
        Log.d("WebSocket channel name", channel);
        String strToCrypt = socketId + ":" + channel;
        String secret = "34345";
        String auth = hash_hmac(strToCrypt, secret).toLowerCase();
        String data = String.format("{\"event\":\"pusher:subscribe\",\"data\":{\"auth\":\"324345:%s\",\"channel\":\"%s\"}}", auth, channel);
        sendData(data);
    }

    public void sendData(String data) throws UnsupportedEncodingException {
        short msg_size = (short) data.getBytes("UTF-8").length;
        boolean useMask = true;
        byte[] h =  new byte[1];
        if (msg_size < 126) {
            h = new byte[2];
            h[0] = (byte) (0x80 | SEND_TEXT);
            h[1] = (byte) ((msg_size & 0xff) | (useMask ? 0x80 : 0x00));
        } else if (msg_size < 65536) {
            h = new byte[4];
            h[0] = (byte) (0x80 | SEND_TEXT);
            h[1] = (byte) (126 | (useMask ? 0x80 : 0));
            h[2] = (byte) ((msg_size >> 8) & 0xff);
            h[3] = (byte) ((msg_size >> 0) & 0xff);
        } else {
            //TODO: test blyad
//                short[] h = {0, 0, 0, 0, 0, 0, 0, 0, 0};
//                h[0] = 127 | (useMask ? 0x80 : 0);
//                h[1] = (msg_size >> 56) & 0xff;
//                h[2] = (msg_size >> 48) & 0xff;
//                h[3] = (msg_size >> 40) & 0xff;
//                h[4] = (msg_size >> 32) & 0xff;
//                h[5] = (msg_size >> 24) & 0xff;
//                h[6] = (msg_size >> 16) & 0xff;
//                h[7] = (msg_size >>  8) & 0xff;
//                h[8] = (msg_size >>  0) & 0xff;
        }
        byte[] cmask = {0x70, 0x71, 0x72, 0x73};
        byte[] dataWithMask = data.getBytes("UTF-8");
        if (useMask) {
            for (int i = 0; i < dataWithMask.length; ++i) {
                dataWithMask[i] = (byte) (dataWithMask[i] ^ cmask[i & 0x3]);
            }
        }
        int totalSize = h.length + data.getBytes("UTF-8").length + cmask.length;
        byte [] rawData = new byte [totalSize];
        try {
            System.arraycopy(h, 0, rawData, 0, h.length);
            System.arraycopy(cmask, 0, rawData, h.length, cmask.length);
            System.arraycopy(dataWithMask, 0, rawData, h.length + cmask.length, dataWithMask.length);
            mOutputStream.write(rawData);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String hash_hmac(String str, String secret) throws Exception{
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKey);
        //String hash = Base64.encodeToString(sha256_HMAC.doFinal(str.getBytes()), Base64.DEFAULT);
        String hash = bytesToHex(sha256_HMAC.doFinal(str.getBytes("UTF-8")));
        return hash;
    }

    String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
