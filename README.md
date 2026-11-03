# Minimal Browser

最小 Android WebView 浏览器：地址栏、网页搜索、前进/后退、刷新、主页、缩放。

## 自动构建 APK
进入 GitHub 仓库的 Actions 页面，等待 Android APK Build 工作流完成，在运行记录底部下载 `MinimalBrowser-debug-apk`。

## 本地构建
使用 Android Studio 打开项目，安装 Android SDK Platform 35，然后执行 `gradle assembleDebug`。

APK 输出位置：`app/build/outputs/apk/debug/app-debug.apk`。

最低 Android 版本：Android 6.0（API 23）。
