package com.yujing.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 网络请求类
 *
 * @author 余静 2020年8月28日10:11:43
 * @version 1.0.4
 */
@SuppressWarnings("unused")
/*
使用方法：JAVA
★1网络请求，java用ynet，安卓用YnetAndroid
举例：
//get
    String url = "http://127.0.0.1:8080/yu/upload";
    Ynet.get(url, new ynetListener);

//post
    Map<String, Object> paramsMap = new HashMap<String, Object>();
    paramsMap.put("name", "yujing");
    paramsMap.put("content", "123456");
    String url = "http://127.0.0.1:8080/yu/upload";
    Ynet.post(url, paramsMap, new ynetListener);

//file
    Ynet ynet = new Ynet();
    Map<String, Object> paramsMap = new HashMap<String, Object>();
    paramsMap.put("name", "yujing");
    paramsMap.put("content", "123456");

    Map<String, File> fileMap = new HashMap<String, File>();
    fileMap.put("file", new File("D:/ab.jpg"));
    fileMap.put("file1", new File("D:/ac.jpg"));

    String url = "http://127.0.0.1:8080/yu/upload";
    ynet.uploadFile(url, paramsMap, fileMap);
    ynet.setYnetListener(new YnetListener() {
        @Override
        public void success(String value) {
        }
        @Override
        public void fail(String value) {

        }
    });

    ynet.setYnetBackSessionListener(new YnetBackSessionListener() {
        @Override
        public void backSessionId(String SessionId) {
            System.out.println("服务器返回的SessionId：" + SessionId);
        }
    });
    ynet.start();

★2文件下载，java用YDownload，安卓用YDownloadAndroid
举例：
	String url = "http://dldir1.qq.com/qqfile/qq/QQ8.9.2/20760/QQ8.9.2.exe";
	final String path = "D:/" + "yu/";
	String fileName = "QQ8.9.2.exe";
	YDownload.Download(url, path, fileName, new YDownloadListener() {
		@Override
		public void response(int downloadedSize, int fileSize, double progress, boolean isfail, File file) {
			if (isfail) {
				System.out.println("下载出错");
				return;
			}
			if (progress == 100) {// 进度
				System.out.println("下载完成:" + downloadedSize + "大小：" + fileSize + "百分比：" + progress);
			} else {
				System.out.println("下载进度:" + downloadedSize + "大小：" + fileSize + "百分比：" + progress);
			}
		}
	});
*/
public class Ynet extends Thread {
    /**
     * boundary就是request头和上传文件内容的分隔符
     */
    protected static String BOUNDARY = "------------yuJing---------------";
    /**
     * 线程队列同时最多运行个数
     */
    protected static int threadNum = 20;
    /**
     * 线程队列
     */
    protected static ScheduledThreadPoolExecutor sTEP = new ScheduledThreadPoolExecutor(threadNum);
    /**
     * 是否显示日志，全局
     */
    protected static boolean globalShowLog = true;
    /**
     * 是否显示日志,本次。当未设置showLog时，调用全局isShowLog
     */
    protected Boolean showLog;
    /**
     * 全局默认sessionId ,无法设置和更改
     */
    protected static String JSESSIONID;
    /**
     * 本次访问SessionId
     */
    protected String mySessionId;
    /**
     * URL地址
     */
    protected String url;
    /**
     * 连接超时
     */
    protected int connectTimeout = 1000 * 20;
    /**
     * 回调监听
     */
    protected YnetListener ynetListener;
    /**
     * 回调监听成功
     */
    protected YnetSuccessListener ynetSuccessListener;
    /**
     * 回调监听失败
     */
    protected YnetFailListener ynetFailListener;
    /**
     * 回调监听读取的比特数组
     */
    protected YnetBytesListener ynetBytesListener;
    /**
     * 回调监听读取的InputStream
     */
    protected YnetInputStreamListener ynetInputStreamListener;
    /**
     * session控制
     */
    protected YnetBackSessionListener ynetBackSessionListener;
    /**
     * 请求参数
     **/
    protected StringBuffer params = null;
    /**
     * Content-Type
     **/
    protected String contentType = "application/x-www-form-urlencoded;charset=utf-8";
    /**
     * 上传比特数组
     **/
    protected byte[] bytes = null;
    /**
     * 上传文件map
     **/
    protected Map<String, File> fileMap = null;
    /**
     * 请求 类型
     */
    protected UrlType urlType = null;
    /**
     * 标记，可用于标记本类的 作用，如：flag="bitmap",那么在继承时sendOther可通过此标记判断
     */
    protected String flag = null;

    /**
     * 请求类型枚举 @author 余静
     **/
    protected enum UrlType {
        /**
         * GET请求
         **/
        GET,
        /**
         * POST请求，发送的是文本
         **/
        POST_STRING,
        /**
         * POST请求，发送的是文件
         **/
        POST_FILE,
        /**
         * POST请求，发送的是图片
         **/
        POST_IMG,
        /**
         * POST请求，发送的是比特数组
         **/
        POST_BYTE,
        /**
         * POST请求，其他类型，自定义，重写的时候用
         **/
        POST_OTHER
    }

    //crt证书
    private String crtSSL;


    // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★静态方法列表★★★★★★★★★★★★★★★★★★★★★★★★★★★★

    /**
     * 快速创建Ynet对象
     *
     * @return Ynet
     */
    public static Ynet create() {
        return new Ynet();
    }

    /**
     * 标准get请求
     *
     * @param url          网络地址
     * @param ynetListener 回调监听
     */
    public static void get(String url, YnetListener ynetListener) {
        Ynet yNet = new Ynet();
        yNet.get(url);
        yNet.setYnetListener(ynetListener);
        yNet.start();
    }

    /**
     * 标准post请求
     *
     * @param url          网络地址
     * @param paramsMap    参数key，value
     * @param ynetListener 回调监听
     */
    public static void post(String url, Map<String, Object> paramsMap, YnetListener ynetListener) {
        Ynet yNet = new Ynet();
        yNet.post(url, paramsMap);
        yNet.setYnetListener(ynetListener);
        yNet.start();
    }

    /**
     * 发送文本请求post
     *
     * @param url          网络地址
     * @param str          文本
     * @param ynetListener 监听回调
     */
    public static void post(String url, String str, YnetListener ynetListener) {
        Ynet yNet = new Ynet();
        yNet.post(url, str);
        yNet.setYnetListener(ynetListener);
        yNet.start();
    }

    /**
     * @param url          网络地址
     * @param bytes        比特数组
     * @param ynetListener 监听回调
     */
    public static void post(String url, byte[] bytes, YnetListener ynetListener) {
        Ynet yNet = new Ynet();
        yNet.post(url, bytes);
        yNet.setYnetListener(ynetListener);
        yNet.start();
    }

    /**
     * 上传图片快捷静态方法post
     *
     * @param url          网络地址
     * @param paramsMap    参数key，value
     * @param fileMap      文件key，value
     * @param ynetListener 监听回调
     */
    public static void uploadImg(String url, Map<String, Object> paramsMap, Map<String, File> fileMap, YnetListener ynetListener) {
        Ynet yNet = new Ynet();
        yNet.uploadImg(url, paramsMap, fileMap);
        yNet.setTimeout(1000 * 30);
        yNet.setYnetListener(ynetListener);
        yNet.start();
    }

    /**
     * 上传文件快捷静态方法post
     *
     * @param url          网络地址
     * @param paramsMap    参数key，value
     * @param fileMap      文件key，value
     * @param ynetListener 监听回调
     */
    public static void uploadFile(String url, Map<String, Object> paramsMap, Map<String, File> fileMap, YnetListener ynetListener) {
        Ynet yNet = new Ynet();
        yNet.uploadFile(url, paramsMap, fileMap);
        yNet.setTimeout(1000 * 30);
        yNet.setYnetListener(ynetListener);
        yNet.start();
    }


    // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★静态方法列表结束★★★★★★★★★★★★★★★★★★★★★★★★★★★★

    /**
     * （初始化）标准 get请求
     *
     * @param url 网络地址
     * @return 返回this
     */
    public Ynet get(String url) {
        urlType = UrlType.GET;
        this.url = url;
        return this;
    }

    /**
     * （初始化）post请求发送文本
     *
     * @param url 网络请求地址
     * @param str 文本
     * @return 返回this
     */
    public Ynet post(String url, String str) {
        urlType = UrlType.POST_STRING;
        this.params = new StringBuffer(str);
        this.url = url;
        return this;
    }

    /**
     * （初始化）post请求发送比特数组
     *
     * @param url   网络请求地址
     * @param bytes 比特数组
     * @return 返回this
     */
    public Ynet post(String url, byte[] bytes) {
        urlType = UrlType.POST_BYTE;
        this.url = url;
        this.bytes = bytes;
        return this;
    }

    /**
     * （初始化）标准post请求发送键值对的参数
     *
     * @param url       网络请求地址
     * @param paramsMap key，value参数
     * @return 返回this
     */
    public Ynet post(String url, Map<String, Object> paramsMap) {
        return post(url, mapToParams(paramsMap).toString());
    }

    /**
     * （初始化）post上传图片
     *
     * @param url       网络请求地址
     * @param paramsMap 参数map对象
     * @param fileMap   文件map对象
     * @return 返回this
     */
    public Ynet uploadImg(String url, Map<String, Object> paramsMap, Map<String, File> fileMap) {
        urlType = UrlType.POST_IMG;
        this.url = url;
        this.params = multiMapToParams(paramsMap);
        this.fileMap = fileMap;
        return this;
    }

    /**
     * （初始化）post上传文件
     *
     * @param url       网络请求地址
     * @param paramsMap 参数map对象
     * @param fileMap   文件map对象
     * @return 返回this
     */
    public Ynet uploadFile(String url, Map<String, Object> paramsMap, Map<String, File> fileMap) {
        urlType = UrlType.POST_FILE;
        this.url = url;
        this.params = multiMapToParams(paramsMap);
        this.fileMap = fileMap;
        return this;
    }

    /**
     * 重写 start()方法，把本类加入线程池
     */
    public void start() {
        add(this);
    }

    /**
     * 停止当前队列中全部请求
     */
    public static void stopAll() {
        if (sTEP != null)
            sTEP.getQueue().clear();
    }

    /**
     * 把一个线程扔进线程池
     *
     * @param yNet yNet
     */
    public synchronized static void add(Thread yNet) {
        synchronized (sTEP) {
            if (sTEP.isShutdown()) {
                sTEP = new ScheduledThreadPoolExecutor(threadNum);
                synchronized (sTEP) {
                    sTEP.execute(yNet);
                }
            } else {
                sTEP.execute(yNet);
            }
        }
    }

    /**
     * 关闭释放线程池
     */
    public synchronized static void shutdown() {
        synchronized (sTEP) {
            if (!sTEP.isShutdown())
                sTEP.shutdown();
        }
    }

    /**
     * Map参数对象转成参数string
     *
     * @param paramsMap paramsMap
     * @return StringBuffer
     */
    protected StringBuffer mapToParams(Map<String, Object> paramsMap) {
        if (paramsMap == null) {
            return null;
        }
        StringBuffer params = new StringBuffer();
        for (Entry<String, Object> element : paramsMap.entrySet()) {
            if (element.getValue() == null)
                continue;
            params.append(element.getKey()).append("=").append(element.getValue()).append("&");
        }
        if (params.length() > 0)
            params.deleteCharAt(params.length() - 1);
        return params;
    }

    /**
     * 多文件上传Map参数对象转成参数string
     *
     * @param paramsMap paramsMap
     * @return StringBuffer
     */
    protected StringBuffer multiMapToParams(Map<String, Object> paramsMap) {
        if (paramsMap == null) {
            return null;
        }
        StringBuffer params = new StringBuffer(paramsMap.toString());
        StringBuilder strBuf = new StringBuilder();
        for (Entry<String, Object> entry : paramsMap.entrySet()) {
            if (entry.getValue() == null)
                continue;
            strBuf.append("\r\n--").append(BOUNDARY).append("\r\n");
            strBuf.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n\r\n").append(entry.getValue().toString());
        }
        return params;
    }

    /**
     * 创建HttpURLConnection对象，如果请求包含https就创建HttpsURLConnection,并且创建证书
     *
     * @param url    请求地址
     * @param crtSSL crt证书
     * @return HttpURLConnection
     * @throws Exception Exception
     */
    public static HttpURLConnection create(String url, String crtSSL) throws Exception {
        if (crtSSL != null && (url.toLowerCase().contains("https://"))) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) (new URL(url)).openConnection();
            httpsURLConnection.setSSLSocketFactory(createSSLSocketFactory(crtSSL));
            //屏蔽https验证
            httpsURLConnection.setHostnameVerifier((hostname, session) -> true);
            return httpsURLConnection;
        } else {
            return (HttpURLConnection) (new URL(url)).openConnection();
        }
    }

    /**
     * 创建SSL套接字工厂
     *
     * @param crtString crt证书
     * @return SSLSocketFactory
     * @throws Exception Exception
     */
    private static SSLSocketFactory createSSLSocketFactory(String crtString) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        //如果是src/main/assets/test.crt，直接获取InputStream：getAssets().open("test.crt")
        Certificate ca = cf.generateCertificate(new ByteArrayInputStream(crtString.getBytes()));

        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new SecureRandom());
        return context.getSocketFactory();
    }

    private Map<String, String> mapSetRequestProperty;
    private Map<String, String> mapAddRequestProperty;

    public Ynet setRequestProperty(String key, String value) {
        if (mapSetRequestProperty == null)
            mapSetRequestProperty = new HashMap<>();
        mapSetRequestProperty.put(key, value);
        return this;
    }

    @SuppressWarnings("StringOperationCanBeSimplified")
    public Ynet addRequestProperty(String key, String value) {
        //可以重复key的map，但是key的内存地址要不一样
        if (mapAddRequestProperty == null)
            mapAddRequestProperty = new IdentityHashMap<>();
        mapAddRequestProperty.put(new String(key), value);
        return this;
    }

    @Override
    public void run() {
        if (urlType == null) {
            return;
        }
        HttpURLConnection httpURLConnection = null;
        try {
            // 打开和URL之间的连接
            httpURLConnection = create(url, crtSSL);
            httpURLConnection.setConnectTimeout(connectTimeout);
            httpURLConnection.setReadTimeout(connectTimeout);// 读取数据超时连续X秒没有读取到数据直接判断超时，不影响正在传输的数据（如：下载）
            // 设置通用的请求属性
            httpURLConnection.setUseCaches(false); // 设置缓存
            httpURLConnection.setRequestProperty("accept", "*/*");
            httpURLConnection.setRequestProperty("connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "utf-8");
            httpURLConnection.setRequestProperty("Content-Type", contentType);// x-www-form-urlencoded可以换成json
            if (mapSetRequestProperty != null)
                for (Map.Entry<String, String> entry : mapSetRequestProperty.entrySet())
                    httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
            if (mapAddRequestProperty != null)
                for (Map.Entry<String, String> entry : mapAddRequestProperty.entrySet())
                    httpURLConnection.addRequestProperty(entry.getKey(), entry.getValue());
            setSession(httpURLConnection);// 设置session
            if (urlType == UrlType.GET) {
                httpURLConnection.setRequestMethod("GET");
            } else {
                httpURLConnection.setRequestMethod("POST");
                // 发送POST请求必须设置如下两行
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                if (urlType == UrlType.POST_STRING) {
                    sendString(httpURLConnection);
                } else if (urlType == UrlType.POST_BYTE) {
                    sendByte(httpURLConnection);
                } else if (urlType == UrlType.POST_FILE || urlType == UrlType.POST_IMG) {
                    sendFile(httpURLConnection);
                } else if (urlType == UrlType.POST_OTHER) {
                    sendOther(httpURLConnection);
                }
            }
            // ---------请求完毕---------------------------------------------------
            getSession(httpURLConnection);// 保存session
            // 根据ResponseCode判断连接是否成功
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                sendBackMsg("请求异常,状态码：" + responseCode, false);
                return;
            }
            // 定义BufferedReader输入流来读取URL的ResponseData
            InputStream inputStream = httpURLConnection.getInputStream();
            if (ynetInputStreamListener != null) {
                InputStream[] inputStreams = inputStreamCopy(inputStream, 2);// 复制两份inputStream
                inputStream = inputStreams[0];
                sendBackMsg(inputStreams[1]);
            }
            if (ynetBytesListener != null) {
                InputStream[] inputStreams = inputStreamCopy(inputStream, 2);// 复制两份inputStream
                inputStream = inputStreams[0];
                sendBackMsg(inputStreamToBytes(inputStreams[1]));
            }
            String value = inputStreamToString(inputStream);
            showLog(value);
            sendBackMsg(value, true);
        } catch (MalformedURLException e) {
            showLog(null);
            sendBackMsg("请求地址错误,或不符合URL规范", false);
            // e.printStackTrace();
        } catch (java.net.SocketTimeoutException e) {
            showLog(null);
            sendBackMsg("网络连接超时", false);
            // e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            showLog(null);
            sendBackMsg("不支持的编码", false);
            // e.printStackTrace();
        } catch (FileNotFoundException e) {
            showLog(null);
            sendBackMsg("找不到该地址：" + e.getMessage(), false);
            // e.printStackTrace();
        } catch (IOException e) {
            showLog(null);
            sendBackMsg("连接服务器失败:" + e.getMessage(), false);
            // e.printStackTrace();
        } catch (Exception e) {
            showLog(null);
            sendBackMsg("未知错误：" + e.getMessage(), false);
            // e.printStackTrace();
        } finally {
            shutdown();
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }
    }

    /**
     * inputStream输入流copy
     *
     * @param inputStream 输入流
     * @param copies      复制次数
     * @return 复制出来的流数组
     * @throws IOException IOException
     */
    public InputStream[] inputStreamCopy(InputStream inputStream, int copies) throws IOException {
        byte[] bytes = inputStreamToBytes(inputStream);
        InputStream[] inputs = new InputStream[copies];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = new ByteArrayInputStream(bytes);
        }
        return inputs;
    }

    /**
     * inputStream输入流读取成byte数组
     *
     * @param inputStream 输入流
     * @return byte数组
     * @throws IOException IOException
     */
    public byte[] inputStreamToBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        baos.flush();
        return baos.toByteArray();
    }

    /**
     * inputStream转String
     *
     * @param inputStream 输入流
     * @return 读取成文字
     * @throws IOException IOException
     */
    public String inputStreamToString(InputStream inputStream) throws IOException {
        return new String(inputStreamToBytes(inputStream), StandardCharsets.UTF_8);
    }

    /**
     * 发送string
     *
     * @param httpURLConnection httpURLConnection
     * @throws IOException IOException
     */
    protected void sendString(HttpURLConnection httpURLConnection) throws IOException {
        if (params == null || params.length() == 0)
            return;
        OutputStream output = httpURLConnection.getOutputStream();
        // 字符处理
        OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        // 获取URLConnection对象对应的输出流
        PrintWriter out = new PrintWriter(writer);
        // 发送请求参数
        out.write(params.toString());
        // flush输出流的缓冲
        out.flush();
        out.close();
    }

    /**
     * 发送sendByte
     *
     * @param httpURLConnection httpURLConnection
     * @throws IOException IOException
     */
    protected void sendByte(HttpURLConnection httpURLConnection) throws IOException {
        if (bytes == null || bytes.length == 0)
            return;
        // 获取写输入流
        OutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
        out.write(bytes);
        out.flush();
        out.close();
    }

    /**
     * 发送文件
     *
     * @param httpURLConnection httpURLConnection
     * @throws IOException IOException
     */
    protected void sendFile(HttpURLConnection httpURLConnection) throws IOException {
        //安卓6.0下使用谷歌浏览器67
        //Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.62 Mobile Safari/537.36
        //iPhoneX下使用谷歌浏览器67
        //Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) CriOS/56.0.2924.75 Mobile/14E5239e Safari/602.1
        //win10下使用谷歌浏览器67
        //Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.62 Safari/537.36
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.62 Safari/537.36");
        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        OutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
        // 文本参数
        if (this.params != null) {
            out.write(params.toString().getBytes());
        }
        // 文件
        if (fileMap != null) {
            for (Entry<String, File> entry : fileMap.entrySet()) {
                File file = entry.getValue();
                if (file == null)
                    continue;
                if (!file.exists()) {
                    System.err.println(file.getPath() + "(系统找不到指定的文件。)");
                    throw new FileNotFoundException(file.getPath() + "(系统找不到指定的文件。)");
                }
                StringBuilder strBuf = new StringBuilder();
                strBuf.append("\r\n--").append(BOUNDARY).append("\r\n");
                strBuf.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"; filename=\"").append(file.getName()).append("\"\r\n");
                if (urlType == UrlType.POST_IMG) {
                    if (file.getName().lastIndexOf(".png") != -1) {
                        strBuf.append("Content-Type:image/png" + "\r\n\r\n");
                    } else {
                        strBuf.append("Content-Type:image/jpeg" + "\r\n\r\n");
                    }
                } else if (urlType == UrlType.POST_FILE) {
                    strBuf.append("Content-Type:application/octet-stream" + "\r\n\r\n");
                }
                out.write(strBuf.toString().getBytes());
                DataInputStream in = new DataInputStream(new FileInputStream(file));
                int bytes;
                byte[] bufferOut = new byte[1024];
                while ((bytes = in.read(bufferOut)) != -1) {
                    out.write(bufferOut, 0, bytes);
                }
                in.close();
            }
        }
        out.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes());// 结束标记
        out.flush();
        out.close();
    }

    /**
     * 用于继承重写该方法
     *
     * @param httpURLConnection httpURLConnection
     * @throws IOException IOException
     */
    protected void sendOther(HttpURLConnection httpURLConnection) throws IOException {
    }

    protected void sendBackMsg(InputStream inputStream) throws IOException {
        if (ynetInputStreamListener != null) {
            ynetInputStreamListener.success(inputStream);
        }
    }

    protected void sendBackMsg(byte[] bytes) {
        if (ynetBytesListener != null) {
            ynetBytesListener.success(bytes);
        }
    }

    /**
     * 发回数据结果到消息队列
     *
     * @param value   接收到服务器返回的信息，如果success为false那么value就是获取数据失败的原因
     * @param success 访问服务器是否成功
     */
    protected void sendBackMsg(final String value, final boolean success) {
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
    }

    protected void showLog(final String value) {
        if (showLog == null) showLog = globalShowLog;
        if (showLog)
            System.out.println("请求地址：" + (urlType == UrlType.GET ? "Get--->" : "Post--->") + url + (params == null ? "" : ("\n请求参数：" + params.toString())) + "\n" + (value == null ? "请求失败" : ("请求结果：" + value)));
    }


    /**
     * 获取crtSSL证书
     *
     * @return String
     */
    public String getCrtSSL() {
        return crtSSL;
    }

    /**
     * 设置crtSSL证书
     *
     * @param crtSSL SSL证书
     * @return Ynet
     */
    public Ynet setCrtSSL(String crtSSL) {
        this.crtSSL = crtSSL;
        return this;
    }

    /**
     * 设置session，把JSESSIONID变量的值设置成session
     *
     * @param httpURLConnection httpURLConnection
     */
    public void setSession(HttpURLConnection httpURLConnection) {
        if (mySessionId != null) {
            httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + mySessionId);
        } else if (JSESSIONID != null) {
            httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + JSESSIONID);
        }
    }

    /**
     * 获取session 并保存到JSESSIONID变量
     *
     * @param httpURLConnection httpURLConnection
     */
    public void getSession(HttpURLConnection httpURLConnection) {
        Map<String, List<String>> map = httpURLConnection.getHeaderFields();
        if (map != null) {
            // Iterator<Map.Entry<String,List<String>>>entries=map.entrySet().iterator();while(entries.hasNext()){Map.Entry<String,List<String>>entry=entries.next();System.out.println(entry.getKey()+":"+entry.getValue());}
            List<String> list = map.get("Set-Cookie");
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    int start = list.get(i).indexOf("JSESSIONID");
                    if (start != -1) {
                        int idStart = start + 10 + 1;
                        int idEnd = start + 10 + 1 + 32;
                        if (list.get(i).length() >= idEnd) {
                            String JSESSIONID = list.get(i).substring(idStart, idEnd);// 如：list.get(i)="JSESSIONID=743D39694F006763220CA0CA63FE8978";
                            if (ynetBackSessionListener != null) {
                                ynetBackSessionListener.backSessionId(JSESSIONID);
                            }
                            Ynet.JSESSIONID = JSESSIONID;
                        }
                    }
                }
            }
        }
    }
    //------------------------------------接口------------------------------------

    /**
     * 请求成功监听
     *
     * @author 余静
     */
    public interface YnetSuccessListener {
        void success(String value);
    }

    /**
     * bytes监听
     * 直接返回请求的byte数据
     *
     * @author 余静 2018年7月24日10:53:01
     */
    public interface YnetBytesListener {
        void success(byte[] bytesValue);
    }

    /**
     * InputStream监听
     * 当拿到inputStream将其copy成两份，一份回调出去，一份继续走流程
     *
     * @author 余静 2018年7月24日10:53:01
     */
    public interface YnetInputStreamListener {
        void success(InputStream inputStreamValue) throws IOException;
    }

    /**
     * 请求失败监听
     *
     * @author 余静 2018年7月24日10:53:01
     */
    public interface YnetFailListener {
        void fail(String value);
    }

    /**
     * 请求信息监听
     *
     * @author 余静 2018年7月24日10:53:01
     */
    public interface YnetListener {
        void success(String value);

        void fail(String value);
    }

    /**
     * session监听
     *
     * @author 余静 2018年7月24日10:53:01
     */
    public interface YnetBackSessionListener {
        void backSessionId(String SessionId);
    }

    //------------------------------------GetAndSet------------------------------------

    /**
     * 释放当前线程池，并重新创建线程池一个最大值未threadNum的线程池
     *
     * @param threadNum 线程池最大值
     */
    public static void setThreadNum(int threadNum) {
        Ynet.threadNum = threadNum;
        shutdown();
        sTEP = new ScheduledThreadPoolExecutor(threadNum);
    }

    /**
     * 全局日志开关
     *
     * @param globalShowLog globalShowLog
     */
    public static void setGlobalShowLog(boolean globalShowLog) {
        Ynet.globalShowLog = globalShowLog;
    }

    public Ynet setYnetSuccessListener(YnetSuccessListener ynetSuccessListener) {
        this.ynetSuccessListener = ynetSuccessListener;
        return this;
    }

    public Ynet setYnetFailListener(YnetFailListener ynetFailListener) {
        this.ynetFailListener = ynetFailListener;
        return this;
    }

    public Ynet setYnetListener(YnetListener ynetListener) {
        this.ynetListener = ynetListener;
        return this;
    }

    public Ynet setYnetBackSessionListener(YnetBackSessionListener ynetBackSessionListener) {
        this.ynetBackSessionListener = ynetBackSessionListener;
        return this;
    }

    public Ynet setYnetBytesListener(YnetBytesListener ynetBytesListener) {
        this.ynetBytesListener = ynetBytesListener;
        return this;
    }

    public Ynet setYnetInputStreamListener(YnetInputStreamListener ynetInputStreamListener) {
        this.ynetInputStreamListener = ynetInputStreamListener;
        return this;
    }

    public Ynet setShowLog(Boolean showLog) {
        this.showLog = showLog;
        return this;
    }

    public Ynet setMySessionId(String mySessionId) {
        this.mySessionId = mySessionId;
        return this;
    }

    public Ynet setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 设置网络连接超时时间
     *
     * @param connectTimeout 毫秒
     * @return this
     */
    public Ynet setTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }
    //------------------------------------参考使用方法------------------------------------

    /**
     * 测试方法不要调用，如果不会本类，使用请参考此方法。
     */
    public static void test() {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("name", "yujing");
        paramsMap.put("content", "123456");

        Map<String, File> fileMap = new HashMap<>();
        fileMap.put("file", new File("D:/ab.jpg"));
        fileMap.put("file1", new File("D:/ac.jpg"));

        String url = "http://127.0.0.1:8080/yu/upload";
        Ynet ynet = new Ynet();
        ynet.uploadFile(url, paramsMap, fileMap);

        ynet.setYnetListener(new YnetListener() {
            @Override
            public void success(String value) {

            }

            @Override
            public void fail(String value) {

            }
        });

        ynet.setYnetBackSessionListener(SessionId -> System.out.println("服务器返回的SessionId：" + SessionId));
        ynet.start();
    }
}
