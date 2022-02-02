// 测试

import org.json.JSONObject
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


fun main(args: Array<String>) {
    val appid = File("appid").readText()
    var query = "population%20of%20france"
    query = "lim\\frac{sinx}{x}"
    query = "population of China"
    query = "polybenzimidazole"
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