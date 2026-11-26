package com.shahid.iptv

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.KeyEvent
import android.graphics.Color
import android.widget.*
import android.text.InputType
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.net.URL

class MainActivity : Activity() {
    private val bg = Color.rgb(9, 9, 9)
    private val red = Color.rgb(229, 9, 20)
    private val white = Color.WHITE
    private var playlistUrl = ""
    private var player: ExoPlayer? = null
    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLogin()
    }

    private fun setupRoot() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(28, 22, 28, 22)
            isFocusableInTouchMode = true
        }
        setContentView(root)
    }

    private fun label(text: String, size: Float = 20f): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(white)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 10, 8, 10)
        }

    private fun action(text: String, onClick: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            isFocusable = true
            isFocusableInTouchMode = true
            setTextColor(white)
            setBackgroundColor(red)
            setOnClickListener { onClick() }
            textSize = 16f
        }

    private fun showLogin() {
        setupRoot()
        root.gravity = Gravity.CENTER
        root.addView(label("شاهد IPTV", 34f))
        root.addView(label("منصتك لمشاهدة القنوات والأفلام والمسلسلات", 16f))
        val password = EditText(this).apply {
            hint = "أدخل كلمة المرور"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            setTextColor(white)
            setHintTextColor(Color.LTGRAY)
            setSingleLine(true)
            textSize = 20f
            setPadding(18, 12, 18, 12)
            setBackgroundColor(Color.rgb(35, 35, 35))
            isFocusable = true
        }
        root.addView(password, LinearLayout.LayoutParams(360, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(action("دخول", {
            if (password.text.toString() == "1470") showHome()
            else Toast.makeText(this, "كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show()
        }))
        password.requestFocus()
    }

    private fun showHome() {
        setupRoot()
        val header = TextView(this).apply {
            text = "شاهد IPTV"
            textSize = 32f
            setTextColor(red)
            setPadding(8, 8, 8, 18)
        }
        root.addView(header)
        root.addView(label("مرحباً بك — اختر القسم", 18f))
        root.addView(action("📺 القنوات المباشرة", { showPlaylist("live") }))
        root.addView(action("🎬 الأفلام", { showPlaylist("movie") }))
        root.addView(action("📚 المسلسلات", { showPlaylist("series") }))
        root.addView(action("⚙ إعداد مصدر المحتوى", { showSettings() }))
        root.addView(label("أضف قائمة M3U تملك حق استخدامها أو مصدراً مرخصاً.", 14f))
    }

    private fun showSettings() {
        setupRoot()
        root.addView(label("إعداد مصدر المحتوى", 28f))
        root.addView(label("رابط قائمة M3U المرخصة", 16f))
        val input = EditText(this).apply {
            hint = "https://example.com/playlist.m3u"
            setText(playlistUrl)
            setTextColor(white)
            setHintTextColor(Color.LTGRAY)
            setSingleLine(true)
            setBackgroundColor(Color.rgb(35, 35, 35))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            isFocusable = true
        }
        root.addView(input)
        root.addView(action("حفظ الرابط", {
            val value = input.text.toString().trim()
            if (value.startsWith("https://")) {
                playlistUrl = value
                Toast.makeText(this, "تم حفظ الرابط لهذه الجلسة", Toast.LENGTH_SHORT).show()
                showHome()
            } else Toast.makeText(this, "استخدم رابط HTTPS صالحاً", Toast.LENGTH_SHORT).show()
        }))
        root.addView(action("رجوع", { showHome() }))
    }

    private fun showPlaylist(category: String) {
        if (playlistUrl.isBlank()) {
            Toast.makeText(this, "أضف رابط قائمة M3U من الإعدادات أولاً", Toast.LENGTH_LONG).show()
            showSettings()
            return
        }
        setupRoot()
        root.addView(label("المحتوى — $category", 26f))
        root.addView(label("جارٍ تحميل القائمة...", 16f))
        Thread {
            try {
                val lines = URL(playlistUrl).readText().lines()
                val items = mutableListOf<Pair<String, String>>()
                var name = "عنصر"
                for (line in lines) {
                    if (line.startsWith("#EXTINF", true)) {
                        name = line.substringAfterLast(",").trim().ifBlank { "عنصر" }
                    } else if (line.startsWith("https://") || line.startsWith("http://")) {
                        items.add(name to line.trim())
                    }
                }
                runOnUiThread {
                    root.removeAllViews()
                    root.addView(label("اختر ما تريد تشغيله", 24f))
                    if (items.isEmpty()) root.addView(label("لم يتم العثور على روابط تشغيل في القائمة."))
                    items.take(150).forEach { item ->
                        root.addView(action(item.first) { play(item.second) })
                    }
                    root.addView(action("رجوع", { showHome() }))
                }
            } catch (e: Exception) {
                runOnUiThread {
                    root.removeAllViews()
                    root.addView(label("تعذر تحميل القائمة. تحقق من الرابط والاتصال."))
                    root.addView(action("رجوع", { showHome() }))
                }
            }
        }.start()
    }

    private fun play(url: String) {
        setupRoot()
        root.addView(label("مشغل شاهد IPTV", 24f))
        val view = PlayerView(this).apply {
            useController = true
            player = ExoPlayer.Builder(this@MainActivity).build().also {
                this.player = it
                it.setMediaItem(MediaItem.fromUri(url))
                it.prepare()
                it.playWhenReady = true
            }
        }
        root.addView(view, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(action("رجوع للقائمة", { player?.release(); player = null; showHome() }))
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}
