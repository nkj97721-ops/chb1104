# Minimal Dark Browser

极简深色 Android 浏览器。

- 深色界面
- Google 搜索与网址直达
- 底部地址栏
- 刷新、前进、后退
- 网页缩放
- Android 10 及以上支持 WebView 算法暗化

## 获取 APK
打开仓库 Actions 页面，进入最新成功的 Android APK Build，下载 `MinimalBrowser-debug-apk`。

## 本地构建
需要 Android SDK Platform 35 和 JDK 17：

```bash
gradle assembleDebug
```

APK 路径：`app/build/outputs/apk/debug/app-debug.apk`
最低系统：Android 6.0（API 23）。
