// 测试

import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.json.JSONArray
import org.json.JSONObject
import whiter.bot.Wolframalpha
import whiter.bot.foreach
import whiter.bot.foreachi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.charset.StandardCharsets


suspend fun main(args: Array<String>) {
    val appid = File("testappid").readText()
    var query = "population%20of%20france"
    query = "lim\\frac{sinx}{x}"
    query = "population of China"
    query = "cadjcbajcsb"
    query = URLEncoder.encode(query, "utf-8")
    val url = "http://api.wolframalpha.com/v2/query?appid=$appid&input=$query&output=json"

    val json = JSONObject(GET(url)).getJSONObject("queryresult")

    println(reduce(json))

    println(json)
}


fun maina(args: Array<String>) {
    val appid = File("testappid").readText()
    var query = "population%20of%20france"
    query = "lim\\frac{sinx}{x}"
    query = "population of China"
//    query = "polybenzimidazole"
    query = "阿里巴巴"
//    query = "vsdjvbskdbv"
//    query = "intarnetinoal"
//    query = "konodioda"
//    query = "the country that hosts? 2022 Olympic"
//    query = "hosts 2022 Olympic"




    query = URLEncoder.encode(query, "utf-8")

    val url = "http://api.wolframalpha.com/v2/query?appid=$appid&input=$query&output=json"
   println(GET(url))

//    val json = JSONObject(File("src/test/kotlin/result.json").readText()).getJSONObject("queryresult")
    val json = JSONObject(GET(url)).getJSONObject("queryresult")

    println(json.get("error") is Boolean)

    var c = 0

    if (json.getBoolean("success")) {
        val pods = json.getJSONArray("pods")
        for (pod in pods) {
            pod as JSONObject

            val title = pod.getString("title")
            val numspods = pod.getInt("numsubpods")

            println("## $title:")

            for (i in 0 until numspods) {
                val item = pod.getJSONArray("subpods").getJSONObject(i)
                val img_src = item.getJSONObject("img").getString("src")
                println("![]($img_src)")

                downloadUsingNIO(img_src, "dddd-${c++}.gif")

            }

            println("---")
        }
    } else if (json.getBoolean("error") == false) {

        if (json.has("didyoumeans")) {
            val dum = json.getJSONObject("didyoumeans")
            val msg = "wolfram|alpha提供的api搜索不到结果, 基于输入值$query, 猜测您有${dum.getFloat("score") * 100}%的概率是想查找: ${dum.getString("val")}"
            println(msg)
        } else if (json.has("tips")) {
            val tips = json.getJSONObject("tips").getString("text")

//            val msg = "wolfram|alpha提供的api搜索不到结果, 基于输入值$query, 猜测您有${dum.getFloat("score") * 100}%的概率是想查找: ${dum.getString("val")}"
            println(tips)
        }
    } else {
        println("error")
    }
}


suspend fun reduce(json: JSONObject): String {
    var msg = ""
    var c = 0
    if (json.getBoolean("success")) {
        val pods = json.getJSONArray("pods")
        // 兼容 android
        pods.mforeach {
            val title = it["title"]
            msg += if (c++ == 0) ("# $title: \n") else ("\n# $title: \n")

            for (i in 0 until it["numsubpods"] as Int) {
                val item = it.getJSONArray("subpods").getJSONObject(i)
                val img_src = item.getJSONObject("img").getString("src")
                msg += img_src
            }

            msg += "-----------------"
//            Wolframalpha.logger.info("\n---------\n")
        }

    } else if (json.get("error") is Boolean && !json.getBoolean("error")) {
        if (json.has("didyoumeans")) {
            if (json["didyoumeans"] is JSONObject) {
                val dum = json.getJSONObject("didyoumeans")
                msg += "wolfram|alpha提供的api搜索不到结果, 基于输入值, 猜测您" +
//                        "有${dum.getFloat("score") * 100}%的概率" +
                        "可能是想查找: ${dum.getString("val")}"
//                Wolframalpha.logger.info("> $msg")
            } else if (json["didyoumeans"] is JSONArray) {
                val dums = json.getJSONArray("didyoumeans")
                msg += "wolfram|alpha提供的api搜索不到结果, 基于输入值, 猜测您" +
//                        "有${dum.getFloat("score") * 100}%的概率" +
                        "可能是想查找: \n"
                dums.mforeachi { it, index ->
                    msg += "${index + 1}. " + it.get("val") + "\n"
                }
//                Wolframalpha.logger.info("> $msg")
            } else  {
                // re
                msg += json["didyoumeans"].toString()
            }
        } else if (json.has("tips")) {
            val tips = json.getJSONObject("tips").getString("text")
            msg += tips
//            Wolframalpha.logger.info("> $msg")
        } else if (json.has("languagemsg")) {
            if (json["languagemsg"] is JSONObject) {
                for (i in json.getJSONObject("languagemsg").keys()) {
                    msg += "" + json.getJSONObject("languagemsg").get(i) + ";"
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

//        Wolframalpha.logger.info("# error")
    }

    return msg

}


suspend fun JSONArray.mforeach(action: suspend (JSONObject) -> Unit) {
    for (i in 0 until this.length()) {
        action(this[i] as JSONObject)
    }
}

suspend fun JSONArray.mforeachi(action: suspend (JSONObject, Int) -> Unit) {
    for (i in 0 until this.length()) {
        action(this[i] as JSONObject, i)
    }
}

@Throws(IOException::class)
 fun downloadUsingNIO(urlStr: String, file: String) {
    val url = URL(urlStr)
    val rbc: ReadableByteChannel = Channels.newChannel(url.openStream())
    val fos = FileOutputStream(file)
    fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
    fos.close()
    rbc.close()
}

fun GET(url: String): String {
    val url_ = URL(url)
    val con = url_.openConnection()
    val http = con as HttpURLConnection
    http.requestMethod = "GET" // PUT is another valid option
    http.setRequestProperty("Content-Type", "application/json; utf-8")
    http.setRequestProperty("Accept", "application/json")
    http.connect()
    return InputStreamReader(http.inputStream, StandardCharsets.UTF_8).readText()
}