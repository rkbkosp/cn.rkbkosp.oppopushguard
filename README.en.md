# OPPO Push Guard: ColorOS push notification blocker

[简体中文](README.md) | English

OPPO Push Guard is an **LSPosed module for mainland China ColorOS ROMs**, intended to reduce advertising and marketing notifications delivered to third-party apps through OPPO Push. It blocks notifications posted by non-system apps on the exact `push_oplus_category_content` content/marketing channel.

**Filtering is channel-based, not content-based. Legitimate notifications on that channel are also blocked; ads on other channels may still appear.**

[Download APK](https://github.com/rkbkosp/cn.rkbkosp.oppopushguard/releases/latest) · [Changelog](CHANGELOG.md) · [Report an issue](https://github.com/rkbkosp/cn.rkbkosp.oppopushguard/issues) · [MIT license](LICENSE)

## Before you install

- Requires a working LSPosed implementation that supports **libxposed API 102**. This module cannot operate as a standalone app.
- Targets mainland China ColorOS ROMs. The configured minimum is Android 8.0 (API 26); **this does not establish compatibility with every Android 8.0+ device**.
- Only selected framework files have been checked statically. Device installation, actual blocking, reboot persistence, and recovery have not yet been validated.
- The module runs in the system notification service and may affect important notifications. Back up important data and prepare a module-disable/recovery method appropriate for your device before enabling it.

## Download and install

1. Download `OppoPushGuard-*.apk` from the assets on the [Releases page](https://github.com/rkbkosp/cn.rkbkosp.oppopushguard/releases/latest), not the source archive. You can check its file hash against `SHA256SUMS` from the same release.
2. Install the APK and enable **OPPO Push Guard** in LSPosed.
3. Confirm that the scope is **System Framework** (`system`). Do not select third-party apps.
4. Reboot and look for the `OppoPushGuard` tag in LSPosed logs.

There is no launcher entry, settings screen, or per-app allowlist. Manage the module through LSPosed. Its application ID is `cn.rkbkosp.oppopushguard`.

## Behavior and limits

- Hooks `NotificationManagerService.enqueueNotificationInternal`. A notification is blocked only when its channel ID is **exactly** `push_oplus_category_content` and its posting package is neither a system app nor an updated-system app.
- Does not classify notification titles or text, block all third-party push notifications, remove in-app ads, or remove all ColorOS system ads.
- Other channels and system apps pass through the current policy. This is not a guarantee that important notifications cannot be affected.
- Does not modify the system image or notification-channel settings. After disabling the module and rebooting, it no longer participates in notification delivery.
- Logs hook installation counts, blocking counts, and lookup errors; it does not deliberately log notification titles or text. Review and redact personal information before sharing logs.
- If policy inspection throws, the notification is allowed through to avoid interrupting the notification service because of that lookup failure.

## Compatibility and validation

The repository records **static checks** against:

- The Ace3 ColorOS `16.0.5.1002` system image
- Read-only framework files from a PJE110 device on `16.0.5.701(CN01)`

These checks found three matching notification enqueue overloads. **This is not a list of device-tested configurations.** Other ColorOS versions, international ROMs, and later updates require their own validation.

Loading produces `Installed N notification enqueue hooks`. For the statically inspected frameworks above, the expected message is `Installed 3 notification enqueue hooks`. This reports hook installation only; it does not prove that a notification was successfully blocked.

## Troubleshooting and recovery

- **Ads still appear:** confirm the module is enabled, scoped correctly, and the device has rebooted. Other channels, system apps, and in-app ads are outside this policy.
- **No blocking log:** the counter is logged only on the first block and every 100 blocks. No log may simply mean no matching notification has arrived.
- **Unexpected hook count or errors:** check for `Could not install notification hooks`, `Channel policy lookup failed`, and the installed hook count. Recheck compatibility after a system update.
- **Missing important notifications or notification-service issues:** disable the module in LSPosed and reboot, then compare behavior. Do not rely on the module to distinguish marketing from legitimate content.
- **Device cannot boot normally:** use the device/framework recovery method prepared before enabling the module. The normal LSPosed UI may be inaccessible.

For reports, include the device model, full ColorOS/ROM build, Android version, LSPosed version, module version, and relevant `OppoPushGuard` logs. Remove notification content, account identifiers, and other personal information. See [Contributing](CONTRIBUTING.md).

## Disclaimer

This module hooks Android's system notification service and changes notification delivery at runtime. It is provided **as is**, with no guarantee that it works on your device or ROM. The author is **not liable** for missed or delayed notifications, notification-service failures, boot loops, data loss, or any other damage arising from installing or using this module. Validate it on your own device and make sure you have a recovery path before enabling it.

## Development and builds

Requirements: JDK 17, Android SDK Platform 36, and Build Tools 36.0.0. The project pins Gradle 9.4.1 and Android Gradle Plugin 9.2.1 and depends on libxposed API 102.

```sh
./gradlew :app:assembleDebug :app:assembleRelease
```

Local release output is unsigned by default. GitHub Actions builds debug and unsigned release variants on main-branch pushes and pull requests targeting main. Published APKs use the signing workflow. A successful build does not establish device compatibility.

<details>
<summary>Maintainers: automated signing and releases</summary>

The release workflow uses the existing PKCS#12 signing identity. Add these repository Actions secrets before creating a release tag:

- `OPPO_PUSH_KEYSTORE_BASE64`: base64 encoding of the PKCS#12 file.
- `OPPO_PUSH_KEYSTORE_PASSWORD`: that keystore's password.

For example, from a trusted local terminal, upload the key file without writing it into the repository:

```sh
base64 < ~/.android/signing/rkbkosp-release.p12 | tr -d '\n' | gh secret set OPPO_PUSH_KEYSTORE_BASE64 --repo rkbkosp/cn.rkbkosp.oppopushguard
gh secret set OPPO_PUSH_KEYSTORE_PASSWORD --repo rkbkosp/cn.rkbkosp.oppopushguard
```

The second command prompts for the password. The workflow checks the APK signer certificate against the expected SHA-256 fingerprint before publishing. The fingerprint is `59ea4ac3a16001cf66899275068c39c4ae5fbeab74537305a8bb7f5f51063263`.

Release tags must be `versionCode-versionName`, matching `app/build.gradle.kts`; the initial version is `1-0.1.0`. Push the tag after the secrets are configured:

```sh
git tag 1-0.1.0
git push origin 1-0.1.0
```

Actions attaches the signed APK and a SHA-256 checksum to the GitHub Release. The initial tag shown above is already published; for a new release, use a new tag matching the updated build configuration rather than pushing the old version again.

When submitting or syncing to the [Xposed Module Repository](https://github.com/Xposed-Modules-Repo#repo-requirment), follow its metadata requirements: the repository name is the application ID, and its description is used as the module name. Keep the catalog repository description as `OPPO Push Guard`; use this README and `SUMMARY.md` for the feature summary.

</details>

## License

MIT — see [LICENSE](LICENSE). You may use, copy, modify, merge, publish, distribute, sublicense and sell this project, including in closed-source form, as long as the copyright notice and the license text are retained. There is no warranty of any kind and no liability on the author; the full terms are in the license text.

OPPO is a trademark of its respective owner. This is an independent compatibility module; it is not affiliated with, endorsed by, or authorized by OPPO.

### Third-party components

- `io.github.libxposed:api` — Apache License 2.0. Referenced at compile time only (`compileOnly`) and not bundled in the APK.
- Gradle wrapper (`gradle/wrapper/gradle-wrapper.jar`) — Apache License 2.0.
