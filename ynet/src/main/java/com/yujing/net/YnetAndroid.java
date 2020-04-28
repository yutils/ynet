package com.yujing.net;

import android.graphics.Bitmap;
import android.os.Handler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * 安卓使用网络连接类
 *
 * @author 余静 2018年7月24日10:24:03
 * @version 2.5
 */
@SuppressWarnings("unused")
public class YnetAndroid extends Ynet {
    /**
     * 消息传递收到后回调监听器
     */
    public Handler handler = new Handler();
    protected Map<String, Bitmap> bitmapMap;

    public static YnetAndroid create() {
        return new YnetAndroid();
    }

    public static void get(String url, YnetListener ynetListener) {
        YnetAndroid ynet = new YnetAndroid();
        ynet.get(url);
        ynet.setYnetListener(ynetListener);
        ynet.start();
    }

    public static void post(String url, Map<String, Object> paramsMap, YnetListener ynetListener) {
        YnetAndroid ynet = new YnetAndroid();
        ynet.post(url, paramsMap);
        ynet.setYnetListener(ynetListener);
        ynet.start();
    }

    public static void post(String url, String str, YnetListener ynetListener) {
        YnetAndroid ynet = new YnetAndroid();
        ynet.setContentType("application/json;charset=utf-8");
        ynet.post(url, str);
        ynet.setYnetListener(ynetListener);
        ynet.start();
    }

    public static void post(String url, byte[] bytes, YnetListener ynetListener) {
        YnetAndroid yNet = new YnetAndroid();
        yNet.post(url, bytes);
        yNet.setYnetListener(ynetListener);
        yNet.start();
    }

    public static void uploadImg(String url, Map<String, Object> paramsMap, Map<String, File> fileMap, YnetListener ynetListener) {
        YnetAndroid yNet = new YnetAndroid();
        yNet.uploadImg(url, paramsMap, fileMap);
        yNet.setTimeout(1000 * 30);
        yNet.setYnetListener(ynetListener);
        yNet.start();
    }

    public static void uploadFile(String url, Map<String, Object> paramsMap, Map<String, File> fileMap, YnetListener ynetListener) {
        YnetAndroid yNet = new YnetAndroid();
        yNet.uploadFile(url, paramsMap, fileMap);
        yNet.setTimeout(1000 * 30);
        yNet.setYnetListener(ynetListener);
        yNet.start();
    }

    public static void uploadBitmap(String url, Map<String, Object> paramsMap, Map<String, Bitmap> bitmapMap, YnetListener ynetListener) {
        final YnetAndroid yNet = new YnetAndroid();
        yNet.setYnetListener(ynetListener);
        yNet.url = url;
        yNet.params = yNet.multiMapToParams(paramsMap);
        yNet.urlType = UrlType.POST_OTHER;
        yNet.flag = "bitmapMap";
        yNet.bitmapMap = bitmapMap;
        yNet.start();
    }

    @Override
    protected void sendOther(HttpURLConnection httpURLConnection) throws IOException {
        if ("bitmapMap".equals(flag)) {
            //安卓6.0下使用谷歌浏览器67
            //Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.62 Mobile Safari/537.36
            //iPhoneX下使用谷歌浏览器67
            //Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) CriOS/56.0.2924.75 Mobile/14E5239e Safari/602.1
            //win10下使用谷歌浏览器67
            //Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.62 Safari/537.36
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.62 Mobile Safari/537.36");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            OutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
            // 文本参数
            if (this.params != null) {
                out.write(params.toString().getBytes());
            }
            // 文件
            for (Map.Entry<String, Bitmap> entry : bitmapMap.entrySet()) {
                Bitmap bitmap = entry.getValue();
                if (bitmap == null)
                    continue;
                String strBuf = "\r\n--" + BOUNDARY + "\r\n" +
                        "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + "Android" + System.currentTimeMillis() + ".jpg" + "\"\r\n" +
                        "Content-Type:image/jpeg" + "\r\n\r\n";
                out.write(strBuf.getBytes());
                // bitmap转Bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                out.write(baos.toByteArray());
            }
            out.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes());// 结束标记
            out.flush();
            out.close();
        }
    }

    // 重写sendBackMsg方法，因为sendBackMsg在线程中执行，然而请求的数据一般情况需要修改界面
    @Override
    protected void sendBackMsg(final String value, final boolean success) {
        handler.post(() -> {
            if (showLog == null) {
                showLog = globalShowLog;
            }
            if (showLog)
                YLog.d("YNet","请求地址↓：" + (urlType == UrlType.GET ? "\nGet--->" : "\nPost--->") + url + (params == null ? "" : ("\n请求参数：" + params.toString())) + "\n请求结果：" + value);
                YLog.json(value);
            if (success) {
                if (ynetSuccessListener != null)
                    ynetSuccessListener.success(value);
                if (ynetListener != null) {
                    ynetListener.success(value);
                }
            } else {
                if (ynetFailListener != null)
                    ynetFailListener.fail(value);
                if (ynetListener != null) {
                    ynetListener.fail(value);
                }
            }
        });
    }

    @Override
    protected void sendBackMsg(final byte[] bytes) {
        if (ynetBytesListener != null) {
            handler.post(() -> ynetBytesListener.success(bytes));
        }
    }

    @Override
    protected void sendBackMsg(final InputStream inputStream) {
        if (ynetInputStreamListener != null) {
            handler.post(() -> {
                try {
                    ynetInputStreamListener.success(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
