# VoiceMuse

VoiceMuse 是一款面向 Android 的实时变声器项目，当前处于早期开发与设备验证阶段。

## 当前版本：v0.4.0（音频引擎接入阶段）

本版本新增 Oboe 依赖、CMake/JNI 桥接、麦克风运行时权限流程，以及输入/输出音频流的基础回放桥接代码。

**重要：** 该版本尚未完成 Android Studio/Gradle 编译，也未在 Redmi K70 Pro 真机验证。当前 Native 音频路径仅用于验证麦克风采集到耳机输出的基础链路，不含变声 DSP；不能视为完整实时变声器。建议先连接耳机测试，避免扬声器回放引发啸叫。

## 功能状态

| 能力 | 状态 |
|---|---|
| Android 工程骨架与中文界面 | 已提交初始实现 |
| Oboe 1.9.3 / CMake / JNI 接入 | 已提交源码，待编译验证 |
| 麦克风运行时权限 | 已接入源码，待设备验证 |
| 输入到输出基础音频桥接 | 已提交初始实现，待真机验证 |
| 音高/共振峰变换 | 未实现 |
| EQ/压缩/降噪 | 未实现 |
| KernelSU/LSPosed 状态读取 | 未实现 |
| LSPosed 目标应用适配 | 未实现 |
| 蓝牙/有线耳机兼容性 | 待真机测试 |

## 构建环境

- JDK 17
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21
- compileSdk 35
- minSdk 26
- CMake 3.22.1
- Oboe 1.9.3

使用 Android Studio 打开仓库根目录并同步 Gradle。若本机 Android SDK 尚未安装 CMake 3.22.1，请通过 SDK Manager 安装对应版本。

## 安全与兼容性

- 默认不 Hook system_server，不全局 Hook 所有应用。
- 默认不启用目标应用作用域；由用户明确选择目标应用。
- Java AudioRecord、AAudio、OpenSL ES 及应用专属音频路径需要分别测试，不承诺通用注入兼容性。
- 音频权限仅在用户主动点击启动监听时申请。
- 不用于录制或处理未经授权的他人音频。

## 设备验证目标

首要测试设备为 Redmi K70 Pro / Android 16 / 官方澎湃 OS，配合用户已安装的 KernelSU 与 LSPosed 环境。设备版本、内核和具体注入状态需以实际读取结果为准。

## 后续计划

1. 在 Android Studio 完成 Gradle Sync 与 CMake 编译，修复设备相关问题。
2. 验证有线耳机和蓝牙耳机的采集、输出、延迟与稳定性。
3. 加入音高、共振峰与 EQ 等 DSP 处理，并做 A/B 试听。
4. 单独实现并验证 LSPosed 目标应用录音路径适配。

## 许可证

暂未指定。
