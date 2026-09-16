# ShareFile

A minimal Android app for sharing and receiving files over your local network. Similar in spirit to [LocalSend](https://localsend.org/), but focused on just two things: **share** and **receive**.

The other device does not need to install anything — it just opens a URL in a browser, or scans a QR code.

## Features

- **Share** — pick files, start a built-in HTTP server, and show a QR code / URL. The other device opens the URL and downloads the files.
- **Receive** — start a built-in HTTP server and show a QR code / URL. The other device opens the URL and uploads files to your device.
- **Scan** — scan another device's QR code to browse and download its shared files directly inside the app.

## How it works

The app embeds a lightweight HTTP server ([NanoHTTPD](https://github.com/NanoHttpd/nanohttpd)) bound to your device's LAN address.

| Mode | URL | Purpose |
|------|-----|---------|
| Share | `http://<ip>:<port>/share` | Browser page listing files for download |
| Receive | `http://<ip>:<port>/receive` | Browser page with an upload form |
| API | `http://<ip>:<port>/api/files` | JSON file list (used by the in-app scanner) |
| Download | `http://<ip>:<port>/download/<id>` | File stream |

The server prefers port `8080` and falls back to a random free port if it is taken.

QR codes are generated with [ZXing](https://github.com/zxing/zxing); scanning uses CameraX + ML Kit.

## Requirements

- Android 8.0 (API 26) or newer
- Both devices on the same local network

## Build

```bash
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

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
./gradlew testDebugUnitTest
```

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- NanoHTTPD — embedded HTTP server
- ZXing — QR generation
- CameraX + ML Kit — QR scanning
- `HttpURLConnection` — in-app client for downloading from another device

## License

[MIT](LICENSE)
