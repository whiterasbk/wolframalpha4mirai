package whiter.bot

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Wolframalpha : KotlinPlugin(
    JvmPluginDescription(
        id = "whiter.bot.wolframalpha",
        version = "1.0-SNAPSHOT",
    )
) {
    lateinit var appid: String

    override fun onEnable() {
        logger.info { "WolframAlpha Plugin loaded" }
        Config.reload()
        appid = Config.appid
        if (appid.isEmpty()) throw Exception("your appid can not be empty")
        val slice = if (Config.prefix == "") "''" else Config.prefix

        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            if (message.contentToString().startsWith(slice)) {
                val msg = message.contentToString()
                group.sendMessage(query(msg.slice(slice.length until msg.length), group))
            }
        }

        globalEventChannel().subscribeAlways<FriendMessageEvent> {
            if (message.contentToString().startsWith(slice)) {
                val msg = message.contentToString()
                sender.sendMessage(query(msg.slice(slice.length until msg.length), sender))
            }
        }
    }

    /**
     * 进行wolfram|α查询
     * @param str 查询字符串
     * @param subject 发送对象
     * @return 消息链，总是返回非空消息
     */
    suspend fun query(str: String, subject: Contact): MessageChain {
        var msg: MessageChain = EmptyMessageChain
        val query = URLEncoder.encode(str, "utf-8")
        val url = "http://api.wolframalpha.com/v2/query?appid=$appid&input=$query&output=json"
        val json = JSONObject(doGet(url)).getJSONObject("queryresult")
        var c = 0

        if (json.getBoolean("success")) {
            val pods = json.getJSONArray("pods")
            for (pod in pods) {
                pod as JSONObject

                val title = pod.getString("title")

                logger.info("\n## $title:\n")
                msg += if (c++ == 0) PlainText("# $title: \n") else PlainText("\n# $title: \n")

                for (i in 0 until pod.getInt("numsubpods")) {
                    val item = pod.getJSONArray("subpods").getJSONObject(i)
                    val img_src = item.getJSONObject("img").getString("src")
                    val img = URL(img_src).openStream().uploadAsImage(subject)
                    msg += img
                    logger.info("![]($img_src)\n")
                }

                msg += PlainText("\n---------")
                logger.info("\n---------\n")
            }
        }

        return if (msg.isEmpty()) {
            msg + PlainText("wolfram|alpha抽风啦")
        } else msg
    }

    /**
     * httpGet请求
     * @param url 链接
     * @return 返回的字符串
     */
    fun doGet(url: String) : String {
        val url_ = URL(url)
        val con = url_.openConnection()
        val http = con as HttpURLConnection
        http.requestMethod = "GET"
        http.setRequestProperty("Content-Type", "application/json; utf-8")
        http.setRequestProperty("Accept", "application/json")
        http.connect()
        return InputStreamReader(http.inputStream, StandardCharsets.UTF_8).readText()
    }
}

object Config : AutoSavePluginConfig("config") {
    val appid: String by value()
    val prefix: String by value()
}