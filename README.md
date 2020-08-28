# ynet网络请求，支持java，kotlin，保存session
> * 1.支持get，post请求
> * 2.支持文件上传下载，回调进度条
> * 3.post请求可为字符串，map，byte[]
> * 4.可以直接返回字符串
> * 5.可以保存session
> * 6.异常回调原因
> * 7.支持https,设置ssl文件
> * 8.简单好用
采用java8.0，安卓10.0，API29，androidx。


## 当前最新版：————>[![](https://jitpack.io/v/yutils/ynet.svg)](https://jitpack.io/#yutils/ynet)

**[releases里面有JAR包。点击前往](https://github.com/yutils/ynet/releases)**

## Gradle 引用

1. 在根build.gradle中添加
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. 子module添加依赖，当前最新版：————>[![](https://jitpack.io/v/yutils/ynet.svg)](https://jitpack.io/#yutils/ynet)

```
dependencies {
     implementation 'com.github.yutils:ynet:1.0.3'
}
```

##  用法：
  1.Ynet返回结果在子线程，适合java工程
  
  2.安卓请把Ynet替换成YnetAndroid，以为Ynet返回结果在子线程，YnetAndroid返回在主线程

<font color=#0099ff size=4 >GET</font>
``` java
  String url = "http://127.0.0.1:8080/yu";
  Ynet.get(url, new ynetListener);

```

<font color=#0099ff size=4 >POST JAVA</font>
``` java
  Map<String, Object> paramsMap = new HashMap<String, Object>();
  paramsMap.put("name", "yujing");
  paramsMap.put("content", "123456");
  String url = "http://127.0.0.1:8080/yu";
  Ynet.post(url, paramsMap, new ynetListener);

```

<font color=#0099ff size=4 >POST kotlin</font>
``` kotlin
var url = "http://127.0.0.1:12345/api"
//参数
val p1 =  HashMap<String, Any>()
p1["key1"] = "value1"
//请求
YnetAndroid.post(url, p1, object : Ynet.YnetListener {
    override fun success(value: String?) {
        //成功返回结果
    }
    override fun fail(value: String?) {
        //失败返回原因
    }
})
```

<font color=#0099ff size=4 >上传文件</font>
``` java
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

```
<font color=#0099ff size=4 >下载文件</font>
``` java
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

```

## 注意添加权限：
> * 必须权限  android.permission.INTERNET


Github地址：[https://github.com/yutils/ynet](https://github.com/yutils/ynet)

我的CSDN：[https://blog.csdn.net/Yu1441](https://blog.csdn.net/Yu1441)

感谢关注微博：[细雨若静](https://weibo.com/32005200)