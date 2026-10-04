# OPPO Push Guard

OPPO Push Guard is a libxposed API 102 module that blocks notifications posted through OPPO Push's `push_oplus_category_content` channel by non-system apps. Its application ID is `cn.rkbkosp.oppopushguard`.

## What it does

- Runs only in the Android system framework (`system` scope).
- Intercepts matching `NotificationManagerService.enqueueNotificationInternal` calls and drops notifications on the exact `push_oplus_category_content` channel when the posting package is not a system or updated-system app.
- Leaves other channels and system apps alone. It does not change notification-channel settings, so disabling the module restores normal posting behavior.
- Logs hook counts, blocked-notification counts, and lookup failures. It does not log notification content.
- Fails open if package or channel inspection throws, to avoid interrupting notification service operation.

## Compatibility and validation

The hook was checked against the Ace3 ColorOS 16.0.5.1002 system image and read-only framework files from a PJE110 device on `16.0.5.701(CN01)`. Those checks found three matching enqueue overloads. Installation, hook execution, reboot persistence, and recovery have not yet been validated on a device. Other ROMs and later updates need their own review.

The module is a system-server hook and can affect notification delivery. Enable it only in LSPosed's **system framework** scope. To recover, disable it in LSPosed and reboot.

## Disclaimer

This module hooks Android's system notification service and changes notification delivery at runtime. It is provided **as is**, with no guarantee that it works on your device or ROM. The author is **not liable** for missed or delayed notifications, notification-service failures, boot loops, data loss, or any other damage arising from installing or using this module. Validate it on your own device and make sure you have a recovery path before enabling it.

## Build

Requirements: JDK 17, Android SDK Platform 36, Build Tools 36.0.0. The Gradle wrapper pins Gradle 9.4.1 and the project uses Android Gradle Plugin 9.2.1.

```sh
./gradlew :app:assembleDebug :app:assembleRelease
```

The release output is unsigned unless signing is configured. GitHub Actions builds both variants for pushes and pull requests. A correctly formatted version tag builds, signs, verifies, and publishes a GitHub Release.

## Automated signing and releases

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

Actions attaches the signed APK and a SHA-256 checksum to the GitHub Release. The official LSPosed module repository reads releases from GitHub and requires a repository named after the application ID, a module-name description, and a valid release tag with an APK.

## Install and recover

Install the APK as a regular app, enable OPPO Push Guard in LSPosed, select only **System Framework** as its scope, and reboot. Check LSPosed logs for `Installed 3 notification enqueue hooks`. To recover, disable the module and reboot. The module does not write to the system image or persist notification-channel changes.

## License

MIT — see [LICENSE](LICENSE). You may use, copy, modify, merge, publish, distribute, sublicense and sell this project, including in closed-source form, as long as the copyright notice and the license text are retained. There is no warranty of any kind and no liability on the author; the full terms are in the license text.

OPPO is a trademark of its respective owner. This is an independent compatibility module; it is not affiliated with, endorsed by, or authorized by OPPO.

### Third-party components

- `io.github.libxposed:api` — Apache License 2.0. Referenced at compile time only (`compileOnly`) and not bundled in the APK.
- Gradle wrapper (`gradle/wrapper/gradle-wrapper.jar`) — Apache License 2.0.
