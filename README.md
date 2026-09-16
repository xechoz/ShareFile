# ShareFile

A cross-platform app for sharing and receiving files over your local network, built with
[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) and targeting
**Android** and **Desktop (JVM)**. Similar in spirit to [LocalSend](https://localsend.org/),
but focused on just two things: **share** and **receive**.

The other device does not need to install anything — it just opens a URL in a browser, or
scans a QR code.

## Features

- **Share** — pick files, start a built-in HTTP server, and show a QR code / URL. The other device opens the URL and downloads the files.
- **Receive** — start a built-in HTTP server and show a QR code / URL. The other device opens the URL and uploads files to your device.
- **Scan** — scan another device's QR code to browse and download its shared files directly inside the app. *Android only; the button is hidden on platforms without a camera.*

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
