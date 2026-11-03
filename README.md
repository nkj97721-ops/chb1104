# Minimal Browser

深色模式 Android WebView 浏览器。

## 功能
- 深色工具栏与地址栏
- 网址访问与 Google 搜索
- 前进、后退、主页、刷新
- 页面缩放
- Android 10 及以上尝试启用网页算法暗色化

## 获取 APK
打开仓库的 **Actions** 页面，进入最新的 **Android APK Build** 运行记录，在 Artifacts 下载 `MinimalBrowser-debug-apk`。

## 本地构建
使用 Android Studio 打开项目，安装 Android SDK Platform 35，然后执行：

```bash
gradle assembleDebug
```

APK 输出：`app/build/outputs/apk/debug/app-debug.apk`。
最低系统：Android 6.0（API 23）。
