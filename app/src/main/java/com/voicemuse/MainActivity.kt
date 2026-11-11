package com.voicemuse

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.os.Build
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private val bg = Color.rgb(14, 18, 29)
    private val panel = Color.rgb(27, 34, 49)
    private val muted = Color.rgb(166, 176, 195)
    private val accent = Color.rgb(164, 143, 255)
    private lateinit var engineStatus: TextView
    private lateinit var routeStatus: TextView
    private lateinit var body: LinearLayout
    private var selectedOutputId = -1
    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(12))
            setBackgroundColor(bg)
        }
        root.addView(label("VoiceMuse", 28, Color.WHITE, true))
        root.addView(label("声韵 Pro · v0.4.1 音频路由测试", 14, muted, false).pad(0, 4, 0, 18))
        body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(label("运行状态", 19, Color.WHITE, true).pad(0, 8, 0, 10))
        body.addView(card("Root / KernelSU", "尚未读取 · 待接入状态检测"))
        body.addView(card("LSPosed 框架", "尚未读取 · 待接入状态检测"))
        body.addView(card("目标应用进程注入", "未验证 · 不代表注入成功"))
        body.addView(card("录音 Hook", "未验证 · 需按应用单独测试"))
        engineStatus = label("音频引擎：尚未启动", 14, accent, true).pad(4, 14, 4, 8)
        body.addView(engineStatus)
        body.addView(Button(this).apply { text = "授权麦克风并启动监听"; setOnClickListener { startEngine() } })
        body.addView(Button(this).apply { text = "停止监听"; setOnClickListener { stopEngine() } })
        body.addView(label("输出设备测试", 19, Color.WHITE, true).pad(0, 20, 0, 8))
        routeStatus = label("请选择输出设备；扬声器测试请调低音量。", 12, muted, false).pad(4, 4, 4, 8)
        body.addView(routeStatus)
        body.addView(Button(this).apply { text = "刷新可用输出设备"; setOnClickListener { refreshOutputDevices() } })
        body.addView(label("可用输出设备", 14, Color.WHITE, true).pad(4, 8, 4, 6))
        refreshOutputDevices()
        body.addView(label("音频诊断", 19, Color.WHITE, true).pad(0, 20, 0, 8))
        body.addView(Button(this).apply { text = "读取引擎统计"; setOnClickListener { showEngineStats() } })
        body.addView(label("音色工作台", 19, Color.WHITE, true).pad(0, 20, 0, 8))
        body.addView(card("普通模式", "萝莉音 · 甜妹音 · 少女音 · 御姐音（待实现）"))
        body.addView(card("专业模式", "音高 · 共振峰 · EQ · 压缩 · 降噪（待实现）"))
        body.addView(label("Root、LSPosed、目标进程注入与录音 Hook 状态尚未实现读取，必须分别验证。当前输出设备选择仅控制 VoiceMuse 自身 Oboe 输出流，不会改变其他应用的麦克风。", 12, muted, false).pad(4, 16, 4, 18))
        val scroll = ScrollView(this).apply { addView(body) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { gravity = Gravity.CENTER }
        listOf("首页", "音色", "应用", "日志", "设置").forEach { name ->
            nav.addView(TextView(this).apply {
                text = name; textSize = 12f; gravity = Gravity.CENTER
                setTextColor(if (name == "首页") accent else muted)
                setPadding(dp(7), dp(12), dp(7), dp(8))
            }, LinearLayout.LayoutParams(0, dp(44), 1f))
        }
        root.addView(nav)
        setContentView(root)
    }

    private fun refreshOutputDevices() {
        if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1002)
        }
        try {
            val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).filter { isSupportedOutput(it) }
            if (outputs.isEmpty()) body.addView(label("系统暂未报告可用输出设备。", 12, muted, false))
            outputs.forEach { device ->
                val text = deviceKind(device) + " · " + device.productName + "（ID " + device.id + "）"
                body.addView(Button(this).apply {
                    this.text = text
                    setOnClickListener {
                        selectedOutputId = device.id
                        routeStatus.text = "已选择：" + deviceKind(device) + "。正在重启引擎应用路由。"
                        stopEngine()
                        startEngine()
                    }
                })
            }
        } catch (_: SecurityException) {
            body.addView(label("读取蓝牙设备需要授予“附近设备”权限；可先测试扬声器和有线耳机。", 12, muted, false))
        } catch (failure: Throwable) {
            body.addView(label("设备枚举失败：" + failure.javaClass.simpleName, 12, muted, false))
        }
    }

    private fun isSupportedOutput(d: AudioDeviceInfo) = when (d.type) {
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_USB_HEADSET,
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
        AudioDeviceInfo.TYPE_BLE_HEADSET, AudioDeviceInfo.TYPE_BLE_SPEAKER -> true
        else -> false
    }

    private fun deviceKind(d: AudioDeviceInfo) = when (d.type) {
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "手机扬声器"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_WIRED_HEADSET -> "有线耳机"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "USB 耳机"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
        AudioDeviceInfo.TYPE_BLE_HEADSET, AudioDeviceInfo.TYPE_BLE_SPEAKER -> "蓝牙设备"
        else -> "其他设备"
    }

    private fun startEngine() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1001); return
        }
        try {
            val started = NativeAudioEngine.nativeStart(selectedOutputId)
            engineStatus.text = if (started) "音频引擎：已启动（基础回放测试）" else "音频引擎：启动失败，已尝试系统默认输出"
            if (started) showEngineStats()
        } catch (failure: Throwable) {
            engineStatus.text = "音频引擎：加载失败（" + failure.javaClass.simpleName + "）"
        }
    }

    private fun stopEngine() {
        try { NativeAudioEngine.nativeStop(); engineStatus.text = "音频引擎：已停止" }
        catch (failure: Throwable) { engineStatus.text = "音频引擎：停止异常（" + failure.javaClass.simpleName + "）" }
    }

    private fun showEngineStats() {
        try { routeStatus.text = "引擎数据：" + NativeAudioEngine.nativeGetStats() }
        catch (failure: Throwable) { routeStatus.text = "统计读取失败：" + failure.javaClass.simpleName }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startEngine()
            else engineStatus.text = "音频引擎：未获得麦克风权限"
        } else if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            refreshOutputDevices()
        }
    }
    override fun onDestroy() { stopEngine(); super.onDestroy() }

    private fun card(title: String, detail: String) = TextView(this).apply {
        text = title + "\n" + detail; textSize = 14f; setTextColor(Color.WHITE)
        setLineSpacing(dp(4).toFloat(), 1f); setPadding(dp(15), dp(14), dp(15), dp(14))
        setBackgroundColor(panel)
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) }
    }
    private fun label(value: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        text = value; textSize = size.toFloat(); setTextColor(color)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }
    private fun TextView.pad(l: Int, t: Int, r: Int, b: Int) = apply { setPadding(dp(l), dp(t), dp(r), dp(b)) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
