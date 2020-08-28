package com.yujing.net.dowload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 文件下载
 * 已过时，建议使用YHttp.create().downloadFile();
 * @author YuJing 2017年6月2日16:33:34
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Deprecated
public class YDownload extends Thread {
    private int fileSize = 0; // 文件总大小
    private int backTime = 200;// 回调时间
    private String url;// 下载文件的地址
    private int timeOut = 1000 * 10;// 连接超时时间
    private File file;// 文件
    private boolean isRun = false;
    private FileDownloadThread downloadThread;// 下载线程
    private YDownloadListener downloadListener;// 接口，用于回调
    private static int sTPEThreadNum = 10; // 线程数量
    private static ScheduledThreadPoolExecutor sTPE = new ScheduledThreadPoolExecutor(sTPEThreadNum);

    public YDownload(String url, String path, String fileName) {
        this.url = url;
        file = new File(path);
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
        }
        file = new File(path, fileName);
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();// 删除存在文件
        }
    }

    public YDownload(String url, File file) {
        this.url = url;
        this.file = file;
        File parent = file.getParentFile();
        if (!Objects.requireNonNull(parent).exists()) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();// 删除存在文件
        }
    }

    // 快捷设置方法
    public static YDownload Download(String url, File file, YDownloadListener downloadListener) {
        YDownload yDownloadFile = new YDownload(url, file);
        yDownloadFile.setDownloadListener(downloadListener);
        yDownloadFile.setTimeOut(1000 * 10);// 设置连接超时
        yDownloadFile.setBackTime(200);// 设置回调时间
        yDownloadFile.start();// 开始下载，把下载线程扔入线程队列
        return yDownloadFile;
    }

    // 快捷设置方法
    public static YDownload Download(String url, String path, String fileName, YDownloadListener downloadListener) {
        YDownload yDownloadFile = new YDownload(url, path, fileName);
        yDownloadFile.setDownloadListener(downloadListener);
        yDownloadFile.setTimeOut(10000);// 设置连接超时
        yDownloadFile.setBackTime(200);// 设置回调时间
        yDownloadFile.start();// 开始下载，把下载线程扔入线程队列
        return yDownloadFile;
    }

    @Override
    public synchronized void start() {
        add(this);// super.start();
    }

    // 把一个线程扔进线程池
    public synchronized static void add(Thread thread) {
        synchronized (sTPE) {
            if (sTPE.isShutdown()) {
                sTPE = new ScheduledThreadPoolExecutor(sTPEThreadNum);
                synchronized (sTPE) {
                    sTPE.execute(thread);
                }
            } else {
                sTPE.execute(thread);
            }
        }
    }

    // 关闭释放线程池
    public synchronized static void shutdown() {
        synchronized (sTPE) {
            if (!sTPE.isShutdown()) {
                sTPE.shutdown();
            }
        }
    }

    public void stopDownLoad() {
        isRun = false;
        if (downloadThread != null) {
            downloadThread.stopDownLoad();
        }
    }

    /**
     * 停止当前队列中全部请求
     */
    public static void stopAll() {
        sTPE.getQueue().clear();
    }

    public int getTimeOut() {
        return timeOut;
    }

    // 设置连接超时时间
    public YDownload setTimeOut(int timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    public int getBackTime() {
        return backTime;
    }

    // 设置回调时间
    public YDownload setBackTime(int backTime) {
        this.backTime = backTime;
        return this;
    }

    @Override
    public void run() {
        isRun = true;
        try {
            URL urlConn = new URL(url);
            URLConnection conn = urlConn.openConnection();
            // 获取下载文件的总大小
            fileSize = conn.getContentLength(); // 获取不到文件大小时候fileSize=-1

            downloadThread = new FileDownloadThread(urlConn, file, timeOut);
            downloadThread.start();
            // 回调进度
            if (downloadListener != null) {
                while (!downloadThread.isFinished() && isRun) {
                    if (fileSize == -1) {// 获取不到文件大小时候fileSize=0，进度为0
                        downloadListener.response(downloadThread.getDownloadSize(), fileSize, 0, false, file);
                    } else {
                        double progress = downloadThread.getDownloadSize() * 1.0 / fileSize * 100;
                        int x = (int) Math.pow(10, 2);// 保留2位小数
                        progress = (double) ((int) (progress * x)) / x;
                        downloadListener.response(downloadThread.getDownloadSize(), fileSize, progress, false, file);
                    }
                    sleep(backTime);// 休息一下后再读取下载进度
                }
                downloadListener.response(downloadThread.getDownloadSize(), fileSize, 100, false, file);
            }
        } catch (Exception e) {
            stopDownLoad();// 停止下载
            if (downloadListener != null) {
                downloadListener.response(downloadThread.getDownloadSize(), fileSize, 0, true, file);
            }
            e.printStackTrace();
        } finally {
            isRun = false;
            shutdown();
        }
    }

    public interface YDownloadListener {
        /**
         * @param downloadSize 当前下载大小
         * @param fileSize     文件总大小
         * @param progress     下载进度
         * @param isFail       是否失败
         * @param file         下载的文件
         */
        void response(int downloadSize, int fileSize, double progress, boolean isFail, File file);
    }

    public void setDownloadListener(YDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    /**
     * @author YuJing
     * 下载类，每一个线程开启一个该类 2016-1-10 下午12:13:55
     */
    @SuppressWarnings("WeakerAccess")
    public class FileDownloadThread extends Thread {
        private static final int BUFFER_SIZE = 1024 * 8;
        private URL mUrl;
        private File file;
        // 用于标识当前线程是否下载完成
        private boolean finished = false;
        private boolean isDownload = false;
        private int timeOut;
        private int downloadSize = 0;

        public FileDownloadThread(URL urlConn, File file, int timeOut) {
            this.mUrl = urlConn;
            this.file = file;
            this.timeOut = timeOut;
        }

        @Override
        public void run() {
            if (!isDownload) {
                isDownload = true;
                BufferedInputStream bis;
                RandomAccessFile fos;
                byte[] buf = new byte[BUFFER_SIZE];
                URLConnection con;
                try {
                    con = mUrl.openConnection();
                    con.setConnectTimeout(timeOut);
                    con.setAllowUserInteraction(true);
                    // 使用java中的RandomAccessFile 对文件进行随机读写操作
                    fos = new RandomAccessFile(file, "rw");
                    bis = new BufferedInputStream(con.getInputStream());
                    // 开始循环以流的形式读写文件
                    while (isDownload) {
                        int len = bis.read(buf, 0, BUFFER_SIZE);
                        if (len == -1) {
                            break;
                        }
                        fos.write(buf, 0, len);
                        downloadSize += len;
                    }
                    // 下载完成设为true
                    this.finished = true;
                    bis.close();
                    fos.close();
                } catch (final IOException e) {
                    stopDownLoad();// 停止下载
                    e.printStackTrace();
                }
            }
        }

        public int getDownloadSize() {
            return downloadSize;
        }

        public boolean isFinished() {
            return finished;
        }

        public void stopDownLoad() {
            isDownload = false;
        }
    }

    public void println(String str) {
        System.out.println(str);
    }

    public static void TEST() {
        String url = "http://dldir1.qq.com/qqfile/qq/QQ8.9.2/20760/QQ8.9.2.exe";
        final String path = "D:/" + "yu/";
        String fileName = "QQ8.9.2.exe";
        YDownload.Download(url, path, fileName, (downloadedSize, fileSize, progress, isfail, file) -> {
            if (isfail) {
                System.out.println("下载出错");
                return;
            }
            if (progress == 100) {// 进度
                System.out.println("下载完成:" + downloadedSize + "大小：" + fileSize + "百分比：" + progress);
            } else {
                System.out.println("下载进度:" + downloadedSize + "大小：" + fileSize + "百分比：" + progress);
            }
        });
    }
}
