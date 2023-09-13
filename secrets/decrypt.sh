#!/usr/bin/env bash

decrypt() {
  OUTPUT=$1
  gpg --quiet --batch --yes --decrypt --passphrase="$GPG_KEY" --output "$OUTPUT" "$OUTPUT.gpg"
}

if [[ -z "$GPG_KEY" ]]; then
  read -p "GPG key: " -r -s
  echo # (optional) move to a new line
  GPG_KEY="$REPLY"
fi

decrypt "secret-environment-variables.sh"
decrypt "release.jks"
decrypt "alusia-niezapominajka-a262eaffa2b9.json"
