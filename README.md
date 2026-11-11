# VoiceMuse

VoiceMuse 是一款面向 Android 的实时变声器项目，当前处于早期开发与设备验证阶段。

## 当前状态

**开发阶段：v0.3 工程初始化。** 当前提交是 Android Studio 工程起点与基础中文控制面板，不是完整变声器，也不代表已经完成 K70 Pro 真机验证。

| 能力 | 状态 |
|---|---|
| Android 工程骨架 | 初始化中 |
| 中文状态面板 | 初始实现 |
| 音频采集与处理 | 待接入 |
| Oboe/C++ 实时引擎 | 待实现与验证 |
| LSPosed 目标应用适配 | 待逐应用实现与验证 |
| 蓝牙/有线耳机兼容 | 待真机测试 |

## 构建

使用 Android Studio 打开仓库根目录，等待 Gradle Sync 完成后运行 app 配置。JDK 17、Android Gradle Plugin 8.7.3、Kotlin 2.0.21、compileSdk 35、minSdk 26。

## 安全与兼容性

- 默认不 Hook system_server，不全局 Hook 所有应用。
- 默认不启用目标应用作用域；由用户明确选择目标应用。
- Java AudioRecord、AAudio、OpenSL ES 及应用专属音频路径需要分别测试，不承诺通用注入兼容性。
- 音频权限仅在用户主动启用相关功能时申请。
- 不用于录制或处理未经授权的他人音频。

## 设备验证目标

首要测试设备为 Redmi K70 Pro / Android 16 / 官方澎湃 OS，配合用户已安装的 KernelSU 与 LSPosed 环境。设备版本、内核和具体注入状态需以实际读取结果为准。

## 许可证

暂未指定。