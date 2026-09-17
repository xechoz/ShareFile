# Quick File Share — Share Files Over Your Local Network (Android & Desktop)

**English** | [简体中文](README.zh-CN.md)

[![Build](https://github.com/xechoz/quick-file-share/actions/workflows/build.yml/badge.svg)](https://github.com/xechoz/quick-file-share/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/xechoz/quick-file-share)](https://github.com/xechoz/quick-file-share/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Quick File Share is a free, open-source **file transfer app for Android and Desktop** that sends
and receives files **over your local network (LAN/Wi-Fi)**. The other device does **not** need to
install anything — it just opens a URL in a browser, or scans a QR code. No account, no cloud, no
internet connection required.

It is similar in spirit to [LocalSend](https://localsend.org/) and AirDrop, but focused on just two
things: **share** and **receive**. Built with [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/).

➡️ **Website & downloads: <https://xechoz.github.io/quick-file-share/>**

## Screenshots

| Android | Share files | Receive files |
|:-------:|:-----:|:-------:|
| <img src="docs/screenshots/android-home.png" width="240" alt="Quick File Share Android app home screen"> | <img src="docs/screenshots/android-share.png" width="240" alt="Share files over local network with a QR code on Android"> | <img src="docs/screenshots/android-receive.png" width="240" alt="Receive files from another device over Wi-Fi on Android"> |

| Desktop | Browser |
|:-------:|:-------:|
| <img src="docs/screenshots/desktop-home.png" width="420" alt="Quick File Share desktop app for Linux, Windows and macOS"> | <img src="docs/screenshots/browser-upload.png" width="240" alt="Browser upload page for receiving files without installing an app"> |
| Share and Receive on Desktop | The other device opens the link in a browser — no install needed |

## Features

- **Share files** — pick files, start a built-in HTTP server, and show a QR code / URL. The other device opens the URL and downloads the files.
- **Receive files** — start a built-in HTTP server and show a QR code / URL. The other device opens the URL and uploads files to your device.
- **Scan a QR code** — scan another device's QR code to browse and download its shared files directly inside the app. *Android only; the button is hidden on platforms without a camera.*
- **Cross-platform** — one app for Android, Linux, Windows and macOS, with the same local network file sharing protocol.
- **No install on the other side** — any device with a web browser can send or receive files.
- **Private by design** — files travel directly between devices on your LAN; nothing is uploaded to a server.

Share and Receive work on both Android and Desktop. Scanning is only available on Android;
desktop shows an explanatory message instead of the camera preview.

## How it works

The app embeds a lightweight HTTP server ([NanoHTTPD](https://github.com/NanoHttpd/nanohttpd)) bound to your device's LAN address.

| Mode | URL | Purpose |
|------|-----|---------|
| Share | `http://<ip>:<port>/share` | Browser page listing files for download |
| Receive | `http://<ip>:<port>/receive` | Browser page with an upload form |
| API | `http://<ip>:<port>/api/files` | JSON file list (used by the in-app scanner) |
| Download | `http://<ip>:<port>/download/<id>` | File stream |

The server prefers port `8080` and falls back to `8081`–`8088` if it is taken, so a firewall
rule can target a known range.

QR codes are generated with [ZXing](https://github.com/zxing/zxing); scanning uses CameraX + ML Kit on Android.

### Firewall

Desktop firewalls block incoming connections by default, so the other device may not be able to
open the link. When that is detected, the Share/Receive screen shows a card with an
**Allow through firewall** button. The rule is scoped to the local subnet, and the exact command
is also shown so it can be run manually.

| Platform | How it is handled |
|----------|-------------------|
| Linux / ufw | `pkexec ufw allow from <subnet> to any port <port> proto tcp` |
| Linux / firewalld | `pkexec firewall-cmd --permanent --add-rich-rule=...` scoped to the subnet |
| Windows | UAC-elevated `netsh advfirewall` rule scoped to the subnet |
| macOS | the system's "accept incoming connections" prompt; best-effort unblock via `socketfilterfw` |

Android has no host firewall, so the card is never shown there.

## FAQ

### Do both devices need to install the app?

No. Only the device that starts Share or Receive runs Quick File Share. The other device just needs
a web browser — it opens the link or scans the QR code, then downloads or uploads the files.

### Does it work offline?

Yes. Files are transferred directly over your local network. No internet connection and no cloud
account are required.

### Is Quick File Share a LocalSend alternative?

Yes. Like LocalSend it is a free, open-source, cross-platform LAN file transfer tool. The difference
is scope: Quick File Share focuses on the share/receive flow and lets the receiving device use a
plain browser instead of installing the app.

### Is it safe?

Your files never leave your local network — they go straight from one device to the other. The
embedded server only listens on your LAN address and is stopped when you close the Share/Receive
screen.

### Which platforms are supported?

Android 8.0+ (APK), Linux (deb, rpm, Arch, portable), Windows (msi) and macOS (dmg).

## Project structure

The project is split into three modules:

| Module | Plugin | Contents |
|--------|--------|----------|
| `:shared` | Kotlin Multiplatform + `com.android.kotlin.multiplatform.library` | All shared code and Compose UI |
| `:app` | `com.android.application` | Android shell: manifest, resources, web assets |
| `:desktop` | Kotlin/JVM + Compose Desktop | Desktop entry point and packaging |

`:shared` is organized by Compose Multiplatform source sets:

```
shared/src/
├── commonMain/     # Models, domain interfaces, all shared Compose UI, theme
├── jvmCommonMain/  # Shared between Android + Desktop: HTTP server, HTTP client, QR matrix, parsers
├── androidMain/    # Android entry point (MainActivity), MediaStore/SAF/FileProvider, CameraX + ML Kit scanner
├── desktopMain/    # Desktop platform impls (filesystem storage, AWT file picker, Skia image decoding), web resources
├── commonTest/     # Pure-logic tests
└── jvmCommonTest/  # JVM-shared tests (run on both Android host tests and desktop tests)
```

`:app` contains no Kotlin: it declares `MainActivity` (defined in `:shared`) in its manifest
and supplies the Android resources and web assets.

Platform capabilities are inverted behind interfaces (`FileServer`, `FileDownloader`,
`PlatformServices`) with `expect`/`actual` for UI-level integrations (file picker, QR scanner,
image loading, back handling), so the shared UI degrades gracefully where a feature is
unavailable.

## Requirements

- Android 8.0 (API 26) or newer
- Desktop: JDK 17 or newer
- Both devices on the same local network

## Build

```bash
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

### Run on desktop

```bash
./gradlew :desktop:run
```

Package a native distribution for the current OS:

```bash
./gradlew :desktop:packageDistributionForCurrentOS
```

### Linux packages

Linux packages are slim: they do not bundle a JRE and depend on the system
Java 17+ instead, which the package manager installs automatically.

```bash
bash desktop/packaging/build-linux-packages.sh
```

The script builds a portable staging directory (`:desktop:slimDist`) and
generates `deb`, `rpm` and Arch packages into `desktop/build/dist/`.

### Release build

Release builds are signed with a keystore configured outside the repository. Create
`keystore.properties` in the project root (it is git-ignored):

```properties
storeFile=/absolute/path/to/your.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Then:

```bash
./gradlew assembleRelease
```

If `keystore.properties` is absent, the release build falls back to the debug signing
configuration so the project still builds out of the box.

## Tests

```bash
./gradlew :shared:desktopTest :shared:testAndroidHostTest
```

## Tech stack

- Kotlin Multiplatform + Compose Multiplatform (Material 3)
- NanoHTTPD — embedded HTTP server (shared JVM code)
- ZXing — QR generation (shared JVM code)
- CameraX + ML Kit — QR scanning (Android)
- `HttpURLConnection` — in-app client for downloading from another device

## Notes

`:shared` uses the new `com.android.kotlin.multiplatform.library` plugin rather than
`androidTarget()`. AGP 9 does not support `androidTarget()` alongside `com.android.application`
in the same module, which is why the Android application lives in `:app` and the shared KMP
code in `:shared`.

## License

[MIT](LICENSE)
