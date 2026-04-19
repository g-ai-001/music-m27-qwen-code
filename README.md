# music-m27-qwen-code

本地音乐播放器 - 一个纯离线的Android音乐播放应用

## 功能特性

- 🎵 **本地音乐扫描**: 自动扫描并导入设备上的本地音频文件
- 🎤 **歌词显示**: 支持LRC格式歌词自动匹配和显示
- 📋 **歌单管理**: 创建和管理个人播放列表
- ❤️ **收藏功能**: 快速收藏喜欢的歌曲
- 🎨 **现代化UI**: 采用Jetpack Compose构建的现代化界面设计
- 🎛️ **多种播放模式**: 封面模式和歌词模式自由切换
- 📱 **迷你播放器**: 全局显示当前播放状态

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构**: MVVM + Clean Architecture
- **音频引擎**: Media3 ExoPlayer
- **数据库**: Room
- **依赖注入**: Hilt
- **目标平台**: Android API 26+

## 版本历史

### v0.1.0
- 项目基础框架搭建
- 本地音乐扫描功能
- 音乐播放服务
- 首页界面 (推荐、歌单、歌手、专辑)
- 播放详情页 (封面模式 & 歌词模式)
- "我的"页面
- 迷你播放器
- 收藏与歌单管理
- 歌词解析
- 日志系统

## 构建

项目使用 GitHub Actions 进行自动构建。推送 Tag 后会自动触发构建流程。

```bash
# 克隆仓库
git clone git@github.com:g-ai-001/music-m27-qwen-code.git

# 本地构建 (需要Android SDK)
./gradlew assembleDebug
```

## 隐私

本应用完全离线运行，不需要任何网络权限，保护您的隐私安全。
