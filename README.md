# GoatGoalGo

完全离线的人生目标清单。把想成为的样子写成目标，再拆成可以一步步完成的子任务。

安装包：[GoatGoalGo 0.1.0](https://github.com/FuDie0915/GoatGoalGo/releases/tag/v0.1.0)

## 功能

- 目标和子任务是同一种节点，可以无限嵌套。
- 有子任务的节点可以展开或折叠，折叠状态保存在本机，重启后仍然有效。
- 状态分为进行中、完成、失败、放弃。父任务不会因为子任务而自动完成或失败。
- 可以给任务选一个截止日期。日期只显示在任务上，不会提醒。
- 同级任务可以长按拖动排序，也可以上移、下移。
- 删除会把整棵分支放进回收站，可以恢复或永久删除。
- 主题支持跟随系统、浅色和深色。内置 Ocean、Forest、Violet、Sunset、Rose、Slate 六套颜色，背景、卡片、主操作和强调色可以单独选择。
- 任务可以导出成 JSON，也可以从 JSON 导入。导入前会确认，并整体替换当前任务。主题和折叠状态不会写入备份。

应用不申请网络权限，也不连接账号、云同步或统计服务。

## 安装

从 [Releases](https://github.com/FuDie0915/GoatGoalGo/releases) 下载 GoatGoalGo-0.1.0.apk。最低系统版本是 Android 8.0。

## 本地构建

用 Android Studio 打开本仓库，或在项目根目录执行：

    ./gradlew assembleDebug

Windows 使用 gradlew.bat。minSdk 是 26，compileSdk 和 targetSdk 是 37。

发布包需要本机签名配置，签名文件不会提交到仓库。在 local.properties 中填写：

    release.storeFile=密钥库路径
    release.storePassword=密钥库密码
    release.keyAlias=密钥别名
    release.keyPassword=密钥密码

然后执行：

    ./gradlew assembleRelease
