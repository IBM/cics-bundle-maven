#!/usr/bin/env bash
set -x

# If this is a master build then deploy at the end
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  mvn -Dstyle.color=always -B -U -P sign --settings travis-settings.xml deploy &&
  mvn site site:stage post-site;
# Otherwise just build and test
else
  mvn -Dstyle.color=always -B -U --settings travis-settings.xml verify &&
  mvn site site:stage post-site;
fi
