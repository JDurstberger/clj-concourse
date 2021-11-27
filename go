#!/usr/bin/env bash

[ -n "$GO_DEBUG" ] && set -x
set -e

project_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

skip_checks="no"

missing_dependency="no"

[ -n "$GO_DEBUG" ] && verbose="yes"
[ -n "$GO_SKIP_CHECKS" ] && skip_checks="yes"


if [[ "$skip_checks" = "no" ]]; then
    echo "Checking for system dependencies."
    if ! type bb >/dev/null 2>&1 || ! bb --version | grep -q "babashka"; then
        echo "This codebase requires Babashka $bb_version."
        missing_dependency="yes"
    fi

    if ! type clojure >/dev/null 2>&1 || ! clojure --version | grep -q "Clojure CLI"; then
        echo "This codebase requires Clojure."
        missing_dependency="yes"
    fi

    if [[ "$missing_dependency" = "yes" ]]; then
        echo "Please install missing dependencies to continue."
        exit 1
    fi

    echo "All system dependencies present. Continuing."
fi

echo "Starting babashka"
if [[ "$#" -eq 0 ]]; then
   bb run default
else
  bb run "$@"
fi
