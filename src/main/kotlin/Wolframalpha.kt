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
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Wolframalpha : KotlinPlugin(
    JvmPluginDescription(
        id = "whiter.bot.wolframalpha",
        version = "1.3",
    )
) {
    private lateinit var appid: String
    lateinit var errorMsg: String
    private lateinit var slice: String
    private lateinit var separationLine: String

    override fun onEnable() {
        logger.info { "WolframAlpha Plugin loaded" }
        Config.reload()
        appid = Config.appid
        if (appid.isEmpty()) throw Exception("your appid can not be empty")
        slice = if (Config.prefix == "") "''" else Config.prefix
        errorMsg = if (Config.error_msg == "") "wolfram|alpha抽风啦" else Config.error_msg
        separationLine = when (Config.separation_line) {
            "" -> "\n---------"
            "empty" -> ""
            else -> Config.separation_line
        }

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
    private suspend fun query(str: String, subject: Contact): MessageChain {
        var msg: MessageChain = EmptyMessageChain
        val query = URLEncoder.encode(str, "utf-8")
        val url = "http://api.wolframalpha.com/v2/query?appid=$appid&input=$query&output=json"
        val json = JSONObject(doGet(url)).getJSONObject("queryresult")
        var c = 0

        if (json.getBoolean("success")) {
            val pods = json.getJSONArray("pods")
            // 兼容 android
            pods.foreach {
                val title = it["title"]
                logger.info("\n## $title:\n")
                msg += if (c++ == 0) PlainText("# $title: \n") else PlainText("\n# $title: \n")

                for (i in 0 until it["numsubpods"] as Int) {
                    val item = it.getJSONArray("subpods").getJSONObject(i)
                    val img_src = item.getJSONObject("img").getString("src")
                    val openStream = URL(img_src).openStream()
                    val img = openStream.uploadAsImage(subject)
                    msg += img
                    logger.info("![]($img_src)\n")
                    openStream.close()
                }

                msg += PlainText(separationLine)
                logger.info("\n---------\n")
            }

        } else if (json.get("error") is Boolean && !json.getBoolean("error")) {
            if (json.has("didyoumeans")) {
                if (json["didyoumeans"] is JSONObject) {
                    val dum = json.getJSONObject("didyoumeans")
                    msg += "wolfram|alpha提供的api搜索不到结果, 基于输入值$str, 猜测您" +
//                        "有${dum.getFloat("score") * 100}%的概率" +
                            "可能是想查找: ${dum.getString("val")}"
                    logger.info("> $msg")
                } else if (json["didyoumeans"] is JSONArray) {
                    val dums = json.getJSONArray("didyoumeans")
                    msg += "wolfram|alpha提供的api搜索不到结果, 基于输入值$str, 猜测您" +
//                        "有${dum.getFloat("score") * 100}%的概率" +
                            "可能是想查找: \n"
                    dums.foreachi { it, index ->
                        msg += "${index + 1}. " + it.get("val") + "\n"
                    }
                    logger.info("> $msg")
                } else  {
                    // re
                    msg += json["didyoumeans"].toString()
                }
            }

            if (json.has("tips")) {
                val tips = json.getJSONObject("tips").getString("text")
                msg += tips
                logger.info("> $msg")
            }

            if (json.has("languagemsg")) {
                if (json["languagemsg"] is JSONObject) {
                    for (i in json.getJSONObject("languagemsg").keys()) {
                        msg += json.getJSONObject("languagemsg").get(i).toString()
                    }
                } else {
                    msg += json["languagemsg"].toString()
                }
            }
        } else {
            if (json["error"] is JSONObject) {
                for (i in json.getJSONObject("error").keys()) {
                    val errmsg = json.getJSONObject("error").get(i)
                    msg += "$i: $errmsg; \n"
                }
            } else if (json["error"] is JSONArray) {
                json.getJSONArray("error").foreach {
                    msg += it.toString() + "\n"
                }
            } else {
                msg += "error: " + json["error"].toString()
            }

            logger.info("# error")
        }

        return if (msg.isEmpty()) {
            msg + PlainText(errorMsg)
        } else {
            // wolframalpha给出的中国地图是错误的
            if (str == "中国" || str == "中华人民共和国" ||
                str == "台湾" || str == "台北" ||
                str == "中国台湾" || str == "中国台北" ||
                str == "藏南" || str == "zangnan" ||
                str.equals("China", true) ||
                str.equals("the People's Republic of China", true) ||
                str.equals("PRC", true) ||
                str.equals("taiwan", true) ||
                str.equals("taipei", true) ||
                str.equals("chinese taipei", true) ||
                str.equals("taipei city", true) ||
                str.equals("People's Republic of China", true)
            ) msg + "\n注: 以上的地图是错误的, 台湾和藏南是中国领土的一部分" else msg
        }
    }

    /**
     * httpGet请求
     * @param url 链接
     * @return 返回的字符串
     */
    private fun doGet(url: String) : String {
        val url = URL(url)
        val con = url.openConnection()
        val req = con as HttpURLConnection
        req.requestMethod = "GET"
        req.setRequestProperty("Content-Type", "application/json; utf-8")
        req.setRequestProperty("Accept", "application/json")
        req.connect()
        return InputStreamReader(req.inputStream, StandardCharsets.UTF_8).readText()
    }
}

suspend fun JSONArray.foreach(action: suspend (JSONObject) -> Unit) {
    for (i in 0 until this.length()) {
        action(this[i] as JSONObject)
    }
}

suspend fun JSONArray.foreachi(action: suspend (JSONObject, Int) -> Unit) {
    for (i in 0 until this.length()) {
        action(this[i] as JSONObject, i)
    }
}

object Config : AutoSavePluginConfig("config") {
    val appid: String by value()
    val prefix: String by value()
    val error_msg: String by value()
    val separation_line: String by value()
}