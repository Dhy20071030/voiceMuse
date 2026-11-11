package com.voicemuse

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
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
        root.addView(label("声韵 Pro · v0.4 音频引擎预览", 14, muted, false).apply {
            setPadding(0, dp(4), 0, dp(18))
        })

        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(label("运行状态", 19, Color.WHITE, true).apply { setPadding(0, dp(8), 0, dp(10)) })
        body.addView(card("Root / KernelSU", "尚未读取 · 待接入状态检测"))
        body.addView(card("LSPosed 框架", "尚未读取 · 待接入状态检测"))
        body.addView(card("目标应用进程注入", "未验证 · 不代表注入成功"))
        body.addView(card("录音 Hook", "未验证 · 需按应用单独测试"))
        engineStatus = label("音频引擎：尚未启动", 14, accent, true)
        body.addView(engineStatus.apply { setPadding(dp(4), dp(14), dp(4), dp(8)) })
        body.addView(Button(this).apply {
            text = "授权麦克风并启动监听"
            setOnClickListener { startEngine() }
        })
        body.addView(Button(this).apply {
            text = "停止监听"
            setOnClickListener { stopEngine() }
        })
        body.addView(label("请连接耳机后再测试，避免扬声器回放造成声学啸叫。当前引擎仅做麦克风到耳机的基础回放，不包含变声算法。", 12, muted, false).apply {
            setPadding(dp(4), dp(10), dp(4), dp(16))
        })
        body.addView(label("音色工作台", 19, Color.WHITE, true).apply { setPadding(0, dp(18), 0, dp(10)) })
        body.addView(card("普通模式", "萝莉音 · 甜妹音 · 少女音 · 御姐音（界面规划）"))
        body.addView(card("专业模式", "音高 · 共振峰 · EQ · 压缩 · 降噪（界面规划）"))
        body.addView(label("Root、LSPosed、目标进程注入与录音 Hook 状态尚未实现读取，必须分别验证。", 12, muted, false).apply {
            setPadding(dp(4), dp(16), dp(4), dp(18))
        })

        val scroll = ScrollView(this).apply { addView(body) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { gravity = Gravity.CENTER }
        listOf("首页", "音色", "应用", "日志", "设置").forEach { name ->
            nav.addView(TextView(this).apply {
                text = name
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(if (name == "首页") accent else muted)
                setPadding(dp(7), dp(12), dp(7), dp(8))
            }, LinearLayout.LayoutParams(0, dp(44), 1f))
        }
        root.addView(nav)
        setContentView(root)
    }

    private fun startEngine() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1001)
            return
        }
        try {
            val started = NativeAudioEngine.nativeStart()
            engineStatus.text = if (started) "音频引擎：已启动（基础回放测试）" else "音频引擎：启动失败，请查看设备兼容性"
        } catch (failure: Throwable) {
            engineStatus.text = "音频引擎：加载失败（" + failure.javaClass.simpleName + "）"
        }
    }

    private fun stopEngine() {
        try {
            NativeAudioEngine.nativeStop()
            engineStatus.text = "音频引擎：已停止"
        } catch (failure: Throwable) {
            engineStatus.text = "音频引擎：停止异常（" + failure.javaClass.simpleName + "）"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startEngine()
            else engineStatus.text = "音频引擎：未获得麦克风权限"
        }
    }

    override fun onDestroy() {
        stopEngine()
        super.onDestroy()
    }

    private fun card(title: String, detail: String): TextView = TextView(this).apply {
        text = "$title\n$detail"
        textSize = 14f
        setTextColor(Color.WHITE)
        setLineSpacing(dp(4).toFloat(), 1f)
        setPadding(dp(15), dp(14), dp(15), dp(14))
        setBackgroundColor(panel)
        val p = LinearLayout.LayoutParams(-1, -2)
        p.bottomMargin = dp(9)
        layoutParams = p
    }

    private fun label(value: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(color)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
