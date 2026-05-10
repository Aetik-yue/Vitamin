# Vitamin

![GitHub stars](https://img.shields.io/github/stars/Aetik-yue/Vitamin?style=social)
![License](https://img.shields.io/github/license/Aetik-yue/Vitamin)
![Android](https://img.shields.io/badge/Android-API%2023%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Compose-7F52FF?logo=kotlin&logoColor=white)

Vitamin 是一款以 Markdown 阅读和轻量编辑为核心的 Android 应用。项目使用 Kotlin、Jetpack Compose、Material 3、Room、DataStore 和 Markwon 构建，支持本地文档导入、阅读进度保存、文档库、分类、搜索、收藏、深色模式和字体大小设置。

## 功能特性

- 通过 Android Storage Access Framework 导入本地 Markdown、TXT、DOC、DOCX 文档
- 使用 Markwon 渲染 Markdown，支持标题、列表、引用、代码块、链接、图片、表格、任务列表和删除线
- Compose UI + AndroidView 嵌入 TextView，兼顾现代 UI 和成熟 Markdown 渲染能力
- 文档库、最近打开、分类、收藏、重命名显示名和排序
- 当前文档内搜索，支持高亮、匹配数量、上一个/下一个和跳转
- 在线/本地编辑 Markdown 内容
- 保存阅读进度，下次打开自动恢复位置
- 中文默认界面，支持中英文切换
- 深色模式和字体大小设置

## 下载 APK

发布版本会归档在 `version/<版本号>/` 目录中。GitHub Release 会附带对应 APK。

当前版本：`v1.0.7`

## 构建方式

```powershell
cd "C:\Users\yanha\Desktop\My projects\Vitamin"
.\gradlew.bat assembleDebug --stacktrace
```

构建产物默认位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- AndroidView + TextView
- Markwon
- Room
- DataStore
- Kotlin Coroutines / StateFlow
- Gradle Kotlin DSL

## 版本归档

每次生成新 APK 后，应同时创建：

```text
version/<版本号>/Vitamin<版本号>.apk
version/<版本号>/CHANGELOG.md
```

只有生成新 APK 时才需要推送到 GitHub 并发布 Release。普通小改动可以留在本地，不必推送。

## License

This project is licensed under the MIT License.
