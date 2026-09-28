# OPPO Push Guard

OPPO Push Guard 是一个基于 libxposed API 102 的 LSPosed 模块。当非系统应用通过 OPPO Push 的 `push_oplus_category_content` 通道提交通知时，模块会在 Android 通知服务中拦截该通知。应用 ID：`cn.rkbkosp.oppopushguard`。

## 功能与边界

- 只运行在 Android 系统框架（`system`）作用域。
- 拦截 `NotificationManagerService.enqueueNotificationInternal` 中通道 ID **完全等于** `push_oplus_category_content` 的通知；发送包属于普通应用时丢弃。
- 系统应用和系统更新应用、其他通道不受影响。模块不修改系统通知通道设置，停用并重启后会恢复原有投递行为。
- 只记录 hook 安装数量、拦截计数和查找错误，不记录通知标题、正文或标识符。
- 包名或通道查询异常时放行通知，避免模块错误中断通知服务。

## 兼容性与验证状态

Hook 已针对 Ace3 ColorOS 16.0.5.1002 系统镜像，以及只读提取的 PJE110 `16.0.5.701(CN01)` 框架文件进行核对，匹配到 3 个通知入队重载。设备安装、实际拦截、重启持续性和停用恢复尚未完成验证。其他 ROM 或后续系统版本需要重新核对。

这是 system_server hook，会影响通知投递。启用时只选择 LSPosed 的**系统框架**作用域。恢复方法是在 LSPosed 中停用模块并重启。

## 构建

需要 JDK 17、Android SDK Platform 36 和 Build Tools 36.0.0。仓库固定使用 Gradle 9.4.1 与 Android Gradle Plugin 9.2.1。

```sh
./gradlew :app:assembleDebug :app:assembleRelease
```

Release 默认是未签名包。GitHub Actions 会为推送和 Pull Request 编译 Debug 与未签名 Release；符合版本格式的 tag 会触发正式签名、签名校验和 GitHub Release 发布。

## 配置自动签名与发布

Release 工作流使用现有 PKCS#12 个人签名身份。创建版本 tag 前，在 GitHub 仓库的 **Settings → Secrets and variables → Actions** 中配置以下加密 Secrets：

- `OPPO_PUSH_KEYSTORE_BASE64`：PKCS#12 文件的 Base64 内容。
- `OPPO_PUSH_KEYSTORE_PASSWORD`：该 keystore 的密码。

可在可信任的本机终端上传密钥文件；命令不会把它写进仓库：

```sh
base64 < /Users/rkbkosp/.android/signing/rkbkosp-release.p12 | tr -d '\n' | gh secret set OPPO_PUSH_KEYSTORE_BASE64 --repo rkbkosp/cn.rkbkosp.oppopushguard
gh secret set OPPO_PUSH_KEYSTORE_PASSWORD --repo rkbkosp/cn.rkbkosp.oppopushguard
```

第二条命令会提示输入密码。工作流会在发布前检查 APK 证书 SHA-256 指纹是否与个人签名身份一致：`59ea4ac3a16001cf66899275068c39c4ae5fbeab74537305a8bb7f5f51063263`。

tag 必须是 `versionCode-versionName`，并与 `app/build.gradle.kts` 一致。初始版本为 `1-0.1.0`。配置好 Secrets 后推送 tag：

```sh
git tag 1-0.1.0
git push origin 1-0.1.0
```

Actions 会把签名 APK 和 SHA-256 校验文件附加到 GitHub Release。LSPosed 官方模块仓库从 GitHub Release 获取 APK；项目仓库名必须与 application ID 一致，仓库描述应是模块名称，Release tag 必须含版本号和版本名。

## 安装与恢复

像普通应用一样安装 APK，在 LSPosed 中启用 OPPO Push Guard，只选择**系统框架**作用域，然后重启。检查 LSPosed 日志中的 `Installed 3 notification enqueue hooks`。恢复时停用模块并重启。模块不修改系统镜像，也不持久化修改通知通道。

## 许可证

当前没有授予源代码复用许可。如需再分发或集成到其他项目，请先联系维护者。
