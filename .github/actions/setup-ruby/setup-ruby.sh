#!/usr/bin/env bash

# Fail if any commands fails.
set -e

echo "::group::Install rbenv"
brew install rbenv
RBENV_SHIMS_DIR=$(rbenv root)/shims
# Make it work in this action.
PATH="$RBENV_SHIMS_DIR:$PATH"
# Make it work for subsequent actions.
# https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#adding-a-system-path
echo "$RBENV_SHIMS_DIR" >> "$GITHUB_PATH"
echo "::endgroup::"

if ! brew list --formula | grep -q "^gmp$"; then
  echo "::group::Install gmp"
  brew install gmp
  echo "::endgroup::"
fi

echo "::group::Install ruby"
rbenv install "$(cat .ruby-version)" --verbose
echo "::endgroup::"

echo "::group::Install gems"
gem install bundler
bundle install
echo "::endgroup::"
