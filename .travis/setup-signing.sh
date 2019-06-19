#!/usr/bin/env bash
set -x

# If this is a master build then lay out signing information
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  openssl aes-256-cbc -K $encrypted_1e42ed2d98cb_key -iv $encrypted_1e42ed2d98cb_iv -in .travis/signingkey.asc.enc -out .travis/signingkey.asc -d
  gpg --version
  gpg --fast-import .travis/signingkey.asc
  rm .travis/signingkey.asc*
fi