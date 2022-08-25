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
3. 启动一次 mirai 并关闭, 或者你也可以在 `config\` 下新建 `bot.query.wolframalpha.whiter\config.yml` 文件
4. 按照如下方式编辑 `config.yml`
```yaml
# wolfram|alpha 的 appid, 前往 https://developer.wolframalpha.com/portal/myapps/index.html 获得
appid: 'your appid'
# 触发前缀, 为空时是两个单引号
prefix: ''
# 当发生错误时的提示信息
error_msg: ''
# 图片与文本的分割线, 为空时是 ---------, 填 empty 时为空白字符
separation_line: ''
# 是否构建转发消息
isForward: true
```
5. 启动 mirai

### 效果

 [<img src="https://s4.ax1x.com/2022/01/14/71ZvkV.png" alt="71ZvkV.png" style="zoom: 54%;" />](https://imgtu.com/i/71ZvkV) [<img src="https://s4.ax1x.com/2022/01/14/71ZxYT.png" alt="71ZxYT.png" style="zoom: 67%;" />](https://imgtu.com/i/71ZxYT)

### 注意事项
 * 由于 wolframalpha 不支持非英文(但好像支持日文), 所以当输入消息中含有非英文字符时可能会导致未知错误
 * 若出现类似如下报错
```text
E/bot.query.wolframalpha.whiter: Exception in coroutine Plugin whiter.bot.wolframalpha of whiter.bot.wolframalpha
    java.lang.NoClassDefFoundError: org/json/JSONObject
    at bot.query.wolframalpha.whiter.Wolframalpha.query(Wolframalpha.kt:57)
```
为缺少 `org.json` 包导致的找不到类的错误

可以在 `plugin-shared-libraries/libraries.txt` 追加以下内容解决
```text
org.json:json:20220320
```