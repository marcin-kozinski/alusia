#!/usr/bin/env bash

encrypt() {
  INPUT=$1
  gpg --batch --yes --passphrase="$GPG_KEY" --cipher-algo AES256 --symmetric --output "$INPUT.gpg" "$INPUT"
}

if [[ -z "$GPG_KEY" ]]; then
  read -p "GPG key: " -r -s
  echo # (optional) move to a new line
  GPG_KEY="$REPLY"
fi

encrypt "secret-environment-variables.sh"
encrypt "release.jks"
encrypt "alusia-niezapominajka-a262eaffa2b9.json"
