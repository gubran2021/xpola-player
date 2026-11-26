package com.example.xpolaplayer

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

private const val PLAYLIST_URL = "https://raw.githubusercontent.com/gubran2021/stream/refs/heads/master/ara.m3u"
private const val APP_PASSWORD = "1470"

data class Channel(val name: String, val group: String, val url: String)

class MainActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var player: ExoPlayer? = null
    private lateinit var root: LinearLayout
    private lateinit var listArea: LinearLayout
    private lateinit var status: TextView
    private var channels: List<Channel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(9,9,9)
        window.navigationBarColor = Color.rgb(9,9,9)
        showLogin()
    }

    private fun baseLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(22, 24, 22, 16)
            setBackgroundColor(Color.rgb(9,9,9))
            layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
        }
    }

    private fun label(text: String, size: Float = 18f): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 12)
        }

    private fun showLogin() {
        root = baseLayout()
        root.gravity = Gravity.CENTER
        root.addView(label("xPola Player", 30f))
        root.addView(label("مشغل القنوات والأفلام", 16f))
        val password = EditText(this).apply {
            hint = "أدخل كلمة المرور"
            inputType = 2 or 128
            setSingleLine(true)
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setBackgroundColor(Color.rgb(35,35,35))
        }
        root.addView(password, LinearLayout.LayoutParams(-1, 54).apply { setMargins(0, 22, 0, 14) })
        val enter = Button(this).apply { text = "دخول" }
        root.addView(enter, LinearLayout.LayoutParams(-1, 52))
        enter.setOnClickListener {
            if (password.text.toString() == APP_PASSWORD) showLibrary()
            else Toast.makeText(this, "كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show()
        }
        setContentView(root)
    }

    private fun showLibrary() {
        root = baseLayout()
        root.gravity = Gravity.TOP
        root.addView(label("xPola Player", 26f))
        status = label("جارٍ تحميل قائمة التشغيل…", 14f)
        root.addView(status)
        val playerView = PlayerView(this).apply {
            useController = true
            setBackgroundColor(Color.BLACK)
        }
        root.addView(playerView, LinearLayout.LayoutParams(-1, 220))
        player = ExoPlayer.Builder(this).build().also { playerView.player = it }
        val refresh = Button(this).apply { text = "تحديث القائمة" }
        root.addView(refresh, LinearLayout.LayoutParams(-1, 48))
        refresh.setOnClickListener { loadPlaylist() }
        val scroll = ScrollView(this)
        listArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(listArea)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        loadPlaylist()
    }

    private fun loadPlaylist() {
        status.text = "جارٍ الاتصال بمصدر القائمة…"
        listArea.removeAllViews()
        scope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    val conn = (URL(PLAYLIST_URL).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15000
                        readTimeout = 20000
                        requestMethod = "GET"
                        setRequestProperty("Cache-Control", "no-cache")
                    }
                    try { conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() } }
                    finally { conn.disconnect() }
                }
                channels = parseM3u(text)
                status.text = "تم تحميل ${channels.size} عنصر"
                renderChannels(channels)
            } catch (e: Exception) {
                status.text = "تعذر تحميل القائمة. تحقق من الإنترنت ثم حدّث القائمة."
            }
        }
    }

    private fun parseM3u(content: String): List<Channel> {
        val result = mutableListOf<Channel>()
        var name = "بدون اسم"
        var group = "عام"
        content.lineSequence().forEach { raw ->
            val line = raw.trim()
            if (line.startsWith("#EXTINF", true)) {
                name = line.substringAfter(",", "بدون اسم").trim().ifBlank { "بدون اسم" }
                group = Regex("""group-title="([^"]*)"""").find(line)?.groupValues?.get(1) ?: "عام"
            } else if (line.isNotEmpty() && !line.startsWith("#") &&
                (line.startsWith("http://") || line.startsWith("https://"))) {
                result.add(Channel(name, group, line))
                name = "بدون اسم"
                group = "عام"
            }
        }
        return result
    }

    private fun renderChannels(items: List<Channel>) {
        listArea.removeAllViews()
        if (items.isEmpty()) {
            listArea.addView(label("لم يتم العثور على قنوات في القائمة"))
            return
        }
        items.forEach { channel ->
            val button = Button(this).apply {
                text = "${channel.name}\n${channel.group}"
                textSize = 13f
                isAllCaps = false
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.rgb(34,34,34))
            }
            listArea.addView(button, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 5, 0, 5)
            })
            button.setOnClickListener {
                player?.setMediaItem(MediaItem.fromUri(channel.url))
                player?.prepare()
                player?.play()
                status.text = "يعمل الآن: ${channel.name}"
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        player?.release()
        player = null
        super.onDestroy()
    }
}
