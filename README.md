# OPPO Push Guard：ColorOS 推送广告拦截模块

简体中文 | [English](README.en.md)

OPPO Push Guard 是面向**国行 ColorOS** 的 **LSPosed 模块**，用于减少第三方应用借助 OPPO Push 系统推送通道发送的广告和营销通知。模块会拦截非系统应用发往 `push_oplus_category_content` 内容/营销通道的通知。

**注意：模块按通知通道拦截，不判断内容是否为广告。同一通道里的正常通知也会被拦截，其他通道里的广告仍可能出现。**

[下载 APK](https://github.com/rkbkosp/cn.rkbkosp.oppopushguard/releases/latest) · [更新记录](CHANGELOG.md) · [反馈问题](https://github.com/rkbkosp/cn.rkbkosp.oppopushguard/issues) · [MIT 许可证](LICENSE)

## 使用前必读

- 需要已安装并正常工作的 LSPosed，且其实现支持 **libxposed API 102**。本模块不能作为普通应用独立运行。
- 目标环境为国行 ColorOS。构建配置的最低 Android 版本为 Android 8.0（API 26），**这不是所有 Android 8.0 及以上设备都兼容的承诺**。
- 当前只完成了部分系统框架文件的静态核对，尚未完成真机安装、实际拦截、重启持续性和停用恢复验证，详见下文。
- 模块运行在系统通知服务中，可能影响重要通知。启用前请备份重要数据，并准备适合自己设备的模块停用与救援方法。

## 下载与安装

1. 在 [Releases](https://github.com/rkbkosp/cn.rkbkosp.oppopushguard/releases/latest) 的资源列表中下载 `OppoPushGuard-*.apk`，不要把源码压缩包当作安装包。需要时可用同一版本的 `SHA256SUMS` 核对文件校验值。
2. 安装 APK，在 LSPosed 中启用 **OPPO Push Guard**。
3. 确认作用域为**系统框架**（`system`），不要勾选第三方应用。
4. 重启设备，在 LSPosed 日志中查找标签 `OppoPushGuard`。

本模块没有桌面启动入口、设置界面或应用白名单，安装后请通过 LSPosed 管理。应用 ID 为 `cn.rkbkosp.oppopushguard`。

## 功能与边界

- 在 `NotificationManagerService.enqueueNotificationInternal` 中检查通知通道；仅当通道 ID **完全等于** `push_oplus_category_content` 且发送包不是系统应用或系统更新应用时拦截。
- 不对通知标题、正文进行广告识别，也不会拦截所有第三方推送、应用内广告或所有 ColorOS 系统广告。
- 按当前规则，其他通道和系统应用会被放行；这不代表重要通知一定不会受影响。
- 不修改系统镜像或通知通道设置。停用并重启后，模块不再参与通知投递。
- 记录 hook 安装数量、拦截计数和查询异常；不主动记录通知标题或正文。分享日志前仍需检查并删除个人信息。
- 检查过程发生异常时放行通知，避免该次规则查询错误中断通知服务。

## 兼容性与验证状态

当前仓库记录了以下**静态核对**：

- Ace3 ColorOS `16.0.5.1002` 系统镜像
- PJE110 `16.0.5.701(CN01)` 的只读框架文件

上述核对找到 3 个匹配的通知入队方法重载。**这不是已通过真机测试的设备列表**，也不能据此保证其他 ColorOS 版本、海外 ROM 或后续系统更新可用。

正常加载时日志会显示 `Installed N notification enqueue hooks`；在上述静态核对的框架中预期为 `Installed 3 notification enqueue hooks`。这条日志只说明 hook 安装数量，不能单独证明通知已经成功拦截。

## 排查与恢复

- **仍然收到广告：**先确认模块已启用、作用域正确且已重启。其他通知通道、系统应用或应用内广告不在当前规则内。
- **找不到拦截日志：**拦截计数只在首次及每 100 次时记录。没有日志不等于模块失效，也可能尚未遇到匹配通知。
- **hook 数量异常或出现错误：**关注 `Could not install notification hooks`、`Channel policy lookup failed` 以及 hook 安装数量；系统更新后应重新确认兼容性。
- **重要通知缺失或通知服务异常：**在 LSPosed 中停用模块并重启，比较停用前后的表现。不要依赖本模块自动区分营销与正常通知。
- **无法正常开机：**使用启用前准备的设备/框架救援方式停用模块。无法进入系统时，普通的 LSPosed 界面操作可能不可用。

反馈时请提供设备型号、完整 ColorOS/ROM 版本、Android 版本、LSPosed 版本、模块版本及相关 `OppoPushGuard` 日志。请删除通知内容、账号标识和其他个人信息，参见[贡献指南](CONTRIBUTING.md)。

## 免责声明

本模块在运行时 hook Android 系统通知服务并改变通知投递行为，**按现状提供**，不保证在你的设备或 ROM 上可用。因安装或使用本模块造成的通知漏失或延迟、通知服务异常、无法开机、数据丢失及其他任何损害，作者**不承担责任**。请在设备上自行验证，并在启用前确保有恢复手段。

## 开发与构建

需要 JDK 17、Android SDK Platform 36 和 Build Tools 36.0.0。仓库固定使用 Gradle 9.4.1 与 Android Gradle Plugin 9.2.1，依赖 libxposed API 102。

```sh
./gradlew :app:assembleDebug :app:assembleRelease
```

本地 Release 默认是未签名包。GitHub Actions 会为主分支推送和面向主分支的拉取请求编译 Debug 与未签名 Release；正式版本通过签名发布工作流提供 APK。构建成功不代表真机兼容性验证通过。

<details>
<summary>维护者：自动签名与发布</summary>

Release 工作流使用现有 PKCS#12 个人签名身份。创建版本 tag 前，在 GitHub 仓库的 **Settings → Secrets and variables → Actions** 中配置以下加密 Secrets：

- `OPPO_PUSH_KEYSTORE_BASE64`：PKCS#12 文件的 Base64 内容。
- `OPPO_PUSH_KEYSTORE_PASSWORD`：该 keystore 的密码。

可在可信任的本机终端上传密钥文件；命令不会把它写进仓库：

```sh
base64 < ~/.android/signing/rkbkosp-release.p12 | tr -d '\n' | gh secret set OPPO_PUSH_KEYSTORE_BASE64 --repo rkbkosp/cn.rkbkosp.oppopushguard
gh secret set OPPO_PUSH_KEYSTORE_PASSWORD --repo rkbkosp/cn.rkbkosp.oppopushguard
```

第二条命令会提示输入密码。工作流会在发布前检查 APK 证书 SHA-256 指纹是否与个人签名身份一致：`59ea4ac3a16001cf66899275068c39c4ae5fbeab74537305a8bb7f5f51063263`。

tag 必须是 `versionCode-versionName`，并与 `app/build.gradle.kts` 一致。初始版本为 `1-0.1.0`。配置好 Secrets 后推送 tag：

```sh
git tag 1-0.1.0
git push origin 1-0.1.0
```

Actions 会把签名 APK 和 SHA-256 校验文件附加到 GitHub Release。上述初始版本 tag 已发布；发布新版本时需使用与构建配置一致的新 tag，不要重复推送旧版本。

向 [Xposed 模块仓库](https://github.com/Xposed-Modules-Repo#repo-requirment) 提交或同步模块时，请遵守其元数据要求：仓库名使用应用 ID，仓库描述用作模块名称。因此用于模块目录的仓库描述保留为 `OPPO Push Guard`，功能说明放在本文和 `SUMMARY` 中。

目录的简短介绍使用无扩展名的 [`SUMMARY`](SUMMARY)，详细介绍使用 `README.md`，见[官方提交说明](https://github.com/Xposed-Modules-Repo/submission/blob/master/README.md)。源码仓库与 [Xposed 模块目录仓库](https://github.com/Xposed-Modules-Repo/cn.rkbkosp.oppopushguard) 是两个独立仓库；更新本仓库后仍需核对并同步目录的说明与摘要，不能把源码 PR 合并视为目录已更新。

</details>

## 许可证

MIT，见 [LICENSE](LICENSE)。你可以使用、复制、修改、合并、发布、分发、再授权乃至出售本项目，包括闭源形式，只需保留版权声明和许可证正文。本项目不提供任何形式的担保，作者不承担任何责任，完整条款以许可证正文为准。

OPPO 是其权利人的商标。本项目是独立的兼容性模块，与 OPPO 无关联，未获其授权或背书。

### 第三方组件

- `io.github.libxposed:api` —— Apache License 2.0，仅编译期引用（`compileOnly`），不打包进 APK。
- Gradle wrapper（`gradle/wrapper/gradle-wrapper.jar`）—— Apache License 2.0。
