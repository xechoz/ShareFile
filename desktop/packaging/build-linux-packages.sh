#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

NFPM_VERSION="2.47.0"
NFPM_BIN="$ROOT/desktop/build/tools/nfpm"
NFPM_CONFIG="$ROOT/desktop/build/nfpm.resolved.yaml"
DIST_DIR="$ROOT/desktop/build/dist"

VERSION="$(sed -n 's/^app\.version=//p' gradle.properties | head -1)"
if [ -z "$VERSION" ]; then
  echo "app.version is missing from gradle.properties" >&2
  exit 1
fi
export VERSION
export ROOT_DIR="$ROOT"

./gradlew :desktop:slimDist --console=plain

if [ ! -x "$NFPM_BIN" ]; then
  mkdir -p "$(dirname "$NFPM_BIN")"
  curl -fsSL "https://github.com/goreleaser/nfpm/releases/download/v${NFPM_VERSION}/nfpm_${NFPM_VERSION}_Linux_x86_64.tar.gz" \
    | tar -xz -C "$(dirname "$NFPM_BIN")" nfpm
  chmod +x "$NFPM_BIN"
fi

rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

envsubst < desktop/packaging/nfpm.yaml > "$NFPM_CONFIG"

for format in deb rpm archlinux; do
  "$NFPM_BIN" package -f "$NFPM_CONFIG" -p "$format" -t "$DIST_DIR/"
done

ls -lh "$DIST_DIR"
