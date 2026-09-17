# Off-page SEO / outreach drafts

Ready-to-post copy for promoting Quick File Share. Post **after** the landing page
(<https://xechoz.github.io/quick-file-share/>) is live, and always link to the landing
page or the GitHub repo — consistent backlinks are what move search rankings.

Rule of thumb: don't paste the same text everywhere. Adapt the intro to each community,
and read the subreddit rules on self-promotion first.

---

## 1. awesome-kotlin (PR)

Repo: <https://github.com/KotlinBy/awesome-kotlin>
Add under the **Applications → Android / Desktop** area (keep alphabetical order):

```
- [Quick File Share](https://github.com/xechoz/quick-file-share) - Share and receive files over the local network from Android and Desktop; the other device only needs a browser.
```

PR title: `Add Quick File Share`

## 2. awesome-compose-multiplatform (PR)

Repo: <https://github.com/JetBrains/compose-multiplatform-awesome> (or the community list found via GitHub search for `awesome compose multiplatform`)
Add to the **Applications** section:

```
- [Quick File Share](https://github.com/xechoz/quick-file-share) - Cross-platform LAN file transfer for Android and Desktop with a built-in HTTP server, QR codes and browser-based receiving.
```

## 3. awesome-selfhosted (PR — check eligibility first)

Repo: <https://github.com/awesome-selfhosted/awesome-selfhosted>
This list targets server-hosted software. Quick File Share is a peer-to-peer app, so it may be
rejected. If you try, add it under **File Transfer - Single-click & Drag-n-drop Upload** only if
the maintainers accept LAN tools, otherwise skip. Entry:

```
- [Quick File Share](https://github.com/xechoz/quick-file-share) - Share files between devices over the local network; the other device needs no app, just a browser. ([Demo](https://xechoz.github.io/quick-file-share/)) `MIT` `Kotlin`
```

## 4. Reddit

### r/selfhosted

Title: `I built a LAN file transfer tool where the other device only needs a browser`

Body:

> I wanted something simpler than setting up Syncthing just to move a file between my phone and
> laptop. Quick File Share starts a tiny HTTP server on your device and shows a QR code / URL.
> The other side opens it in a browser and downloads or uploads — no app install, no cloud, no
> account, works offline.
>
> Android + Linux/Windows/macOS, MIT licensed, built with Kotlin/Compose Multiplatform.
> Repo: https://github.com/xechoz/quick-file-share — site: https://xechoz.github.io/quick-file-share/
>
> Feedback welcome, especially on the desktop firewall flow.

### r/androidapps

Title: `Quick File Share — send files to any device on your Wi-Fi, no app needed on the other side`

Body: same story, lead with the Android experience, mention APK download and Material 3 UI.

### r/kotlin

Title: `Quick File Share: a KMP / Compose Multiplatform LAN file transfer app (Android + Desktop)`

Body: focus on the architecture — shared Compose UI, `jvmCommonMain` HTTP server, `expect`/`actual`
for the picker/scanner, and the AGP 9 `com.android.kotlin.multiplatform.library` note. Ask for
code feedback.

### r/opensource

Title: `Quick File Share — a small MIT-licensed LAN file transfer app (Kotlin + Compose Multiplatform)`

## 5. Show HN

Title: `Show HN: Quick File Share – LAN file transfer, no app needed on the receiving device`

URL: `https://xechoz.github.io/quick-file-share/`

First comment (post it yourself right after submitting):

> Author here. I use AirDrop/LocalSend daily but often need to send a file to a device that
> doesn't have the app installed (a TV, a work laptop, a friend's phone). Quick File Share runs a
> small embeddable web server on the sending device (NanoHTTPD) and just shows a QR code. The
> other side opens the URL in a browser and downloads or uploads.
>
> Android + Desktop (Linux/Windows/macOS), written in Kotlin with Compose Multiplatform. The
> desktop builds are slim (no bundled JRE, depends on system Java 17+). MIT licensed. Happy to
> answer anything about the KMP setup or the firewall handling.

## 6. AlternativeTo

URL: <https://alternativeto.net/manage/new/>
Name: `Quick File Share`
Licenses: Open Source, Free
Platforms: Android, Linux, Windows, macOS
Categories: File Sharing, File Transfer, Local Network
Alternatives to: **LocalSend** (and optionally AirDrop, Snapdrop)
Description: use the same text as the repo description.

## 7. Other listings (optional, high SEO value)

- **F-Droid**: needs an MR against <https://gitlab.com/fdroid/fdroiddata>; check the inclusion
  policy (no proprietary dependencies, reproducible builds, `Fastlane` metadata). This alone can
  rank for "android file transfer".
- **Flathub**: ship an AppImage/Flatpak if you want the Flathub listing and its backlink.
- **AUR**: already producing Arch packages — an AUR entry links back to the repo.
- **Product Hunt**: optional launch, good for a burst of backlinks.

---

## After publishing: verify indexing (2–4 weeks)

- Google: `site:github.com/xechoz/quick-file-share` and `site:xechoz.github.io/quick-file-share`
- Submit both the repo and the Pages site in **Google Search Console** (verify the `github.io`
  property and submit `https://xechoz.github.io/quick-file-share/sitemap.xml`).
- Expect the brand query ("quick file share") to rank first; generic queries
  ("share files local network") take longer and depend on the backlinks above.
