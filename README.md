# wolframalpha4mirai

这是一个将wolframalpha api接入mirai的插件
添加后, 你可以在qq里直接向wolframalpha发出查询请求

## 使用方法
* 将jar文件放入mirai运行目录下的plugins\文件夹
* 前往[此处](https://developer.wolframalpha.com/portal/myapps/index.html)按照提示获取一个appid
* 启动一次mirai并关闭, 或者你也可以在config\下新建whiter.bot.wolframalpha\config.yml文件
* 按照如下方式编辑config.yml
```yaml
appid: 'your appid' # 填入刚刚获得的appid
prefix: '' # 触发前缀, 可以省略, 省略时默认使用两个单引号做触发前缀
```
* 启动mirai

## 注意事项
> 由于wolframalpha不支持非英文(好像还支持日文), 所以当输入消息中含有非英文字符时可能会导致未知错误
