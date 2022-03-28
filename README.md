# SDK
sca: 这个是语音播报功能，例如把收到的文字播报出来。语音播报。

语音这获取的sdk放入build_shell 目录中

- mix 离在线版本
- offline 纯离线版本
- online 在线版本

之后根据sdk的zip文件名，新建一个目录。如果是google play的专用版本，则加gp后缀。

修改switch.sh 

bash build_shell/switch.sh online normal
即可切换普通online版本

bash pack.sh 
打包4个线上版本


# 百度离线语音播报，可以实现类似,文字转语音， 可以配合 push, 播报推送内容。文档地址：https://cloud.baidu.com/doc/SPEECH/s/Gk7h80eb4