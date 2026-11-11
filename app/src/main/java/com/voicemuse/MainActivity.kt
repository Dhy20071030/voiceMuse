package com.voicemuse

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private val bg = Color.rgb(14, 18, 29)
    private val panel = Color.rgb(27, 34, 49)
    private val muted = Color.rgb(166, 176, 195)
    private val accent = Color.rgb(164, 143, 255)

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
        root.addView(label("声韵 Pro · v0.3 开发预览", 14, muted, false).apply {
            setPadding(0, dp(4), 0, dp(18))
        })

        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(label("运行状态", 19, Color.WHITE, true).apply { setPadding(0, dp(8), 0, dp(10)) })
        body.addView(card("Root / KernelSU", "尚未读取 · 待接入状态检测"))
        body.addView(card("LSPosed 框架", "尚未读取 · 待接入状态检测"))
        body.addView(card("目标应用进程注入", "未验证 · 不代表注入成功"))
        body.addView(card("录音 Hook", "未验证 · 需按应用单独测试"))
        body.addView(card("音频引擎", "尚未启动 · Oboe 引擎待接入"))
        body.addView(label("音色工作台", 19, Color.WHITE, true).apply { setPadding(0, dp(18), 0, dp(10)) })
        body.addView(card("普通模式", "萝莉音 · 甜妹音 · 少女音 · 御姐音"))
        body.addView(card("专业模式", "音高 · 共振峰 · EQ · 压缩 · 降噪"))
        body.addView(label("说明：当前仅为工程初始化界面。Root、LSPosed、进程注入与录音 Hook 均未实现读取，所有状态以实际检测结果为准。", 12, muted, false).apply {
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
