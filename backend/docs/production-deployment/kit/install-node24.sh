#!/usr/bin/env bash
set -euo pipefail
[[ $(id -u) == 0 ]] || { echo 'Run with sudo.' >&2; exit 2; }
[[ $(uname -m) == x86_64 ]] || { echo 'This bundle targets Linux x86_64.' >&2; exit 2; }
node_tmp=$(mktemp -d)
trap 'rm -rf "$node_tmp"' EXIT
cd "$node_tmp"
curl --fail --show-error --silent --location \
 https://nodejs.org/download/release/latest-v24.x/SHASUMS256.txt -o SHASUMS256.txt
awk '$2 ~ /^node-v24\.[0-9]+\.[0-9]+-linux-x64\.tar\.xz$/ { print; n++ } END { if (n!=1) exit 1 }' \
 SHASUMS256.txt > selected.sha256
node_archive=$(awk '{print $2}' selected.sha256)
curl --fail --show-error --silent --location \
 "https://nodejs.org/download/release/latest-v24.x/$node_archive" -o "$node_archive"
sha256sum --check selected.sha256
node_dir=${node_archive%.tar.xz}
tar -xJf "$node_archive" -C /opt
ln -sfn "/opt/$node_dir/bin/node" /usr/local/bin/node
ln -sfn "/opt/$node_dir/bin/npm" /usr/local/bin/npm
ln -sfn "/opt/$node_dir/bin/npx" /usr/local/bin/npx
node --version
