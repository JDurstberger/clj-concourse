#!/usr/bin/env bash

[ -n "$DEBUG" ] && set -x
set -e
set -o pipefail

bash < <(curl -s https://raw.githubusercontent.com/babashka/babashka/v0.6.5/install)
cd ..
