# wolframalpha4mirai

![GitHub](https://img.shields.io/github/license/whiterasbk/wolframalpha4mirai)
![GitHub all releases](https://img.shields.io/github/downloads/whiterasbk/wolframalpha4mirai/total)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/whiterasbk/wolframalpha4mirai)
![GitHub top language](https://img.shields.io/github/languages/top/whiterasbk/wolframalpha4mirai)

这是一个将 wolframalpha api 接入 mirai 的插件
添加后, 你可以在 qq 里直接向 wolframalpha 发出查询请求

### 使用方法
1. 将 [release](https://github.com/whiterasbk/wolframalpha4mirai/releases/tag/1.3) 下的 [wolframalpha4mirai.jar](https://github.com/whiterasbk/wolframalpha4mirai/releases/download/1.3/wolframalpha-1.3.jar) 文件下载并放入 mirai 运行目录下的 `plugins\` 文件夹
2. 前往 [此处](https://developer.wolframalpha.com/portal/myapps/index.html) 按照提示获取一个 **appid**
3. 启动一次 mirai 并关闭, 或者你也可以在 `config\` 下新建 `whiter.bot.wolframalpha\config.yml` 文件
4. 按照如下方式编辑 `config.yml`
```yaml
appid: 'your appid' # 填入刚刚获得的appid, 必需
prefix: '' # 触发前缀, 可选, 省略时默认使用两个单引号做为触发前缀
error_msg: '' # 错误提示信息, 可选
separation_line: '' # 分隔符, 可选, 默认为'---------', 若填empty则无分隔符
```
5. 启动 mirai

### 效果

 [<img src="https://s4.ax1x.com/2022/01/14/71ZvkV.png" alt="71ZvkV.png" style="zoom: 54%;" />](https://imgtu.com/i/71ZvkV) [<img src="https://s4.ax1x.com/2022/01/14/71ZxYT.png" alt="71ZxYT.png" style="zoom: 67%;" />](https://imgtu.com/i/71ZxYT)

### 注意事项
 * 由于 wolframalpha 不支持非英文(但好像还支持日文), 所以当输入消息中含有非英文字符时可能会导致未知错误
 * 若出现类似如下报错
```text
E/whiter.bot.wolframalpha: Exception in coroutine Plugin whiter.bot.wolframalpha of whiter.bot.wolframalpha
    java.lang.NoClassDefFoundError: org/json/JSONObject
    at whiter.bot.Wolframalpha.query(Wolframalpha.kt:57)
```
 则 **使用方法** `步骤 1` 中需要下载 [wolframalpha4mirai-with-org.json.jar](https://github.com/whiterasbk/wolframalpha4mirai/releases/download/1.3/wolframalpha-1.3-with-org-json.jar) 文件而不是 `wolframalpha4mirai.jar`
